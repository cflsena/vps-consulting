// Stress test de concorrencia: debito de saldo e chave de idempotencia (projeto kotlin/ -
// Credit-Management).
//
// Dois cenarios independentes, cada um contra um parceiro isolado criado no setup():
//
//   1) balance_race: N requisicoes concorrentes de debito (POST .../transactions/debit) contra
//      um parceiro com saldo fixo, cada uma com idempotencyKey UNICA. Valida que o saldo nunca
//      fica negativo, ou seja, que o UPDATE condicional que debita o saldo e realmente atomico
//      sob concorrencia real via HTTP (e nao so dentro da mesma JVM, como nos testes JUnit
//      existentes).
//
//   2) idempotency_race: M requisicoes concorrentes de debito disparadas ao mesmo tempo contra
//      outro parceiro, todas com a MESMA idempotencyKey. Como ha uma janela entre o SELECT que
//      checa duplicidade e o INSERT da transacao (protegida apenas pela constraint UNIQUE no
//      banco), o objetivo e provar que, mesmo sob corrida real, no maximo uma requisicao e
//      processada como sucesso e as demais recebem 409 de forma limpa - nunca um erro 500.
//
// Uso (Docker, ver docker-compose.k6.yml na raiz do repositorio):
//   docker compose -f docker-compose.k6.yml run --rm k6-transaction-race
//
// Uso (k6 instalado localmente):
//   k6 run -e BASE_URL=http://localhost:8081 k6/transaction-credit-race.js
//
// Parametros (via -e):
//   BASE_URL                  URL base da API (default http://localhost:8080)
//   BALANCE_VUS                VUs concorrentes no cenario de saldo (default 30)
//   BALANCE_DEBIT_AMOUNT        Valor de cada debito no cenario de saldo (default 1000)
//   BALANCE_INITIAL_CREDIT      Credito inicial do parceiro do cenario de saldo (default 10000)
//   IDEMPOTENCY_VUS             VUs concorrentes no cenario de idempotencia (default 20)
//   IDEMPOTENCY_DEBIT_AMOUNT    Valor do debito (mesma idempotencyKey) (default 500)

import http from 'k6/http';
import { check } from 'k6';
import { Counter } from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const BALANCE_VUS = Number(__ENV.BALANCE_VUS || 30);
const BALANCE_DEBIT_AMOUNT = Number(__ENV.BALANCE_DEBIT_AMOUNT || 1000);
const BALANCE_INITIAL_CREDIT = Number(__ENV.BALANCE_INITIAL_CREDIT || 10000);
const IDEMPOTENCY_VUS = Number(__ENV.IDEMPOTENCY_VUS || 20);
const IDEMPOTENCY_DEBIT_AMOUNT = Number(__ENV.IDEMPOTENCY_DEBIT_AMOUNT || 500);

const debitsCompleted = new Counter('debits_completed');
const debitsFailed = new Counter('debits_failed');
const debitsUnexpected = new Counter('debits_unexpected');
const idempotencyAccepted = new Counter('idempotency_accepted');
const idempotencyConflict = new Counter('idempotency_conflict');
const idempotencyUnexpected = new Counter('idempotency_unexpected');

export const options = {
  scenarios: {
    balance_race: {
      executor: 'per-vu-iterations',
      vus: BALANCE_VUS,
      iterations: 1,
      maxDuration: '30s',
      exec: 'balanceRace',
    },
    idempotency_race: {
      executor: 'per-vu-iterations',
      vus: IDEMPOTENCY_VUS,
      iterations: 1,
      maxDuration: '30s',
      exec: 'idempotencyRace',
    },
  },
};

function createPartner(name) {
  const headers = { 'Content-Type': 'application/json' };
  const payload = JSON.stringify({
    name,
    document: String(Date.now() + Math.floor(Math.random() * 1000)).padStart(14, '0'),
  });
  const res = http.post(`${BASE_URL}/api/v1/b2b/partners`, payload, { headers });
  const created = check(res, { [`partner created (201) - ${name}`]: (r) => r.status === 201 });
  if (!created) {
    throw new Error(`Falha ao criar parceiro "${name}": ${res.status} ${res.body}`);
  }
  return res.json('id');
}

function creditPartner(partnerId, amount, idempotencyKey) {
  const headers = { 'Content-Type': 'application/json' };
  const payload = JSON.stringify({
    amount,
    description: 'k6 setup - saldo inicial para teste de stress',
    idempotencyKey,
  });
  const res = http.post(`${BASE_URL}/api/v1/b2b/partners/${partnerId}/transactions/credit`, payload, { headers });
  const ok = check(res, { 'credito inicial aplicado (COMPLETED)': (r) => r.status === 201 && r.json('status') === 'COMPLETED' });
  if (!ok) {
    throw new Error(`Falha ao creditar saldo inicial do parceiro ${partnerId}: ${res.status} ${res.body}`);
  }
}

export function setup() {
  const balancePartnerId = createPartner(`K6 Balance Race ${Date.now()}`);
  creditPartner(balancePartnerId, BALANCE_INITIAL_CREDIT, `k6-setup-balance-${Date.now()}`);

  const idempotencyPartnerId = createPartner(`K6 Idempotency Race ${Date.now()}`);
  creditPartner(idempotencyPartnerId, IDEMPOTENCY_DEBIT_AMOUNT, `k6-setup-idempotency-${Date.now()}`);

  // Gerada uma unica vez aqui (setup roda uma unica vez, fora do contexto de cada VU) e
  // propagada via "data" para todas as VUs do cenario idempotency_race. Se fosse uma constante
  // de modulo, cada VU do k6 re-executaria o init code isoladamente e teria seu proprio valor -
  // o que destruiria o proposito do teste (todas as VUs precisam disputar a MESMA chave).
  const sharedIdempotencyKey = `k6-idempotency-race-${Date.now()}`;

  console.log(`Parceiro balance_race: ${balancePartnerId} (credito=${BALANCE_INITIAL_CREDIT}, debito=${BALANCE_DEBIT_AMOUNT}, vus=${BALANCE_VUS})`);
  console.log(`Parceiro idempotency_race: ${idempotencyPartnerId} (debito=${IDEMPOTENCY_DEBIT_AMOUNT}, vus=${IDEMPOTENCY_VUS}, key=${sharedIdempotencyKey})`);

  return { balancePartnerId, idempotencyPartnerId, sharedIdempotencyKey };
}

export function balanceRace(data) {
  const headers = { 'Content-Type': 'application/json' };
  const payload = JSON.stringify({
    amount: BALANCE_DEBIT_AMOUNT,
    description: 'k6 stress test - debito concorrente',
    idempotencyKey: `k6-balance-race-${__VU}-${Date.now()}-${Math.random()}`,
  });

  const res = http.post(`${BASE_URL}/api/v1/b2b/partners/${data.balancePartnerId}/transactions/debit`, payload, { headers });

  const status = res.status === 201 ? res.json('status') : null;
  if (status === 'COMPLETED') {
    debitsCompleted.add(1);
  } else if (status === 'FAILED') {
    debitsFailed.add(1);
  } else {
    debitsUnexpected.add(1);
    console.error(`balance_race - resposta inesperada: ${res.status} ${res.body}`);
  }

  check(res, {
    'balance_race: http 201 (resultado vem no corpo)': (r) => r.status === 201,
    'balance_race: status do corpo e COMPLETED ou FAILED': () => status === 'COMPLETED' || status === 'FAILED',
  });
}

export function idempotencyRace(data) {
  const headers = { 'Content-Type': 'application/json' };
  const payload = JSON.stringify({
    amount: IDEMPOTENCY_DEBIT_AMOUNT,
    description: 'k6 stress test - corrida de idempotencia',
    idempotencyKey: data.sharedIdempotencyKey,
  });

  const res = http.post(`${BASE_URL}/api/v1/b2b/partners/${data.idempotencyPartnerId}/transactions/debit`, payload, { headers });

  if (res.status === 201 && res.json('status') === 'COMPLETED') {
    idempotencyAccepted.add(1);
  } else if (res.status === 409) {
    idempotencyConflict.add(1);
  } else {
    idempotencyUnexpected.add(1);
    console.error(`idempotency_race - resposta inesperada: ${res.status} ${res.body}`);
  }

  check(res, {
    'idempotency_race: nunca retorna 500': (r) => r.status !== 500,
    'idempotency_race: apenas 201 (aceita) ou 409 (chave duplicada)': (r) => r.status === 201 || r.status === 409,
  });
}

export function teardown(data) {
  const balanceRes = http.get(`${BASE_URL}/api/v1/b2b/partners/${data.balancePartnerId}/balance`);
  check(balanceRes, { 'balance endpoint acessivel (balance_race)': (r) => r.status === 200 });
  const balance = balanceRes.json();
  console.log(`Estado final do saldo (balance_race): ${JSON.stringify(balance)}`);

  const available = Number(balance.availableBalance);
  const totalDebited = Number(balance.totalDebited);

  check(balance, {
    'balance_race: availableBalance nunca fica negativo': () => available >= -0.01,
    'balance_race: totalDebited e multiplo exato do valor do debito': () =>
      Math.abs(Math.round(totalDebited / BALANCE_DEBIT_AMOUNT) * BALANCE_DEBIT_AMOUNT - totalDebited) < 0.01,
    'balance_race: totalDebited nao excede o credito inicial': () => totalDebited <= BALANCE_INITIAL_CREDIT + 0.01,
  });

  const idempotencyRes = http.get(`${BASE_URL}/api/v1/b2b/partners/${data.idempotencyPartnerId}/balance`);
  check(idempotencyRes, { 'balance endpoint acessivel (idempotency_race)': (r) => r.status === 200 });
  const idempotencyBalance = idempotencyRes.json();
  console.log(`Estado final do saldo (idempotency_race): ${JSON.stringify(idempotencyBalance)}`);

  check(idempotencyBalance, {
    'idempotency_race: exatamente um debito foi aplicado (availableBalance == 0)': () =>
      Math.abs(Number(idempotencyBalance.availableBalance)) < 0.01,
  });
}

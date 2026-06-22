// Stress test de concorrência: reserva de credito ao criar pedidos (projeto java/ - B2B-Management).
//
// O que valida: ao criar um pedido (POST /api/v1/b2b/orders), o sistema reserva parte do
// credito disponivel do parceiro (partner_credit.reserved_balance). Esse teste cria um parceiro
// isolado com um limite de credito fixo e dispara N requisicoes concorrentes de criacao de
// pedido, cada uma reservando uma fracao fixa do limite. Se a reserva de credito nao for
// atomica no nivel do banco, o total reservado pode ultrapassar o limite configurado
// (overbooking).
//
// Uso (Docker, ver docker-compose.k6.yml na raiz do repositorio):
//   docker compose -f docker-compose.k6.yml run --rm k6-order-race
//
// Uso (k6 instalado localmente):
//   k6 run -e BASE_URL=http://localhost:8080 k6/order-credit-race.js
//
// Parametros (via -e):
//   BASE_URL        URL base da API (default http://localhost:8080)
//   VUS             Quantidade de VUs concorrentes, cada uma criando 1 pedido (default 30)
//   ORDER_AMOUNT    Valor de cada pedido (default 1000)
//   CREDIT_LIMIT    Limite de credito do parceiro de teste (default 10000)

import http from 'k6/http';
import { check } from 'k6';
import { Counter } from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const VUS = Number(__ENV.VUS || 30);
const ORDER_AMOUNT = Number(__ENV.ORDER_AMOUNT || 1000);
const CREDIT_LIMIT = Number(__ENV.CREDIT_LIMIT || 10000);

const ordersApproved = new Counter('orders_approved');
const ordersRejected = new Counter('orders_rejected');
const ordersUnexpected = new Counter('orders_unexpected');

export const options = {
  scenarios: {
    concurrent_order_burst: {
      executor: 'per-vu-iterations',
      vus: VUS,
      iterations: 1,
      maxDuration: '30s',
    },
  },
};

export function setup() {
  const headers = { 'Content-Type': 'application/json' };
  const payload = JSON.stringify({
    name: `K6 Stress Partner ${Date.now()}`,
    document: String(Date.now()).padStart(14, '0'),
    creditLimit: CREDIT_LIMIT,
  });

  const res = http.post(`${BASE_URL}/api/v1/b2b/partners`, payload, { headers });
  const created = check(res, { 'partner created (201)': (r) => r.status === 201 });
  if (!created) {
    throw new Error(`Falha ao criar parceiro de teste: ${res.status} ${res.body}`);
  }

  const partnerId = res.json('id');
  console.log(`Parceiro de teste criado: ${partnerId} (creditLimit=${CREDIT_LIMIT}, orderAmount=${ORDER_AMOUNT}, vus=${VUS})`);
  return { partnerId };
}

export default function (data) {
  const headers = { 'Content-Type': 'application/json' };
  const payload = JSON.stringify({
    partnerId: data.partnerId,
    items: [
      {
        productId: `prod-${__VU}`,
        quantity: 1,
        unitPrice: ORDER_AMOUNT,
      },
    ],
  });

  const res = http.post(`${BASE_URL}/api/v1/b2b/orders`, payload, { headers });

  if (res.status === 201) {
    ordersApproved.add(1);
  } else if (res.status === 422) {
    ordersRejected.add(1);
  } else {
    ordersUnexpected.add(1);
    console.error(`Resposta inesperada: ${res.status} ${res.body}`);
  }

  check(res, {
    'status e 201 (aprovado) ou 422 (credito insuficiente)': (r) => r.status === 201 || r.status === 422,
  });
}

export function teardown(data) {
  const res = http.get(`${BASE_URL}/api/v1/b2b/partners/${data.partnerId}/credit`);
  const reachable = check(res, { 'credit endpoint acessivel': (r) => r.status === 200 });
  if (!reachable) {
    throw new Error(`Falha ao consultar credito final do parceiro: ${res.status} ${res.body}`);
  }

  const credit = res.json();
  console.log(`Estado final do credito do parceiro: ${JSON.stringify(credit)}`);

  const reserved = Number(credit.reservedBalance);
  const available = Number(credit.availableBalance);
  const limit = Number(credit.creditLimit);

  check(credit, {
    // Invariante real do modelo (ver java/README.md): available_balance só muda quando um
    // pedido é confirmado/cancelado; reserved_balance é o soft-hold dos pedidos PENDING. Como
    // nenhum pedido deste teste avança de status, o invariante de concorrência a provar é
    // "reserved nunca passa do que estava disponível no momento da reserva" - nunca excede
    // creditLimit nem availableBalance.
    'reservedBalance nunca excede creditLimit (sem overbooking)': () => reserved <= limit + 0.01,
    'reservedBalance nunca excede availableBalance (sem overbooking)': () => reserved <= available + 0.01,
    'availableBalance nunca fica negativo': () => available >= -0.01,
    'reservedBalance e multiplo exato do valor do pedido (sem escrita parcial/duplicada)': () =>
      Math.abs(Math.round(reserved / ORDER_AMOUNT) * ORDER_AMOUNT - reserved) < 0.01,
  });
}

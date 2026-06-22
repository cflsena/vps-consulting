# VPS Consulting — B2B Credit/Order Management

Este repositório contém duas implementações independentes do mesmo desafio de negócio
(gestão de crédito/saldo de parceiros B2B), em stacks diferentes:

- [`java/`](java/README.md) — **B2B-Management**: gerencia o ciclo de vida de pedidos
  (`Order`), reservando crédito do parceiro no momento da criação (modelo *hold-and-capture*).
- [`kotlin/`](kotlin/README.md) — **Credit-Management**: gerencia saldo de parceiros através de
  transações de crédito/débito (`Transaction`), com idempotência garantida por chave única.

Cada projeto tem seu próprio `README.md` com a arquitetura, decisões técnicas e o racional por
trás do controle de concorrência adotado (ambos usam `UPDATE` condicional atômico no PostgreSQL
em vez de locks explícitos — ver a seção "Concorrência sem locks explícitos" em cada um). Este
README é mais genérico: explica como subir as duas aplicações de uma vez e como rodar o teste de
stress de concorrência com [k6](https://k6.io/) contra elas.

---

## Subindo as duas aplicações

Há um `docker-compose.yml` na raiz que sobe as duas aplicações simultaneamente, cada uma com seu
próprio Postgres e RabbitMQ, em portas distintas:

```bash
docker compose up -d --build
```

| Serviço | Descrição | Porta no host |
|---|---|---|
| `app-java` | API do projeto `java/` (B2B-Management) | `8080` |
| `postgres-java` | Banco do projeto `java/` (`b2b_management`) | `5432` |
| `rabbitmq-java` | RabbitMQ do projeto `java/` (management UI) | `5672` / `15672` |
| `app-kotlin` | API do projeto `kotlin/` (Credit-Management) | `8081` |
| `postgres-kotlin` | Banco do projeto `kotlin/` (`credits`) | `5433` |
| `rabbitmq-kotlin` | RabbitMQ do projeto `kotlin/` (management UI) | `5673` / `15673` |

Confirme que as duas aplicações subiram corretamente:

```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
```

Para derrubar tudo: `docker compose down` (use `-v` se quiser também remover os volumes dos
bancos).

---

## Teste de stress de concorrência (k6)

### Por que esse teste existe

Os dois projetos já garantem, na camada de persistência, que a reserva de crédito (`java/`) e o
débito de saldo (`kotlin/`) são operações atômicas no banco — um único `UPDATE` condicional
(`WHERE saldo_disponivel >= :valor`), sem `SELECT FOR UPDATE` nem lock otimista. Isso já é
validado por testes JUnit que disparam dezenas/centenas de threads **dentro da mesma JVM**
diretamente contra o repositório (`OrderCreationConcurrencyIT`, `DefaultPartnerRepositoryConcurrencyIT`
no `java/`; `DefaultPartnerBalanceRepositoryConcurrencyIT` no `kotlin/`).

O que esses testes não cobrem é o comportamento sob concorrência real **através da API HTTP** —
múltiplas conexões TCP simultâneas, pool de conexões do banco, pool de threads do servidor web,
serialização/desserialização JSON. O k6 dispara essa carga como um cliente externo faria,
funcionando como uma camada complementar de validação "black-box" para os mesmos invariantes.

No `kotlin/` há ainda um caso adicional: a verificação de `idempotencyKey` duplicada é feita por
um `SELECT` antes do `INSERT`, protegida apenas pela constraint `UNIQUE` no banco como última
linha de defesa — existe uma janela de corrida (TOCTOU) entre o `SELECT` e o `INSERT` que só se
manifesta com requisições verdadeiramente concorrentes (não com retries sequenciais). É
exatamente esse tipo de corrida que o teste de idempotência do k6 tenta expor.

### O que cada cenário valida

**`k6/order-credit-race.js`** (projeto `java/`):
- Cria um parceiro isolado com um limite de crédito fixo.
- Dispara N requisições concorrentes de criação de pedido (`POST /api/v1/b2b/orders`), cada uma
  reservando uma fração fixa do limite.
- Ao final, verifica que: o saldo reservado nunca excedeu o limite de crédito nem o saldo
  disponível do parceiro (sem *overbooking*), e que o saldo disponível nunca ficou negativo.

**`k6/transaction-credit-race.js`** (projeto `kotlin/`), dois cenários em paralelo, cada um
contra um parceiro isolado:
- **`balance_race`**: N débitos concorrentes (`POST .../transactions/debit`) de valor fixo, cada
  um com uma `idempotencyKey` própria, contra um parceiro com saldo limitado. Valida que o saldo
  disponível nunca fica negativo e que o total debitado é sempre múltiplo exato do valor de cada
  débito (sem escrita parcial/duplicada).
- **`idempotency_race`**: M débitos disparados ao mesmo tempo, todos com a **mesma**
  `idempotencyKey`. O esperado é que exatamente uma requisição seja processada com sucesso e
  todas as demais recebam `409` de forma limpa — o check mais importante deste cenário é que
  **nenhuma resposta seja `500`**, o que provaria uma falha real no tratamento da corrida.

### Rodando os testes (Docker)

k6 não precisa ser instalado localmente — há um `docker-compose.k6.yml` na raiz com um serviço
por script, usando a imagem oficial `grafana/k6` conectada à mesma rede Docker das aplicações
(resolve `app-java` e `app-kotlin` pelo nome do serviço).

**Pré-requisito:** as aplicações precisam estar de pé (`docker compose up -d --build`, seção
anterior).

```bash
# Teste de reserva de crédito (java/)
docker compose -f docker-compose.k6.yml run --rm k6-order-race

# Teste de saldo e idempotência (kotlin/)
docker compose -f docker-compose.k6.yml run --rm k6-transaction-race
```

Para ajustar a carga, sobrescreva as variáveis de ambiente do serviço (ver valores default em
`docker-compose.k6.yml` e nos comentários de cada script em `k6/`):

```bash
# 50 pedidos concorrentes contra um limite de crédito de 20000
VUS=50 CREDIT_LIMIT=20000 docker compose -f docker-compose.k6.yml run --rm k6-order-race

# 80 débitos concorrentes de 200 cada, contra um saldo inicial de 5000
BALANCE_VUS=80 BALANCE_DEBIT_AMOUNT=200 BALANCE_INITIAL_CREDIT=5000 \
  docker compose -f docker-compose.k6.yml run --rm k6-transaction-race
```

### Rodando os testes com k6 instalado localmente

Alternativa sem Docker, apontando direto para os hosts publicados:

```bash
k6 run -e BASE_URL=http://localhost:8080 k6/order-credit-race.js
k6 run -e BASE_URL=http://localhost:8081 k6/transaction-credit-race.js
```

### Interpretando o resultado

Ao final da execução, o k6 imprime um resumo de `checks` (ex.: `checks_succeeded: 100.00%
✓ 42 ✗ 0`). Esse resumo é a evidência principal: qualquer `✗` (check falho) indica que algum
invariante de concorrência foi violado — overbooking de crédito, saldo negativo, ou uma resposta
`500` na corrida de idempotência. Os logs de `setup()`/`teardown()` (impressos antes do resumo)
mostram o estado final do crédito/saldo do parceiro de teste, útil para investigar qualquer
falha apontada pelos `checks`.

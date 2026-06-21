# B2B Management API

## Visão Geral

API REST para gestão de crédito de parceiros B2B e controle do ciclo de vida de pedidos.

O contexto de negócio: parceiros têm um limite de crédito configurável. Quando um pedido é criado, parte desse crédito é reservado (soft hold). Conforme o pedido avança no seu ciclo de vida, o crédito é debitado de forma definitiva ou devolvido caso o pedido seja cancelado. A API gerencia esse fluxo de ponta a ponta — desde a configuração do limite até a confirmação de entrega.

---

## Decisões

- **Código:** Aplicação de Clean Architecture com DDD em uma estrutura module-first (`order/`, `partner/`, `shared/`). Cada módulo tem suas próprias camadas de domain, application e infrastructure. A ideia central é que o domínio (regras de negócio, entidades, eventos) não tenha dependências externas — qualquer troca não afeta uma linha de regra de negócio.

- **Modelo de crédito (hold-and-capture):** O saldo de crédito de um parceiro é gerenciado por três campos: `credit_limit` (teto configurado), `available_balance` (saldo após débitos confirmados) e `reserved_balance` (soma dos soft holds de pedidos em aberto). A invariante é `available_balance - reserved_balance >= 0`. Cada transição de status do pedido dispara uma operação diferente sobre esses campos:

  | Transição | Operação |
  |---|---|
  | Criação do pedido (→ PENDING) | `reserved_balance += totalAmount` |
  | PENDING → APPROVED | `available_balance -= amount` e `reserved_balance -= amount` |
  | PENDING → CANCELED | `reserved_balance -= amount` |
  | APPROVED/IN_PROCESS/SENT → CANCELED | `available_balance += amount` (estorno) |

- **Concorrência sem locks explícitos:** Em um cenário de alto volume de criação de pedidos, ler o saldo, checar se há disponibilidade e depois atualizar cria uma race condition clássica:

  ```
  Thread A: SELECT available_balance - reserved_balance → 500
  Thread B: SELECT available_balance - reserved_balance → 500
  Thread A: 500 >= 400? Sim → UPDATE reserved_balance += 400
  Thread B: 500 >= 300? Sim → UPDATE reserved_balance += 300  ← overdraft
  ```

  Um possível solução seria `SELECT FOR UPDATE` ou `@Lock(PESSIMISTIC_WRITE)`. Funciona, porém o lock seguraria todos os pedidos de um mesmo parceiro, enquanto todas as outras aguardam a criação do pedido finalizar, diminuindo consideravelmente o throughput.

  A abordagem adotada aqui foi o **UPDATE condicional atômico** direto no PostgreSQL:

  ```sql
  UPDATE partner_credit
  SET reserved_balance = reserved_balance + :amount,
      updated_at = now()
  WHERE partner_id = :partnerId
    AND available_balance - reserved_balance >= :amount
  ```

  Um único statement SQL é atômico por definição, o PostgreSQL (por ser ACID) garante isso internamente. Se o saldo for insuficiente, a query retorna 0 linhas afetadas e o use case lança `InsufficientCreditException`.

  Dessa forma, evita-se deadlocks, contenções entre threads de parceiros diferentes, e o lock interno do PostgreSQL é mantido por microssegundos (apenas durante a escrita), não pelo tempo da transação da aplicação. 
  
  Para validar, foram implementados testes de concorrência com 200 threads simultâneas tentando criar pedidos para o mesmo parceiro e nenhuma produziu overdraft.


- **Anti-Corruption Layer:** O módulo `order` precisa operar crédito de parceiros, mas não pode depender diretamente do módulo `partner`. A interface `PartnerCreditService` (definida dentro do domínio de `order`) age como barreira: os casos de uso de pedido chamam `reserveCredit()`, `releaseReservation()`, etc., sem saber o que existe do outro lado.


- **Máquina de estados do pedido:** `OrderStatus` codifica quais transições são válidas. Qualquer tentativa de transição inválida lança `InvalidOrderTransitionException`. O status ``CANCELED`` é acessível de qualquer estado não-terminal porque cancelamento é uma realidade de negócio em qualquer ponto do ciclo de vida.


- **Value Objects:** `Money` encapsula `BigDecimal` + moeda, garante que o valor nunca seja negativo e impede operações entre currencies diferentes. `OrderId`, `PartnerId` e `OrderItemId` são tipos distintos — impossível passar um `PartnerId` onde um `OrderId` é esperado.

---

## Tech Stack

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 17 |
| Framework | Spring Boot 4.0.7 |
| Banco de Dados | PostgreSQL |
| ORM | Spring Data JPA + Hibernate |
| Migrations | Flyway |
| Mensageria | RabbitMQ + Spring AMQP |
| Testes | JUnit, Mockito, Testcontainers |
| Cobertura | JaCoCo |
| Documentação | SpringDoc OpenAPI (Swagger) |
| Containerização | Docker, Docker Compose |

---

## Arquitetura & Camadas

Estrutura module-first com Clean Architecture. O código é agrupado por módulo de negócio, e dentro de cada módulo por camada:

```
src/main/java/.../
├── order/
│   ├── domain/          ← Agregado Order, ports, eventos, exceções
│   ├── application/     ← Casos de uso (Create, Update, Find, List)
│   └── infrastructure/  ← Controller REST, JPA entities, repositórios, mappers
├── partner/
│   ├── domain/          ← Agregado Partner, ports, exceções
│   ├── application/     ← Casos de uso (Create, AdjustLimit, FindCredit, Replenish)
│   └── infrastructure/  ← Controller REST, JPA entities, repositórios, service ACL
└── shared/
    ├── core/            ← Money, Identifier, Entity, PageCustom, DomainEvent, exceções base
    └── infrastructure/  ← GlobalExceptionHandler, OpenApiConfig, RabbitMQEventPublisher
```

- **Domain:** Agregados (`Order`, `Partner`), value objects (`Money`, `OrderId`, `PartnerId`), ports (interfaces de repositório e serviço), eventos de domínio (`OrderCreated`, `OrderStatusChanged`), exceções de domínio. Sem dependências externas.


- **Application:** Casos de uso que orquestram domínio e ports. Cada operação tem sua própria classe: `DefaultCreateOrderUseCase`, `DefaultUpdateOrderStatusUseCase`, `DefaultAdjustCreditLimitUseCase`, `DefaultReplenishAvailableCreditUseCase`, etc.


- **Infrastructure:** Controllers REST com validação Bean Validation, entidades JPA, repositórios Spring Data com queries nativas para as operações atômicas, publicador RabbitMQ, mappers estáticos (sem MapStruct, sem beans — classes `final` com métodos estáticos).

---

## Endpoints

Documentação interativa disponível em `http://localhost:8080/swagger-ui/index.html`.

### Partners

**`POST /api/v1/b2b/partners`** — Cria um parceiro

```json
{ "name": "Empresa X", "document": "12.345.678/0001-99", "creditLimit": 50000.00 }
```

Cria o parceiro e inicializa o saldo de crédito com `available_balance = creditLimit` e `reserved_balance = 0`. Retorna 201 com o UUID gerado. Falha com 400 se os campos forem inválidos ou 409 se o documento já existir.

---

**`PATCH /api/v1/b2b/partners/{id}/credit-limit`** — Ajusta o limite de crédito

```json
{ "newCreditLimit": 80000.00 }
```

Valida que o novo limite não é menor que o total já comprometido (`creditLimit - availableBalance + reservedBalance`). Se passar na validação, executa um UPDATE atômico que recalcula `available_balance` automaticamente: `available_balance = available_balance + (newLimit - oldLimit)`. Retorna 204. Falha com 422 se o novo limite violar a invariante.

---

**`PATCH /api/v1/b2b/partners/{id}/available-credit`** — Reposição manual de saldo

```json
{ "amount": 10000.00 }
```

Caso de uso típico: parceiro quitou faturas e o crédito precisa ser reposto. Valida que `amount <= creditLimit - availableBalance` para não ultrapassar o limite. Executa `available_balance += amount`. Retorna 204.

---

**`GET /api/v1/b2b/partners/{id}/credit`** — Consulta de crédito

```json
{
  "creditLimit": 50000.00,
  "availableBalance": 35000.00,
  "reservedBalance": 5000.00,
  "updatedAt": "2024-01-15T10:30:00Z"
}
```

---

**`GET /api/v1/b2b/partners`** — Lista parceiros

Query params: `pageNumber` (default 0), `pageSize` (default 20).

---

### Orders

**`POST /api/v1/b2b/orders`** — Cria um pedido

```json
{
  "partnerId": "uuid-do-parceiro",
  "items": [
    { "productId": "PROD-001", "quantity": 3, "unitPrice": 150.00 }
  ]
}
```

A ordem das operações importa:

1. Valida os campos do request
2. Calcula `totalAmount` (somatório de todos os itens)
3. Executa o UPDATE atômico de reserva de crédito — se o saldo for insuficiente, a execução para aqui com `InsufficientCreditException` (422). Nenhum dado de pedido é persistido.
4. Persiste o `Order` com status PENDING e os `OrderItems`
5. Publica o evento `OrderCreated` no RabbitMQ
6. Retorna 201 com o UUID do pedido

---

**`GET /api/v1/b2b/orders`** — Lista pedidos com filtros

Query params: `from` e `to`, `status`, `partnerId`, `pageNumber` (default 0), `pageSize` (default 20). Todos os filtros são opcionais e combinam entre si.

---

**`GET /api/v1/b2b/orders/{id}`** — Busca por ID

Retorna os dados completos do pedido incluindo `totalAmount`, `status`, `createdAt`, `updatedAt`.

---

**`GET /api/v1/b2b/orders/{id}/items`** — Lista itens do pedido

Paginado com `pageNumber` e `pageSize`.

---

**`PATCH /api/v1/b2b/orders/{id}/status`** — Avança ou cancela o pedido

```json
{ "targetStatus": "APPROVED" }
```

Valida a transição em `OrderStatus`. Se inválida, retorna 422 (`InvalidOrderTransitionException`). Se válida, persiste o novo status e executa a operação de crédito correspondente (ver tabela na seção Decisões). Publica `OrderStatusChanged` — com informações de estorno se foi cancelamento. Retorna 204.

**Máquina de estados:**

```
PENDING → APPROVED → IN_PROCESS → SENT → DELIVERED
    ↓         ↓           ↓         ↓
CANCELED  CANCELED   CANCELED  CANCELED
```

---

## Estratégia de Testes

- **Unitários de domínio:** `MoneyTest`, `OrderTest`, `OrderStatusTest` (matriz completa de transições parametrizada), `PartnerTest`, testes de todos os value objects.


- **Unitários de caso de uso:** `DefaultCreateOrderUseCaseTest` valida que a reserva de crédito ocorre antes de persistir, que `InsufficientCreditException` impede a persistência, que eventos são publicados no fluxo feliz. Mesma cobertura para os demais use cases.


- **Integração com Testcontainers:** `OrderControllerIT`, `PartnerControllerIT`, `DefaultPartnerRepositoryIT`, `RabbitMQEventPublisherIT`. PostgreSQL e RabbitMQ reais, Flyway aplicado automaticamente, sem mocks de infraestrutura.


- **Concorrência:** `OrderCreationConcurrencyIT` e `DefaultPartnerRepositoryConcurrencyIT` disparam 200 threads simultâneas tentando reservar crédito para o mesmo parceiro. Validam que o número de sucesso nunca excede `creditLimit / orderAmount`, que `reserved_balance` é exatamente `successCount × orderAmount` e que nenhum CHECK constraint do banco é violado.


```bash
./mvnw test      # unitários + integração
./mvnw verify    # inclui o quality gate do JaCoCo
```

Thresholds de cobertura: domain ≥ 95%, application ≥ 85%, infrastructure ≥ 70%.

---

## Documentação & Qualidade

- **OpenAPI (Swagger UI):** Documentação completa disponível em `http://localhost:8080/swagger-ui/index.html` com todos os schemas de request/response e exemplos.


- **Observabilidade:**
  - `GET /actuator/health` — health check da aplicação, banco e broker
  - `GET /actuator/metrics` — métricas da JVM e da aplicação


- **Filas de eventos RabbitMQ:**

  | Exchange | Routing Key | Queue | Evento |
  |---|---|---|---|
  | `b2b.events` | `order.created` | `queue.order.created` | `OrderCreated` |
  | `b2b.events` | `order.status.changed` | `queue.order.status.changed` | `OrderStatusChanged` |

  Todos os eventos são encapsulados em um `EventMessage` com `eventId` (UUID), `occurredAt` e `eventType` — campos necessários para deduplicação e rastreamento pelos consumidores.

---

## Possíveis Melhorias

- **Java 21 e Virtual Threads:** para altas cargas de I/O-bound.

- **Rate Limiting:** para evitar abuso e picos inesperados.

- **Redis:** para cache de consultas frequentes de saldo, parceiros etc.

- **Idempotência:** na criação de pedidos para evitar duplicidade e reservar crédito múltiplas vezes.

- **Publicação de eventos pós-commit:** publicar somente após a confirmação da transação, a fim de evitar lançar eventos inconsistentes.

- **Dead Letter Queue:** mensagens que falham no consumo precisam de um destino explícito para que possam ser tratadas posteriormente.

---

## Execução Local

**Requisitos:** Docker e Docker Compose v2.

```bash
cp .env.example .env
docker compose up -d --build
```

Aguardar o health check passar:

```bash
curl http://localhost:8080/actuator/health
```

Swagger disponível em `http://localhost:8080/swagger-ui/index.html`.

### Parceiros disponíveis após o seed

O Flyway aplica automaticamente o script `V2__seed_partners.sql` na inicialização. Três parceiros já estão cadastrados e prontos para uso:

| Nome | ID | Documento (CNPJ) | Limite de Crédito | Saldo Disponível |
|---|---|---|---|---|
| Empresa A | `c0892412-4fb5-4550-9b91-b3a3e5fe0e68` | 64781774000180 | R$ 10.000,00 | R$ 10.000,00 |
| Empresa B | `53924ce9-7a07-419f-b6c0-3807d4276350` | 06228417000192 | R$ 50.000,00 | R$ 50.000,00 |
| Empresa C | `ac666aa0-0d46-4a68-b3c8-9a48cb62ad7a` | 90468921000176 | R$ 200.000,00 | R$ 200.000,00 |

Todos os parceiros começam com `reserved_balance = 0`.

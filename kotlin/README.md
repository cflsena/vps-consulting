# B2B Credit Management API

## Visão Geral

API REST para gestão de saldo de crédito de parceiros B2B através de transações de crédito e débito.

O contexto de negócio: cada parceiro possui um saldo de crédito composto por `total_balance` (total já creditado historicamente) e `available_balance` (saldo livre para débito). Toda movimentação de saldo é registrada como uma `Transaction` (CREDIT ou DEBIT), criada com idempotência garantida por uma `idempotencyKey` única. O crédito/débito é aplicado de forma síncrona já no momento da criação, finalizando a transação como `COMPLETED` ou `FAILED`; um job de reconciliação assíncrono existe apenas como fallback para reprocessar transações que ficaram pendentes por falha inesperada no processamento síncrono.

---

## Decisões

- **Código:** Aplicação de Clean Architecture com DDD em uma estrutura module-first (`partner/`, `transaction/`, `shared/`). Cada módulo tem suas próprias camadas de domain, application e infrastructure. O domínio (regras de negócio, entidades, eventos) não tem dependências externas.


- **Modelo de saldo:** mais simples que um modelo de hold-and-capture — não há reserva de crédito. O saldo de um parceiro é gerenciado por dois campos: `total_balance` (soma histórica de créditos) e `available_balance` (saldo livre para débito). Cada tipo de transação dispara uma operação diferente:

  | Tipo de Transação | Operação |
  |---|---|
  | CREDIT | `total_balance += amount` e `available_balance += amount` |
  | DEBIT | `available_balance -= amount` (somente se `available_balance >= amount`) |


- **Concorrência sem locks explícitos:** assim como em qualquer cenário de alto volume de débitos simultâneos para o mesmo parceiro, ler o saldo, checar disponibilidade e depois atualizar cria uma race condition clássica. A abordagem adotada foi o **UPDATE condicional atômico** direto no PostgreSQL, em `PartnerBalanceJpaRepository`:

  ```sql
  UPDATE partner_balance
  SET available_balance = available_balance - :amount,
      updated_at = now()
  WHERE partner_id = :partnerId
    AND available_balance >= :amount
  ```

  Um único statement SQL é atômico por definição — o PostgreSQL garante isso internamente. Se o saldo for insuficiente, a query retorna 0 linhas afetadas e o handler marca a transação como `FAILED`, sem produzir overdraft. O CREDIT, por não depender de saldo disponível, sempre é aplicado sem condição.

  Para validar, foi implementado `DefaultPartnerBalanceRepositoryConcurrencyIT` com 100 threads simultâneas tentando debitar saldo do mesmo parceiro, e nenhuma produziu saldo negativo.


- **Idempotência:** toda transação exige uma `idempotencyKey` única (`UNIQUE` no banco). Uma segunda tentativa com a mesma chave é rejeitada com `DuplicateTransactionException` (409) antes de qualquer movimentação de saldo — protege contra reenvio de requisições (timeout, retry de cliente, etc).


- **Processamento síncrono na criação, com reconciliação como fallback:** toda `Transaction` nasce em memória como `PENDING`, mas o `DefaultCreateTransactionUseCase` tenta processá-la imediatamente, ainda na mesma chamada, delegando para o `TransactionHandler` correspondente ao tipo (`CreditTransactionHandler` ou `DebitTransactionHandler` — strategy pattern), que aplica o UPDATE atômico de saldo. Na grande maioria dos casos a transação já é persistida com status final — `COMPLETED` em caso de sucesso, ou `FAILED` em caso de falha de negócio (ex.: saldo insuficiente). Ela só permanece `PENDING` após a criação se uma exceção inesperada (não mapeada como erro de negócio) interromper esse processamento síncrono. O `ReconcileTransactionsUseCase`, executado por `@Scheduled` a cada 60 segundos (`ReconciliationScheduler`), busca essas transações órfãs em `PENDING` e tenta reprocessá-las — funcionando como retry/fallback, não como o fluxo principal. Em ambos os casos (síncrono ou via reconciliação), ao sair de `PENDING` é publicado o evento `TransactionStatusChanged` no RabbitMQ.


- **Value Objects:** `Money` encapsula `BigDecimal`, garantindo escala fixa de 2 casas decimais e operações aritméticas seguras. `PartnerId` e `TransactionId` são `value class` (`@JvmInline`) — sem overhead em runtime, mas com segurança de tipos: impossível passar um `PartnerId` onde um `TransactionId` é esperado.

---

## Tech Stack

| Camada | Tecnologia |
|---|---|
| Linguagem | Kotlin 2.2.21 |
| Framework | Spring Boot 4.0.7 |
| Banco de Dados | PostgreSQL |
| ORM | Spring Data JPA + Hibernate |
| Migrations | Flyway |
| Mensageria | RabbitMQ + Spring AMQP |
| Testes | JUnit 5, Mockito-Kotlin, Testcontainers |
| Cobertura | JaCoCo |
| Documentação | SpringDoc OpenAPI (Swagger) |
| Containerização | Docker, Docker Compose |

---

## Arquitetura & Camadas

Estrutura module-first com Clean Architecture. O código é agrupado por módulo de negócio, e dentro de cada módulo por camada:

```
src/main/kotlin/.../
├── partner/
│   ├── domain/          ← Agregados Partner e PartnerBalance, ports, exceções
│   ├── application/     ← Casos de uso (Create, FindBalance, List)
│   └── infrastructure/  ← Controller REST, JPA entities, repositórios, mappers
├── transaction/
│   ├── domain/          ← Agregado Transaction, ports, eventos, exceções
│   ├── application/     ← Casos de uso (Create, Reconcile, ListHistory), handlers de CREDIT/DEBIT
│   └── infrastructure/  ← Controller REST, JPA entity, repositório, scheduler de reconciliação
└── shared/
    ├── core/            ← Money, Identifier, Entity, PageCustom, DomainEvent, exceções base
    └── infrastructure/  ← GlobalExceptionHandler, SwaggerConfig, RabbitMQConfig/Publisher
```

- **Domain:** Agregados (`Partner`, `PartnerBalance`, `Transaction`), value objects (`Money`, `PartnerId`, `TransactionId`), ports (interfaces de repositório), eventos de domínio (`TransactionStatusChanged`), exceções de domínio. Sem dependências externas.


- **Application:** Casos de uso que orquestram domínio e ports. Cada operação tem sua própria classe: `DefaultCreatePartnerUseCase`, `DefaultFindPartnerBalanceUseCase`, `DefaultListPartnersUseCase`, `DefaultCreateTransactionUseCase`, `DefaultReconcileTransactionsUseCase`, `DefaultListTransactionHistoryUseCase`. Os handlers `CreditTransactionHandler` e `DebitTransactionHandler` implementam a interface `TransactionHandler`, aplicando a strategy correta por tipo.


- **Infrastructure:** Controllers REST com validação Bean Validation, entidades JPA, repositórios Spring Data com queries nativas para as operações atômicas de saldo, publicador RabbitMQ, scheduler de reconciliação (`@Scheduled`, configurável via `ConditionalOnProperty`).

---

## Endpoints

Documentação interativa disponível em `http://localhost:8080/swagger-ui/index.html`.

### Partners

**`POST /api/v1/b2b/partners`** — Cadastra um parceiro

```json
{ "name": "Empresa X", "document": "12.345.678/0001-99", "availableBalance": 50000.00 }
```

Cria o parceiro e inicializa o saldo com `total_balance = availableBalance` e `available_balance = availableBalance`. Retorna 201 com o UUID gerado. Falha com 400 se os campos forem inválidos ou 409 se o documento já existir.

---

**`GET /api/v1/b2b/partners/{partnerId}/balance`** — Consulta de saldo

```json
{
  "partnerId": "uuid-do-parceiro",
  "totalBalance": 50000.00,
  "availableBalance": 35000.00,
  "updatedAt": "2024-01-15T10:30:00Z"
}
```

Falha com 404 se o parceiro não existir.

---

**`GET /api/v1/b2b/partners`** — Lista parceiros

Query params: `document` (filtro opcional), `pageNumber` (default 0), `pageSize` (default 20).

---

### Transactions

**`POST /api/v1/b2b/partners/{partnerId}/transactions/credit`** — Credita saldo

```json
{ "amount": 1000.00, "description": "Reposição de saldo", "idempotencyKey": "chave-unica-001" }
```

A ordem das operações importa:

1. Valida os campos do request e a existência do parceiro
2. Valida a unicidade da `idempotencyKey` — se já existir, lança `DuplicateTransactionException` (409)
3. Tenta processar a transação imediatamente, na mesma requisição: o `CreditTransactionHandler` aplica `total_balance += amount` e `available_balance += amount` via UPDATE atômico, definindo o status final já nesta chamada
4. Persiste a `Transaction` com o status resultante (`COMPLETED` em caso de sucesso; `FAILED` em caso de falha de negócio)
5. Se o status não for `PENDING`, publica `TransactionStatusChanged`
6. Retorna 201 com `transactionId` e o status. Normalmente já vem `COMPLETED`/`FAILED`; `PENDING` só ocorre no caso raro de uma falha inesperada interromper o processamento síncrono, ficando a cargo da reconciliação assíncrona resolver depois

---

**`POST /api/v1/b2b/partners/{partnerId}/transactions/debit`** — Debita saldo

```json
{ "amount": 500.00, "description": "Pagamento de fatura", "idempotencyKey": "chave-unica-002" }
```

Mesmo fluxo do crédito, mas o `DebitTransactionHandler` executa o UPDATE atômico condicional (`available_balance >= amount`) já no momento da criação. Se o saldo for insuficiente, a transação é marcada `FAILED` com `errorDescription` preenchido na própria resposta da requisição, em vez de ficar pendente aguardando a reconciliação.

---

**`GET /api/v1/b2b/partners/{partnerId}/transactions`** — Histórico de transações

Query params: `from` e `to` (datas), `type` (`CREDIT` ou `DEBIT`), `pageNumber` (default 0), `pageSize` (default 20). Todos os filtros são opcionais e combinam entre si.

```json
{
  "transactionId": "uuid-da-transacao",
  "type": "DEBIT",
  "amount": 500.00,
  "description": "Pagamento de fatura",
  "status": "COMPLETED",
  "createdAt": "2024-01-15T10:30:00Z"
}
```

---

## Estratégia de Testes

- **Unitários de domínio:** `MoneyTest`, `PartnerTest`, `PartnerIdTest`, `TransactionTest`, `TransactionIdTest`, `TransactionExceptionsTest`.


- **Unitários de caso de uso:** `DefaultCreatePartnerUseCaseTest`, `DefaultFindPartnerBalanceUseCaseTest`, `DefaultListPartnersUseCaseTest`, `DefaultCreateTransactionUseCaseTest`, `DefaultReconcileTransactionsUseCaseTest`, `DefaultListTransactionHistoryUseCaseTest`. Cobrem validação de idempotência, falha por saldo insuficiente, e publicação de eventos no fluxo de reconciliação.


- **Unitários de handler/validator:** `CreditTransactionHandlerTest`, `DebitTransactionHandlerTest`, `CreateTransactionValidatorTest`, `ListTransactionHistoryValidatorTest`.


- **Integração com Testcontainers:** `PartnerControllerIT`, `TransactionControllerIT`, `DefaultPartnerRepositoryIT`, `DefaultPartnerBalanceRepositoryIT`, `DefaultTransactionRepositoryIT`. PostgreSQL e RabbitMQ reais, Flyway aplicado automaticamente, sem mocks de infraestrutura.


- **Concorrência:** `DefaultPartnerBalanceRepositoryConcurrencyIT` dispara 100 threads simultâneas tentando debitar saldo do mesmo parceiro. Valida que o saldo nunca fica negativo e que nenhum CHECK constraint do banco é violado.


```bash
./gradlew test      # unitários + integração
./gradlew jacocoTestCoverageVerification   # quality gate de cobertura
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
  | `b2b.credits.events` | `transaction.status.changed` | `queue.transaction.status.changed` | `TransactionStatusChanged` |

  Os eventos são publicados quando uma transação sai do estado `PENDING`, com o resultado da reconciliação (`COMPLETED` ou `FAILED`).

---

## Possíveis Melhorias

- **Rate Limiting:** para evitar abuso e picos inesperados de criação de transações.

- **Redis:** para cache de consultas frequentes de saldo e parceiros.

- **Dead Letter Queue:** mensagens que falham no consumo de `TransactionStatusChanged` precisam de um destino explícito para tratamento posterior.

- **Reconciliação particionada:** o job atual processa todas as transações `PENDING` em uma única execução; sob alto volume, dividir em lotes ou paralelizar por parceiro reduziria a latência de confirmação.

- **Lock distribuído na reconciliação:** o `ReconciliationScheduler` não tem nenhuma proteção contra execução concorrente entre instâncias. Se a aplicação escalar horizontalmente, cada instância dispara o `@Scheduled` no seu próprio intervalo, e todas processam as mesmas transações `PENDING` em paralelo — risco de reprocessamento duplicado. Soluções possíveis: ShedLock, lock advisory do PostgreSQL (`pg_try_advisory_lock`), quartz etc.

- **Publicação de eventos pós-commit:** publicar somente após a confirmação da transação de banco, evitando eventos inconsistentes em caso de rollback.

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

O Flyway aplica automaticamente o script `V2__seed_partners.sql` na inicialização. Dois parceiros já estão cadastrados e prontos para uso:

| Nome | ID | Documento (CNPJ) | Saldo Total | Saldo Disponível |
|---|---|---|---|---|
| Empresa A | `1f2b3c4d-5e6f-4a7b-8c9d-0e1f2a3b4c5d` | 11222333000181 | R$ 10.000,00 | R$ 10.000,00 |
| Empresa B | `2a3b4c5d-6e7f-4b8c-9d0e-1f2a3b4c5d6e` | 44555666000162 | R$ 50.000,00 | R$ 50.000,00 |

package br.com.vps.consulting.b2b.management.transaction.infrastructure.mapper

import br.com.vps.consulting.b2b.management.shared.core.vo.Money
import br.com.vps.consulting.b2b.management.transaction.domain.Transaction
import br.com.vps.consulting.b2b.management.transaction.domain.TransactionId
import br.com.vps.consulting.b2b.management.transaction.infrastructure.persistence.TransactionEntity

fun TransactionEntity.toDomain(): Transaction = Transaction.with(
    id = TransactionId.from(id),
    partnerId = partnerId,
    type = type,
    amount = Money.of(amount),
    description = description,
    idempotencyKey = idempotencyKey,
    status = status,
    errorDescription = errorDescription,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun Transaction.toEntity(): TransactionEntity = TransactionEntity(
    id = id.value,
    partnerId = partnerId,
    type = type,
    amount = amount.value,
    description = description,
    idempotencyKey = idempotencyKey,
    status = status,
    errorDescription = errorDescription,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

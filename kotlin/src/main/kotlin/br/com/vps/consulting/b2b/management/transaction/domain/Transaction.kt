package br.com.vps.consulting.b2b.management.transaction.domain

import br.com.vps.consulting.b2b.management.shared.core.entity.Entity
import br.com.vps.consulting.b2b.management.shared.core.vo.Money
import java.time.Instant
import java.util.*

class Transaction private constructor(
    id: TransactionId,
    val partnerId: UUID,
    val type: TransactionType,
    val amount: Money,
    val description: String,
    val idempotencyKey: String,
    var status: TransactionStatus,
    var errorDescription: String?,
    val createdAt: Instant,
    var updatedAt: Instant,
) : Entity<TransactionId>(id) {

    init {
        validate()
    }

    fun complete() {
        status = TransactionStatus.COMPLETED
        errorDescription = null
        updatedAt = Instant.now()
    }

    fun fail(errorDescription: String? = null) {
        status = TransactionStatus.FAILED
        this.errorDescription = errorDescription
        updatedAt = Instant.now()
    }

    fun isPending() = status == TransactionStatus.PENDING

    override fun validate() {
        require(!amount.isNegativeOrZero()) { "O valor deve ser positivo" }
        require(description.isNotBlank()) { "A descrição não pode estar em branco" }
        require(idempotencyKey.isNotBlank()) { "A chave de idempotência não pode estar em branco" }
    }

    companion object {
        fun createAsPending(
            partnerId: UUID,
            type: TransactionType,
            amount: Money,
            description: String,
            idempotencyKey: String,
        ): Transaction = Transaction(
            id = TransactionId.generate(),
            partnerId = partnerId,
            type = type,
            amount = amount,
            description = description,
            idempotencyKey = idempotencyKey,
            status = TransactionStatus.PENDING,
            errorDescription = null,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
        )

        fun with(
            id: TransactionId,
            partnerId: UUID,
            type: TransactionType,
            amount: Money,
            description: String,
            idempotencyKey: String,
            status: TransactionStatus,
            errorDescription: String?,
            createdAt: Instant,
            updatedAt: Instant,
        ): Transaction = Transaction(
            id, partnerId, type, amount, description, idempotencyKey, status, errorDescription, createdAt, updatedAt,
        )
    }
}

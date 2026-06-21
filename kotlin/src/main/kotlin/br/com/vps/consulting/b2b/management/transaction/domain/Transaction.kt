package br.com.vps.consulting.b2b.management.transaction.domain

import br.com.vps.consulting.b2b.management.partner.domain.PartnerId
import br.com.vps.consulting.b2b.management.shared.core.entity.Entity
import br.com.vps.consulting.b2b.management.shared.core.vo.Money
import java.time.Instant

class Transaction private constructor(
    id: TransactionId,
    val partnerId: PartnerId,
    val type: TransactionType,
    val amount: Money,
    val description: String,
    val idempotencyKey: String,
    var status: TransactionStatus,
    val createdAt: Instant,
    var updatedAt: Instant,
) : Entity<TransactionId>(id) {

    init {
        validate()
    }

    fun complete() {
        status = TransactionStatus.COMPLETED
        updatedAt = Instant.now()
    }

    fun fail() {
        status = TransactionStatus.FAILED
        updatedAt = Instant.now()
    }

    override fun validate() {
        require(!amount.isNegativeOrZero()) { "O valor deve ser positivo" }
        require(description.isNotBlank()) { "A descrição não pode estar em branco" }
        require(idempotencyKey.isNotBlank()) { "A chave de idempotência não pode estar em branco" }
    }

    companion object {
        fun create(
            partnerId: PartnerId,
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
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
        )

        fun with(
            id: TransactionId,
            partnerId: PartnerId,
            type: TransactionType,
            amount: Money,
            description: String,
            idempotencyKey: String,
            status: TransactionStatus,
            createdAt: Instant,
            updatedAt: Instant,
        ): Transaction = Transaction(
            id, partnerId, type, amount, description, idempotencyKey, status, createdAt, updatedAt,
        )
    }
}

package br.com.vps.consulting.b2b.management.transaction.infrastructure.persistence

import br.com.vps.consulting.b2b.management.transaction.domain.TransactionStatus
import br.com.vps.consulting.b2b.management.transaction.domain.TransactionType
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant
import java.util.*

@Entity
@Table(name = "transaction")
class TransactionEntity(
    @Id
    val id: UUID,

    @Column(name = "partner_id", nullable = false)
    val partnerId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: TransactionType,

    @Column(nullable = false)
    val amount: BigDecimal,

    @Column(nullable = false)
    val description: String,

    @Column(name = "idempotency_key", nullable = false, unique = true)
    val idempotencyKey: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: TransactionStatus,

    @Column(name = "error_description")
    val errorDescription: String?,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant,

    @Column(name = "updated_at", nullable = false)
    val updatedAt: Instant

)

package br.com.vps.consulting.b2b.management.partner.infrastructure.persistence

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant
import java.util.*

@Entity
@Table(name = "partner_balance")
class PartnerBalanceEntity(
    @Id
    @Column(name = "partner_id")
    val partnerId: UUID,

    @Column(name = "total_balance", nullable = false)
    val totalBalance: BigDecimal,

    @Column(name = "available_balance", nullable = false)
    val availableBalance: BigDecimal,

    @Column(name = "updated_at", nullable = false)
    val updatedAt: Instant,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id", insertable = false, updatable = false)
    val partner: PartnerEntity? = null,
)

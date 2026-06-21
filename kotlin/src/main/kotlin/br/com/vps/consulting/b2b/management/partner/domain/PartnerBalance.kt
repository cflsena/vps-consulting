package br.com.vps.consulting.b2b.management.partner.domain

import br.com.vps.consulting.b2b.management.shared.core.entity.Entity
import br.com.vps.consulting.b2b.management.shared.core.vo.Money
import java.math.BigDecimal
import java.time.Instant

class PartnerBalance private constructor(
    id: PartnerId,
    val totalBalance: Money,
    val availableBalance: Money,
    val updatedAt: Instant
) : Entity<PartnerId>(id) {

    init {
        validate()
    }

    override fun validate() {
    }

    companion object {
        fun with(
            id: PartnerId,
            totalBalance: BigDecimal = BigDecimal.ZERO,
            availableBalance: BigDecimal,
            updatedAt: Instant = Instant.now()
        ) = PartnerBalance(
            id = id,
            totalBalance = Money.of(totalBalance),
            availableBalance = Money.of(availableBalance),
            updatedAt = updatedAt
        )
    }

}

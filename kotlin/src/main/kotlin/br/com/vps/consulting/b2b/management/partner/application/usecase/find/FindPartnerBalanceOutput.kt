package br.com.vps.consulting.b2b.management.partner.application.usecase.find

import br.com.vps.consulting.b2b.management.partner.domain.PartnerBalance
import java.math.BigDecimal
import java.time.Instant
import java.util.*

data class FindPartnerBalanceOutput(
    val partnerId: UUID,
    val totalBalance: BigDecimal,
    val availableBalance: BigDecimal,
    val updatedAt: Instant,
) {
    companion object {
        fun from(partnerBalance: PartnerBalance) = FindPartnerBalanceOutput(
            partnerId = partnerBalance.id.value,
            totalBalance = partnerBalance.totalBalance.value,
            availableBalance = partnerBalance.availableBalance.value,
            updatedAt = partnerBalance.updatedAt
        )
    }
}

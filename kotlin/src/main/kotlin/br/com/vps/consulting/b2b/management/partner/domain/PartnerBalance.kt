package br.com.vps.consulting.b2b.management.partner.domain

import br.com.vps.consulting.b2b.management.shared.core.vo.Money
import java.time.Instant

data class PartnerBalance(
    val partnerId: PartnerId,
    val totalBalance: Money,
    val availableBalance: Money,
    val updatedAt: Instant,
)

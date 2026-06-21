package br.com.vps.consulting.b2b.management.partner.application.usecase.find

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class FindPartnerBalanceOutput(
    val partnerId: UUID,
    val totalBalance: BigDecimal,
    val availableBalance: BigDecimal,
    val updatedAt: Instant,
)

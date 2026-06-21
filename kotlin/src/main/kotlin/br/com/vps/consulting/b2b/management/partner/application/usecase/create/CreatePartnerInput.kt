package br.com.vps.consulting.b2b.management.partner.application.usecase.create

import java.math.BigDecimal

data class CreatePartnerInput(
    val name: String,
    val document: String,
    val availableBalance: BigDecimal
)

package br.com.vps.consulting.b2b.management.partner.infrastructure.api.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import java.math.BigDecimal

data class CreatePartnerRequest(
    @field:Schema(description = "Nome do parceiro")
    @field:NotBlank(message = "O nome não pode estar em branco")
    val name: String,

    @field:Schema(description = "Documento do parceiro (CPF ou CNPJ)")
    @field:NotBlank(message = "O documento não pode estar em branco")
    val document: String,

    @field:Schema(description = "Saldo disponível inicial do parceiro", example = "100.00")
    val availableBalance: BigDecimal
)

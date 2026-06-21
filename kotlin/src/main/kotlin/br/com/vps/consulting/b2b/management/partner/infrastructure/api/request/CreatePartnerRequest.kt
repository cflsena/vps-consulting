package br.com.vps.consulting.b2b.management.partner.infrastructure.api.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

data class CreatePartnerRequest(
    @field:Schema(description = "Nome do parceiro")
    @field:NotBlank(message = "O nome não pode estar em branco")
    val name: String,

    @field:Schema(description = "Documento do parceiro (CPF ou CNPJ)")
    @field:NotBlank(message = "O documento não pode estar em branco")
    val document: String,
)

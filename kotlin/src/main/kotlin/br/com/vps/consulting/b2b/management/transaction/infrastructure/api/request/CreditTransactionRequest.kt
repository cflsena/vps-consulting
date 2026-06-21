package br.com.vps.consulting.b2b.management.transaction.infrastructure.api.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.math.BigDecimal

data class CreditTransactionRequest(
    @field:Schema(description = "Valor a ser creditado", example = "100.00")
    @field:NotNull(message = "O valor não pode ser nulo")
    @field:Positive(message = "O valor deve ser positivo")
    val amount: BigDecimal,

    @field:Schema(description = "Motivo/descrição da transação")
    @field:NotBlank(message = "A descrição não pode estar em branco")
    val description: String,

    @field:Schema(description = "Chave de idempotência da transação")
    @field:NotBlank(message = "A chave de idempotência não pode estar em branco")
    val idempotencyKey: String,
)

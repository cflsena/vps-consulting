package br.com.vps.consulting.b2b.management.partner.infrastructure.api.response

import br.com.vps.consulting.b2b.management.partner.application.usecase.find.FindPartnerBalanceOutput
import br.com.vps.consulting.b2b.management.shared.core.extension.toBrazilianOffsetDateTime
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.*

data class FindPartnerBalanceResponse(
    @field:Schema(description = "ID único do parceiro")
    val partnerId: UUID,

    @field:Schema(description = "Total creditado historicamente para o parceiro", example = "1000.00")
    val totalCredited: BigDecimal,

    @field:Schema(description = "Total debitado historicamente do parceiro", example = "250.00")
    val totalDebited: BigDecimal,

    @field:Schema(description = "Saldo disponível para operações do parceiro", example = "750.50")
    val availableBalance: BigDecimal,

    @field:Schema(description = "Data e hora da última atualização do saldo")
    val updatedAt: OffsetDateTime,
) {
    companion object {
        fun from(output: FindPartnerBalanceOutput) = FindPartnerBalanceResponse(
            partnerId = output.partnerId,
            totalCredited = output.totalCredited,
            totalDebited = output.totalDebited,
            availableBalance = output.availableBalance,
            updatedAt = output.updatedAt.toBrazilianOffsetDateTime()
        )
    }
}

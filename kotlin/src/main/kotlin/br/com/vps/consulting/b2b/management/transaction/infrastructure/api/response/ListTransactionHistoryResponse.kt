package br.com.vps.consulting.b2b.management.transaction.infrastructure.api.response

import br.com.vps.consulting.b2b.management.shared.core.extension.toBrazilianOffsetDateTime
import br.com.vps.consulting.b2b.management.shared.core.page.PageCustom
import br.com.vps.consulting.b2b.management.shared.infrastructure.api.pagination.PageResponseDTO
import br.com.vps.consulting.b2b.management.transaction.application.usecase.history.ListTransactionHistoryOutput
import br.com.vps.consulting.b2b.management.transaction.domain.TransactionStatus
import br.com.vps.consulting.b2b.management.transaction.domain.TransactionType
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.*

data class ListTransactionHistoryResponse(
    @field:Schema(description = "ID único da transação")
    val transactionId: UUID,

    @field:Schema(description = "Tipo da transação (CREDIT ou DEBIT)")
    val type: TransactionType,

    @field:Schema(description = "Valor da transação", example = "100.00")
    val amount: BigDecimal,

    @field:Schema(description = "Descrição/motivo da transação")
    val description: String,

    @field:Schema(description = "Status final da transação (PENDING, COMPLETED ou FAILED)")
    val status: TransactionStatus,

    @field:Schema(description = "Data e hora de criação da transação")
    val createdAt: OffsetDateTime,
) {
    companion object {
        fun from(page: PageCustom<ListTransactionHistoryOutput>): PageResponseDTO<ListTransactionHistoryResponse> =
            PageResponseDTO(
                pageNumber = page.pageNumber,
                pageSize = page.pageSize,
                totalPages = page.totalPages,
                totalElements = page.totalElements,
                items = page.items.map { from(it) },
                numberOfElements = page.items.size,
            )

        fun from(transaction: ListTransactionHistoryOutput): ListTransactionHistoryResponse =
            ListTransactionHistoryResponse(
                transactionId = transaction.id,
                type = transaction.type,
                amount = transaction.amount,
                description = transaction.description,
                status = transaction.status,
                createdAt = transaction.createdAt.toBrazilianOffsetDateTime(),
            )
    }
}

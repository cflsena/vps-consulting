package br.com.vps.consulting.b2b.management.transaction.application.usecase.history

import br.com.vps.consulting.b2b.management.shared.core.page.PageCustom
import br.com.vps.consulting.b2b.management.transaction.domain.Transaction
import br.com.vps.consulting.b2b.management.transaction.domain.TransactionStatus
import br.com.vps.consulting.b2b.management.transaction.domain.TransactionType
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class ListTransactionHistoryOutput(
    val id: UUID,
    val type: TransactionType,
    val amount: BigDecimal,
    val description: String,
    val status: TransactionStatus,
    val createdAt: Instant,
) {
    companion object {
        fun from(page: PageCustom<Transaction>): PageCustom<ListTransactionHistoryOutput> = PageCustom(
            pageNumber = page.pageNumber,
            pageSize = page.pageSize,
            totalPages = page.totalPages,
            totalElements = page.totalElements,
            items = page.items.map { from(it) },
        )

        fun from(transaction: Transaction): ListTransactionHistoryOutput = ListTransactionHistoryOutput(
            id = transaction.id.value,
            type = transaction.type,
            amount = transaction.amount.value,
            description = transaction.description,
            status = transaction.status,
            createdAt = transaction.createdAt,
        )
    }
}

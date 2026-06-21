package br.com.vps.consulting.b2b.management.transaction.application.usecase.history

import br.com.vps.consulting.b2b.management.transaction.domain.TransactionType
import java.time.Instant
import java.util.UUID

data class ListTransactionHistoryInput(
    val partnerId: UUID,
    val from: Instant?,
    val to: Instant?,
    val type: TransactionType?,
    val pageSize: Int,
    val pageNumber: Int,
)

package br.com.vps.consulting.b2b.management.transaction.application.usecase.create

import br.com.vps.consulting.b2b.management.transaction.domain.TransactionStatus
import java.util.UUID

data class CreateTransactionOutput(
    val transactionId: UUID,
    val status: TransactionStatus,
    val errorDescription: String?,
)

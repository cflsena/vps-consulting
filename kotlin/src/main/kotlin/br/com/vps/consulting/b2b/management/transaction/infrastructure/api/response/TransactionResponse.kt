package br.com.vps.consulting.b2b.management.transaction.infrastructure.api.response

import br.com.vps.consulting.b2b.management.transaction.application.usecase.create.CreateTransactionOutput
import br.com.vps.consulting.b2b.management.transaction.domain.TransactionStatus
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

data class TransactionResponse(
    @field:Schema(description = "ID único da transação")
    val transactionId: UUID,

    @field:Schema(description = "Status final da transação")
    val status: TransactionStatus,

    @field:Schema(description = "Motivo da falha, preenchido somente quando o status é FAILED")
    val errorDescription: String?,
) {
    companion object {
        fun from(output: CreateTransactionOutput): TransactionResponse = TransactionResponse(
            transactionId = output.transactionId,
            status = output.status,
            errorDescription = output.errorDescription,
        )
    }
}

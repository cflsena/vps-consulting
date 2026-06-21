package br.com.vps.consulting.b2b.management.transaction.infrastructure.api

import br.com.vps.consulting.b2b.management.shared.core.extension.toBrazilianInstant
import br.com.vps.consulting.b2b.management.shared.infrastructure.api.pagination.PageResponseDTO
import br.com.vps.consulting.b2b.management.transaction.application.usecase.create.CreateTransactionInput
import br.com.vps.consulting.b2b.management.transaction.application.usecase.create.CreateTransactionUseCase
import br.com.vps.consulting.b2b.management.transaction.application.usecase.history.ListTransactionHistoryInput
import br.com.vps.consulting.b2b.management.transaction.application.usecase.history.ListTransactionHistoryUseCase
import br.com.vps.consulting.b2b.management.transaction.domain.TransactionType
import br.com.vps.consulting.b2b.management.transaction.infrastructure.api.request.CreditTransactionRequest
import br.com.vps.consulting.b2b.management.transaction.infrastructure.api.request.DebitTransactionRequest
import br.com.vps.consulting.b2b.management.transaction.infrastructure.api.response.ListTransactionHistoryResponse
import br.com.vps.consulting.b2b.management.transaction.infrastructure.api.response.TransactionResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

@RestController
class TransactionController(
    private val createTransactionUseCase: CreateTransactionUseCase,
    private val listTransactionHistoryUseCase: ListTransactionHistoryUseCase,
) : TransactionApi {

    override fun credit(partnerId: UUID, request: CreditTransactionRequest): ResponseEntity<TransactionResponse> {
        val output = createTransactionUseCase.execute(
            CreateTransactionInput(
                partnerId = partnerId,
                amount = request.amount,
                description = request.description,
                idempotencyKey = request.idempotencyKey,
                type = TransactionType.CREDIT,
            )
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(TransactionResponse.from(output))
    }

    override fun debit(partnerId: UUID, request: DebitTransactionRequest): ResponseEntity<TransactionResponse> {
        val output = createTransactionUseCase.execute(
            CreateTransactionInput(
                partnerId = partnerId,
                amount = request.amount,
                description = request.description,
                idempotencyKey = request.idempotencyKey,
                type = TransactionType.DEBIT,
            )
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(TransactionResponse.from(output))
    }

    override fun history(
        partnerId: UUID,
        from: LocalDate?,
        to: LocalDate?,
        type: TransactionType?,
        pageSize: Int,
        pageNumber: Int,
    ): ResponseEntity<PageResponseDTO<ListTransactionHistoryResponse>> {
        val page = listTransactionHistoryUseCase.execute(
            ListTransactionHistoryInput(
                partnerId = partnerId,
                from = from?.atStartOfDay()?.toBrazilianInstant(),
                to = to?.atTime(LocalTime.MAX)?.toBrazilianInstant(),
                type = type,
                pageSize = pageSize,
                pageNumber = pageNumber,
            )
        )
        return ResponseEntity.ok(ListTransactionHistoryResponse.from(page))
    }
}

package br.com.vps.consulting.b2b.management.transaction.infrastructure.api

import br.com.vps.consulting.b2b.management.shared.infrastructure.api.pagination.PageResponseDTO
import br.com.vps.consulting.b2b.management.transaction.domain.TransactionType
import br.com.vps.consulting.b2b.management.transaction.infrastructure.api.request.CreditTransactionRequest
import br.com.vps.consulting.b2b.management.transaction.infrastructure.api.request.DebitTransactionRequest
import br.com.vps.consulting.b2b.management.transaction.infrastructure.api.response.ListTransactionHistoryResponse
import br.com.vps.consulting.b2b.management.transaction.infrastructure.api.response.TransactionResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.util.*

@RequestMapping("/api/v1/b2b/partners/{partnerId}/transactions")
@Tag(name = "Transações", description = "Operações de crédito, débito e histórico de transações dos parceiros")
interface TransactionApi {

    @Operation(
        summary = "Credita saldo para um parceiro",
        description = "Registra uma transação de crédito e atualiza o saldo total e disponível do parceiro. " +
            "Em caso de falha de negócio, a resposta é 201 com status FAILED e errorDescription preenchida.",
    )
    @ApiResponse(responseCode = "201", description = "Transação processada (status COMPLETED ou FAILED no corpo)")
    @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos")
    @ApiResponse(responseCode = "404", description = "Parceiro não encontrado")
    @ApiResponse(responseCode = "409", description = "Transação já processada para a chave de idempotência informada")
    @PostMapping("/credit", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun credit(
        @Parameter(description = "ID único do parceiro") @PathVariable partnerId: UUID,
        @RequestBody @Valid request: CreditTransactionRequest,
    ): ResponseEntity<TransactionResponse>

    @Operation(
        summary = "Debita saldo de um parceiro",
        description = "Registra uma transação de débito, validando o saldo disponível antes de confirmar. " +
            "Em caso de saldo insuficiente, a resposta é 201 com status FAILED e errorDescription preenchida.",
    )
    @ApiResponse(responseCode = "201", description = "Transação processada (status COMPLETED ou FAILED no corpo)")
    @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos")
    @ApiResponse(responseCode = "404", description = "Parceiro não encontrado")
    @ApiResponse(responseCode = "409", description = "Transação já processada para a chave de idempotência informada")
    @PostMapping("/debit", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun debit(
        @Parameter(description = "ID único do parceiro") @PathVariable partnerId: UUID,
        @RequestBody @Valid request: DebitTransactionRequest,
    ): ResponseEntity<TransactionResponse>

    @Operation(
        summary = "Consulta o histórico de transações do parceiro",
        description = "Retorna uma lista paginada de transações do parceiro, com filtros opcionais por período e tipo",
    )
    @ApiResponse(responseCode = "200", description = "Histórico consultado com sucesso")
    @ApiResponse(responseCode = "404", description = "Parceiro não encontrado")
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun history(
        @Parameter(description = "ID único do parceiro") @PathVariable partnerId: UUID,
        @Parameter(description = "Data inicial do filtro (yyyy-MM-dd)")
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) from: LocalDate?,
        @Parameter(description = "Data final do filtro (yyyy-MM-dd)")
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) to: LocalDate?,
        @Parameter(description = "Filtro por tipo de transação") @RequestParam(required = false) type: TransactionType?,
        @Parameter(description = "Quantidade de registros por página") @RequestParam(defaultValue = "20") pageSize: Int,
        @Parameter(description = "Número da página (base 0)") @RequestParam(defaultValue = "0") pageNumber: Int,
    ): ResponseEntity<PageResponseDTO<ListTransactionHistoryResponse>>
}

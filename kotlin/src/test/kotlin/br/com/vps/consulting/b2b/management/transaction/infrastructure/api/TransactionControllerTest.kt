package br.com.vps.consulting.b2b.management.transaction.infrastructure.api

import br.com.vps.consulting.b2b.management.shared.core.page.PageCustom
import br.com.vps.consulting.b2b.management.shared.core.vo.Money
import br.com.vps.consulting.b2b.management.shared.infrastructure.exception.GlobalExceptionHandler
import br.com.vps.consulting.b2b.management.transaction.application.usecase.create.CreateTransactionOutput
import br.com.vps.consulting.b2b.management.transaction.application.usecase.create.CreateTransactionUseCase
import br.com.vps.consulting.b2b.management.transaction.application.usecase.history.ListTransactionHistoryOutput
import br.com.vps.consulting.b2b.management.transaction.application.usecase.history.ListTransactionHistoryUseCase
import br.com.vps.consulting.b2b.management.transaction.domain.TransactionStatus
import br.com.vps.consulting.b2b.management.transaction.domain.TransactionType
import br.com.vps.consulting.b2b.management.transaction.domain.exception.DuplicateTransactionException
import br.com.vps.consulting.b2b.management.transaction.domain.exception.InsufficientBalanceException
import br.com.vps.consulting.b2b.management.transaction.domain.exception.TransactionPartnerNotFoundException
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@WebMvcTest(TransactionController::class)
@Import(GlobalExceptionHandler::class)
class TransactionControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var createTransactionUseCase: CreateTransactionUseCase

    @MockitoBean
    lateinit var listTransactionHistoryUseCase: ListTransactionHistoryUseCase

    private val partnerId = UUID.randomUUID()

    private fun creditRequestBody() = """{"amount":100.00,"description":"Compra de créditos","idempotencyKey":"key-123"}"""

    @Test
    fun `should return 201 with COMPLETED status on credit`() {
        val transactionId = UUID.randomUUID()
        whenever(createTransactionUseCase.execute(any()))
            .thenReturn(CreateTransactionOutput(transactionId, TransactionStatus.COMPLETED, null))

        mockMvc.perform(
            post("/api/v1/b2b/partners/$partnerId/transactions/credit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(creditRequestBody())
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.status").value("COMPLETED"))
    }

    @Test
    fun `should return 201 with FAILED status and errorDescription when business failure occurs`() {
        val transactionId = UUID.randomUUID()
        whenever(createTransactionUseCase.execute(any()))
            .thenReturn(CreateTransactionOutput(transactionId, TransactionStatus.FAILED, "Saldo insuficiente"))

        mockMvc.perform(
            post("/api/v1/b2b/partners/$partnerId/transactions/debit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(creditRequestBody())
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.status").value("FAILED"))
            .andExpect(jsonPath("$.errorDescription").value("Saldo insuficiente"))
    }

    @Test
    fun `should return 400 when amount is not positive`() {
        mockMvc.perform(
            post("/api/v1/b2b/partners/$partnerId/transactions/credit")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"amount":0,"description":"Compra","idempotencyKey":"key-123"}""")
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `should return 422 when use case throws a DomainException`() {
        whenever(createTransactionUseCase.execute(any()))
            .thenThrow(InsufficientBalanceException(partnerId, Money.of(BigDecimal("100.00")), Money.of(BigDecimal("10.00"))))

        mockMvc.perform(
            post("/api/v1/b2b/partners/$partnerId/transactions/debit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(creditRequestBody())
        )
            .andExpect(status().isUnprocessableContent)
    }

    @Test
    fun `should return 409 when use case throws DuplicateTransactionException`() {
        whenever(createTransactionUseCase.execute(any()))
            .thenThrow(DuplicateTransactionException("key-123"))

        mockMvc.perform(
            post("/api/v1/b2b/partners/$partnerId/transactions/credit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(creditRequestBody())
        )
            .andExpect(status().isConflict)
    }

    @Test
    fun `should return 200 with paginated transaction history`() {
        val output = ListTransactionHistoryOutput(
            transactionId = UUID.randomUUID(),
            type = TransactionType.CREDIT,
            amount = BigDecimal("100.00"),
            description = "Compra de créditos",
            status = TransactionStatus.COMPLETED,
            createdAt = Instant.now(),
        )
        whenever(listTransactionHistoryUseCase.execute(any()))
            .thenReturn(PageCustom(pageNumber = 0, pageSize = 20, totalPages = 1, totalElements = 1L, items = listOf(output)))

        mockMvc.perform(get("/api/v1/b2b/partners/$partnerId/transactions"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.items[0].status").value("COMPLETED"))
    }

    @Test
    fun `should return 404 when history use case throws TransactionPartnerNotFoundException`() {
        whenever(listTransactionHistoryUseCase.execute(any()))
            .thenThrow(TransactionPartnerNotFoundException(partnerId))

        mockMvc.perform(get("/api/v1/b2b/partners/$partnerId/transactions"))
            .andExpect(status().isNotFound)
    }

}

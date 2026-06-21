package br.com.vps.consulting.b2b.management.transaction.application.usecase.history

import br.com.vps.consulting.b2b.management.shared.core.page.PageCustom
import br.com.vps.consulting.b2b.management.shared.core.vo.Money
import br.com.vps.consulting.b2b.management.transaction.domain.Transaction
import br.com.vps.consulting.b2b.management.transaction.domain.TransactionRepository
import br.com.vps.consulting.b2b.management.transaction.domain.TransactionType
import br.com.vps.consulting.b2b.management.transaction.domain.exception.TransactionPartnerNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import java.math.BigDecimal
import java.util.*

@ExtendWith(MockitoExtension::class)
class DefaultListTransactionHistoryUseCaseTest {

    @Mock
    lateinit var validator: ListTransactionHistoryValidator

    @Mock
    lateinit var transactionRepository: TransactionRepository

    private lateinit var useCase: DefaultListTransactionHistoryUseCase

    @BeforeEach
    fun setUp() {
        useCase = DefaultListTransactionHistoryUseCase(validator, transactionRepository)
    }

    private fun input(partnerId: UUID = UUID.randomUUID()) = ListTransactionHistoryInput(
        partnerId = partnerId,
        from = null,
        to = null,
        type = TransactionType.CREDIT,
        pageSize = 20,
        pageNumber = 0,
    )

    @Test
    fun `should return mapped PageCustom of ListTransactionHistoryOutput from repository page`() {
        val partnerId = UUID.randomUUID()
        val transaction = Transaction.createAsPending(
            partnerId = partnerId,
            type = TransactionType.CREDIT,
            amount = Money.of(BigDecimal("100.00")),
            description = "Compra",
            idempotencyKey = "key-123",
        )
        val page = PageCustom(pageNumber = 0, pageSize = 20, totalPages = 1, totalElements = 1L, items = listOf(transaction))
        whenever(
            transactionRepository.findByPartnerId(partnerId, null, null, TransactionType.CREDIT, 20, 0)
        ).thenReturn(page)

        val result = useCase.execute(input(partnerId))

        assertThat(result.items).hasSize(1)
        assertThat(result.items[0].id).isEqualTo(transaction.id.value)
    }

    @Test
    fun `should call validator validate before querying the repository`() {
        val partnerId = UUID.randomUUID()
        whenever(transactionRepository.findByPartnerId(any(), anyOrNull(), anyOrNull(), anyOrNull(), any(), any()))
            .thenReturn(PageCustom(0, 20, 0, 0L, emptyList()))

        useCase.execute(input(partnerId))

        verify(validator).validate(any())
    }

    @Test
    fun `should forward all input filters to transactionRepository findByPartnerId`() {
        val partnerId = UUID.randomUUID()
        whenever(transactionRepository.findByPartnerId(partnerId, null, null, TransactionType.CREDIT, 20, 0))
            .thenReturn(PageCustom(0, 20, 0, 0L, emptyList()))

        useCase.execute(input(partnerId))

        verify(transactionRepository).findByPartnerId(partnerId, null, null, TransactionType.CREDIT, 20, 0)
    }

    @Test
    fun `should propagate exception thrown by the validator without querying the repository`() {
        doThrow(TransactionPartnerNotFoundException(UUID.randomUUID())).whenever(validator).validate(any())

        assertThatThrownBy { useCase.execute(input()) }
            .isInstanceOf(TransactionPartnerNotFoundException::class.java)

        verify(transactionRepository, never()).findByPartnerId(any(), anyOrNull(), anyOrNull(), anyOrNull(), any(), any())
    }

}

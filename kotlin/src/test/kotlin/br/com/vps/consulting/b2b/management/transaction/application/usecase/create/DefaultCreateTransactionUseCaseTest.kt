package br.com.vps.consulting.b2b.management.transaction.application.usecase.create

import br.com.vps.consulting.b2b.management.shared.core.event.EventPublisher
import br.com.vps.consulting.b2b.management.shared.core.vo.Money
import br.com.vps.consulting.b2b.management.transaction.application.usecase.common.handler.TransactionHandler
import br.com.vps.consulting.b2b.management.transaction.domain.Transaction
import br.com.vps.consulting.b2b.management.transaction.domain.TransactionRepository
import br.com.vps.consulting.b2b.management.transaction.domain.TransactionStatus
import br.com.vps.consulting.b2b.management.transaction.domain.TransactionType
import br.com.vps.consulting.b2b.management.transaction.domain.exception.InsufficientBalanceException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.lenient
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import java.math.BigDecimal
import java.util.*

@ExtendWith(MockitoExtension::class)
class DefaultCreateTransactionUseCaseTest {

    @Mock
    lateinit var transactionRepository: TransactionRepository

    @Mock
    lateinit var eventPublisher: EventPublisher

    @Mock
    lateinit var validator: CreateTransactionValidator

    @Mock
    lateinit var creditHandler: TransactionHandler

    @Mock
    lateinit var debitHandler: TransactionHandler

    private lateinit var useCase: DefaultCreateTransactionUseCase

    @BeforeEach
    fun setUp() {
        lenient().whenever(creditHandler.type).thenReturn(TransactionType.CREDIT)
        lenient().whenever(debitHandler.type).thenReturn(TransactionType.DEBIT)
        lenient().whenever(transactionRepository.save(any())).thenAnswer { it.arguments[0] as Transaction }
        useCase = DefaultCreateTransactionUseCase(transactionRepository, eventPublisher, validator, listOf(creditHandler, debitHandler))
    }

    private fun input(type: TransactionType = TransactionType.CREDIT) = CreateTransactionInput(
        partnerId = UUID.randomUUID(),
        amount = BigDecimal("100.00"),
        description = "Compra de créditos",
        idempotencyKey = "key-${UUID.randomUUID()}",
        type = type,
    )

    @Test
    fun `should complete transaction and save with COMPLETED status when CREDIT handler returns true`() {
        whenever(creditHandler.successfullyProcessed(any())).thenReturn(true)

        val result = useCase.execute(input(TransactionType.CREDIT))

        assertThat(result.status).isEqualTo(TransactionStatus.COMPLETED)
        assertThat(result.errorDescription).isNull()
    }

    @Test
    fun `should complete transaction and save with COMPLETED status when DEBIT handler returns true`() {
        whenever(debitHandler.successfullyProcessed(any())).thenReturn(true)

        val result = useCase.execute(input(TransactionType.DEBIT))

        assertThat(result.status).isEqualTo(TransactionStatus.COMPLETED)
    }

    @Test
    fun `should fail transaction with default message when handler returns false`() {
        whenever(creditHandler.successfullyProcessed(any())).thenReturn(false)

        val result = useCase.execute(input(TransactionType.CREDIT))

        assertThat(result.status).isEqualTo(TransactionStatus.FAILED)
        assertThat(result.errorDescription).isEqualTo("Operação de saldo não confirmada")
    }

    @Test
    fun `should fail transaction with the BaseException's message when handler throws a BaseException`() {
        val partnerId = UUID.randomUUID()
        whenever(creditHandler.successfullyProcessed(any()))
            .doThrow(InsufficientBalanceException(partnerId, Money.of(BigDecimal("100.00")), Money.of(BigDecimal("10.00"))))

        val result = useCase.execute(input(TransactionType.CREDIT))

        assertThat(result.status).isEqualTo(TransactionStatus.FAILED)
        assertThat(result.errorDescription).contains("Saldo insuficiente")
    }

    @Test
    fun `should leave transaction PENDING and still save it when handler throws a generic Exception`() {
        whenever(creditHandler.successfullyProcessed(any())).doThrow(RuntimeException("erro inesperado"))

        val result = useCase.execute(input(TransactionType.CREDIT))

        assertThat(result.status).isEqualTo(TransactionStatus.PENDING)
        verify(transactionRepository).save(any())
        verify(eventPublisher, never()).publish(any())
    }

    @Test
    fun `should publish TransactionStatusChanged event when final status is COMPLETED`() {
        whenever(creditHandler.successfullyProcessed(any())).thenReturn(true)

        useCase.execute(input(TransactionType.CREDIT))

        verify(eventPublisher).publish(any())
    }

    @Test
    fun `should publish TransactionStatusChanged event when final status is FAILED`() {
        whenever(creditHandler.successfullyProcessed(any())).thenReturn(false)

        useCase.execute(input(TransactionType.CREDIT))

        verify(eventPublisher).publish(any())
    }

    @Test
    fun `should never publish an event when final status remains PENDING`() {
        whenever(creditHandler.successfullyProcessed(any())).doThrow(RuntimeException("erro inesperado"))

        useCase.execute(input(TransactionType.CREDIT))

        verify(eventPublisher, never()).publish(any())
    }

    @Test
    fun `should call validator validate before processing the transaction`() {
        whenever(creditHandler.successfullyProcessed(any())).thenReturn(true)
        val transactionInput = input(TransactionType.CREDIT)

        useCase.execute(transactionInput)

        verify(validator).validate(transactionInput)
    }

    @Test
    fun `should throw IllegalArgumentException when no handler is registered for the transaction type`() {
        val useCaseWithoutHandlers = DefaultCreateTransactionUseCase(transactionRepository, eventPublisher, validator, emptyList())

        assertThatThrownBy { useCaseWithoutHandlers.execute(input(TransactionType.CREDIT)) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `should return CreateTransactionOutput matching the saved transaction`() {
        whenever(creditHandler.successfullyProcessed(any())).thenReturn(true)

        val result = useCase.execute(input(TransactionType.CREDIT))

        assertThat(result.transactionId).isNotNull()
        assertThat(result.status).isEqualTo(TransactionStatus.COMPLETED)
        assertThat(result.errorDescription).isNull()
    }

}

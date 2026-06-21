package br.com.vps.consulting.b2b.management.transaction.application.usecase.reconciliation

import br.com.vps.consulting.b2b.management.shared.core.event.EventPublisher
import br.com.vps.consulting.b2b.management.shared.core.vo.Money
import br.com.vps.consulting.b2b.management.transaction.application.usecase.common.handler.TransactionHandler
import br.com.vps.consulting.b2b.management.transaction.domain.Transaction
import br.com.vps.consulting.b2b.management.transaction.domain.TransactionRepository
import br.com.vps.consulting.b2b.management.transaction.domain.TransactionStatus
import br.com.vps.consulting.b2b.management.transaction.domain.TransactionType
import br.com.vps.consulting.b2b.management.transaction.domain.exception.InsufficientBalanceException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.Mockito.lenient
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class DefaultReconcileTransactionsUseCaseTest {

    @Mock
    lateinit var transactionRepository: TransactionRepository

    @Mock
    lateinit var eventPublisher: EventPublisher

    @Mock
    lateinit var creditHandler: TransactionHandler

    @Mock
    lateinit var debitHandler: TransactionHandler

    private lateinit var useCase: DefaultReconcileTransactionsUseCase

    @BeforeEach
    fun setUp() {
        lenient().whenever(creditHandler.type).thenReturn(TransactionType.CREDIT)
        lenient().whenever(debitHandler.type).thenReturn(TransactionType.DEBIT)
        useCase = DefaultReconcileTransactionsUseCase(transactionRepository, eventPublisher, listOf(creditHandler, debitHandler))
    }

    @Test
    fun `should call findPending with limit 100`() {
        whenever(transactionRepository.findPending(100)).thenReturn(emptyList())

        useCase.execute()

        verify(transactionRepository).findPending(100)
    }

    @Test
    fun `should complete a pending transaction when its handler returns true`() {
        val transaction = pendingTransaction(TransactionType.CREDIT)
        whenever(transactionRepository.findPending(100)).thenReturn(listOf(transaction))
        whenever(creditHandler.successfullyProcessed(transaction)).thenReturn(true)

        useCase.execute()

        assertThat(transaction.status).isEqualTo(TransactionStatus.COMPLETED)
        verify(transactionRepository).save(transaction)
    }

    @Test
    fun `should fail a pending transaction with default message when its handler returns false`() {
        val transaction = pendingTransaction(TransactionType.DEBIT)
        whenever(transactionRepository.findPending(100)).thenReturn(listOf(transaction))
        whenever(debitHandler.successfullyProcessed(transaction)).thenReturn(false)

        useCase.execute()

        assertThat(transaction.status).isEqualTo(TransactionStatus.FAILED)
        assertThat(transaction.errorDescription).isEqualTo("Operação de saldo não confirmada")
    }

    @Test
    fun `should fail a pending transaction with the BaseException message when its handler throws a BaseException`() {
        val partnerId = UUID.randomUUID()
        val transaction = pendingTransaction(TransactionType.DEBIT, partnerId)
        whenever(transactionRepository.findPending(100)).thenReturn(listOf(transaction))
        whenever(debitHandler.successfullyProcessed(transaction))
            .doThrow(InsufficientBalanceException(partnerId, Money.of(BigDecimal("100.00")), Money.of(BigDecimal("10.00"))))

        useCase.execute()

        assertThat(transaction.status).isEqualTo(TransactionStatus.FAILED)
        assertThat(transaction.errorDescription).contains("Saldo insuficiente")
    }

    @Test
    fun `should leave the transaction PENDING and still save it when handler throws a generic Exception`() {
        val transaction = pendingTransaction(TransactionType.CREDIT)
        whenever(transactionRepository.findPending(100)).thenReturn(listOf(transaction))
        whenever(creditHandler.successfullyProcessed(transaction)).doThrow(RuntimeException("erro inesperado"))

        useCase.execute()

        assertThat(transaction.status).isEqualTo(TransactionStatus.PENDING)
        verify(transactionRepository).save(transaction)
        verify(eventPublisher, never()).publish(any())
    }

    @Test
    fun `should skip a transaction when no handler is registered for its type`() {
        val useCaseWithoutHandlers = DefaultReconcileTransactionsUseCase(transactionRepository, eventPublisher, emptyList())
        val transaction = pendingTransaction(TransactionType.CREDIT)
        whenever(transactionRepository.findPending(100)).thenReturn(listOf(transaction))

        useCaseWithoutHandlers.execute()

        assertThat(transaction.status).isEqualTo(TransactionStatus.PENDING)
        verify(transactionRepository, never()).save(any())
        verify(eventPublisher, never()).publish(any())
    }

    @Test
    fun `should publish event only when final status is not PENDING`() {
        val completed = pendingTransaction(TransactionType.CREDIT)
        whenever(transactionRepository.findPending(100)).thenReturn(listOf(completed))
        whenever(creditHandler.successfullyProcessed(completed)).thenReturn(true)

        useCase.execute()

        verify(eventPublisher, times(1)).publish(any())
    }

    @Test
    fun `should process multiple pending transactions independently in one execute call`() {
        val completed = pendingTransaction(TransactionType.CREDIT)
        val failed = pendingTransaction(TransactionType.DEBIT)
        val stillPending = pendingTransaction(TransactionType.CREDIT)
        whenever(transactionRepository.findPending(100)).thenReturn(listOf(completed, failed, stillPending))
        whenever(creditHandler.successfullyProcessed(completed)).thenReturn(true)
        whenever(debitHandler.successfullyProcessed(failed)).thenReturn(false)
        whenever(creditHandler.successfullyProcessed(stillPending)).doThrow(RuntimeException("erro inesperado"))

        useCase.execute()

        assertThat(completed.status).isEqualTo(TransactionStatus.COMPLETED)
        assertThat(failed.status).isEqualTo(TransactionStatus.FAILED)
        assertThat(stillPending.status).isEqualTo(TransactionStatus.PENDING)
        verify(transactionRepository, times(3)).save(any())
        verify(eventPublisher, times(2)).publish(any())
    }

    private fun pendingTransaction(type: TransactionType = TransactionType.CREDIT, partnerId: UUID = UUID.randomUUID()) =
        Transaction.createAsPending(
            partnerId = partnerId,
            type = type,
            amount = Money.of(BigDecimal("100.00")),
            description = "Compra de créditos",
            idempotencyKey = "key-${UUID.randomUUID()}",
        )


}

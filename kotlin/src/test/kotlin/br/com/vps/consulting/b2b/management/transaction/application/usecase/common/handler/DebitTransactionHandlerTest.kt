package br.com.vps.consulting.b2b.management.transaction.application.usecase.common.handler

import br.com.vps.consulting.b2b.management.shared.core.vo.Money
import br.com.vps.consulting.b2b.management.transaction.application.service.PartnerService
import br.com.vps.consulting.b2b.management.transaction.domain.Transaction
import br.com.vps.consulting.b2b.management.transaction.domain.TransactionType
import br.com.vps.consulting.b2b.management.transaction.domain.exception.InsufficientBalanceException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.util.*

@ExtendWith(MockitoExtension::class)
class DebitTransactionHandlerTest {

    @Mock
    lateinit var partnerService: PartnerService

    private lateinit var handler: DebitTransactionHandler

    @BeforeEach
    fun setUp() {
        handler = DebitTransactionHandler(partnerService)
    }

    private fun transaction(partnerId: UUID = UUID.randomUUID(), amount: BigDecimal = BigDecimal("50.00")) =
        Transaction.createAsPending(
            partnerId = partnerId,
            type = TransactionType.DEBIT,
            amount = Money.of(amount),
            description = "Consumo de créditos",
            idempotencyKey = "key-${UUID.randomUUID()}",
        )

    @Test
    fun `should return true and call partnerService debitBalance when amount does not exceed available balance`() {
        val partnerId = UUID.randomUUID()
        val transaction = transaction(partnerId = partnerId, amount = BigDecimal("50.00"))
        whenever(partnerService.findBalanceById(partnerId)).thenReturn(Money.of(BigDecimal("100.00")))
        whenever(partnerService.debitBalance(eq(partnerId), eq(Money.of(BigDecimal("50.00"))))).thenReturn(true)

        val result = handler.successfullyProcessed(transaction)

        assertThat(result).isTrue()
    }

    @Test
    fun `should return false when partnerService debitBalance returns false`() {
        val partnerId = UUID.randomUUID()
        val transaction = transaction(partnerId = partnerId, amount = BigDecimal("50.00"))
        whenever(partnerService.findBalanceById(partnerId)).thenReturn(Money.of(BigDecimal("100.00")))
        whenever(partnerService.debitBalance(eq(partnerId), eq(Money.of(BigDecimal("50.00"))))).thenReturn(false)

        val result = handler.successfullyProcessed(transaction)

        assertThat(result).isFalse()
    }

    @Test
    fun `should throw InsufficientBalanceException when amount is greater than available balance`() {
        val partnerId = UUID.randomUUID()
        val transaction = transaction(partnerId = partnerId, amount = BigDecimal("150.00"))
        whenever(partnerService.findBalanceById(partnerId)).thenReturn(Money.of(BigDecimal("100.00")))

        assertThatThrownBy { handler.successfullyProcessed(transaction) }
            .isInstanceOf(InsufficientBalanceException::class.java)
    }

    @Test
    fun `should call partnerService findBalanceById with the transaction's partnerId`() {
        val partnerId = UUID.randomUUID()
        val transaction = transaction(partnerId = partnerId, amount = BigDecimal("50.00"))
        whenever(partnerService.findBalanceById(partnerId)).thenReturn(Money.of(BigDecimal("100.00")))
        whenever(partnerService.debitBalance(eq(partnerId), eq(Money.of(BigDecimal("50.00"))))).thenReturn(true)

        handler.successfullyProcessed(transaction)

        verify(partnerService).findBalanceById(partnerId)
    }

}

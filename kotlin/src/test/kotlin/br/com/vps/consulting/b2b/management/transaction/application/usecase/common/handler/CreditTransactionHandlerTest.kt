package br.com.vps.consulting.b2b.management.transaction.application.usecase.common.handler

import br.com.vps.consulting.b2b.management.shared.core.vo.Money
import br.com.vps.consulting.b2b.management.transaction.application.service.PartnerService
import br.com.vps.consulting.b2b.management.transaction.domain.Transaction
import br.com.vps.consulting.b2b.management.transaction.domain.TransactionType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class CreditTransactionHandlerTest {

    @Mock
    lateinit var partnerService: PartnerService

    private lateinit var handler: CreditTransactionHandler

    @BeforeEach
    fun setUp() {
        handler = CreditTransactionHandler(partnerService)
    }

    private fun transaction(partnerId: UUID = UUID.randomUUID(), amount: BigDecimal = BigDecimal("100.00")) =
        Transaction.createAsPending(
            partnerId = partnerId,
            type = TransactionType.CREDIT,
            amount = Money.of(amount),
            description = "Compra de créditos",
            idempotencyKey = "key-${UUID.randomUUID()}",
        )

    @Test
    fun `should return true and call partnerService creditBalance when amount is positive`() {
        val partnerId = UUID.randomUUID()
        val transaction = transaction(partnerId = partnerId, amount = BigDecimal("100.00"))
        whenever(partnerService.creditBalance(eq(partnerId), eq(Money.of(BigDecimal("100.00"))))).thenReturn(true)

        val result = handler.successfullyProcessed(transaction)

        assertThat(result).isTrue()
    }

    @Test
    fun `should return false when partnerService creditBalance returns false`() {
        val partnerId = UUID.randomUUID()
        val transaction = transaction(partnerId = partnerId, amount = BigDecimal("100.00"))
        whenever(partnerService.creditBalance(eq(partnerId), eq(Money.of(BigDecimal("100.00"))))).thenReturn(false)

        val result = handler.successfullyProcessed(transaction)

        assertThat(result).isFalse()
    }

}

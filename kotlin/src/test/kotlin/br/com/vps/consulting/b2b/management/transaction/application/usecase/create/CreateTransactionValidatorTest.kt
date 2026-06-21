package br.com.vps.consulting.b2b.management.transaction.application.usecase.create

import br.com.vps.consulting.b2b.management.shared.core.vo.Money
import br.com.vps.consulting.b2b.management.transaction.application.service.PartnerService
import br.com.vps.consulting.b2b.management.transaction.domain.Transaction
import br.com.vps.consulting.b2b.management.transaction.domain.TransactionRepository
import br.com.vps.consulting.b2b.management.transaction.domain.TransactionType
import br.com.vps.consulting.b2b.management.transaction.domain.exception.DuplicateTransactionException
import br.com.vps.consulting.b2b.management.transaction.domain.exception.InvalidIdempotencyKeyException
import br.com.vps.consulting.b2b.management.transaction.domain.exception.InvalidTransactionAmountException
import br.com.vps.consulting.b2b.management.transaction.domain.exception.TransactionPartnerNotFoundException
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.util.*

@ExtendWith(MockitoExtension::class)
class CreateTransactionValidatorTest {

    @Mock
    lateinit var partnerService: PartnerService

    @Mock
    lateinit var transactionRepository: TransactionRepository

    private lateinit var validator: CreateTransactionValidator

    @BeforeEach
    fun setUp() {
        validator = CreateTransactionValidator(partnerService, transactionRepository)
    }

    private fun input(
        partnerId: UUID = UUID.randomUUID(),
        amount: BigDecimal = BigDecimal("100.00"),
        idempotencyKey: String = "key-123",
    ) = CreateTransactionInput(
        partnerId = partnerId,
        amount = amount,
        description = "Compra de créditos",
        idempotencyKey = idempotencyKey,
        type = TransactionType.CREDIT,
    )

    @Test
    fun `should throw InvalidIdempotencyKeyException when idempotencyKey is blank`() {
        assertThatThrownBy { validator.validate(input(idempotencyKey = "  ")) }
            .isInstanceOf(InvalidIdempotencyKeyException::class.java)

        verify(transactionRepository, never()).findByIdempotencyKey(any())
    }

    @Test
    fun `should throw DuplicateTransactionException when findByIdempotencyKey returns an existing transaction`() {
        val existing = Transaction.createAsPending(
            partnerId = UUID.randomUUID(),
            type = TransactionType.CREDIT,
            amount = Money.of(BigDecimal("50.00")),
            description = "Anterior",
            idempotencyKey = "key-123",
        )
        whenever(transactionRepository.findByIdempotencyKey("key-123")).thenReturn(existing)

        assertThatThrownBy { validator.validate(input(idempotencyKey = "key-123")) }
            .isInstanceOf(DuplicateTransactionException::class.java)
    }

    @Test
    fun `should throw InvalidTransactionAmountException when amount is zero`() {
        whenever(transactionRepository.findByIdempotencyKey("key-123")).thenReturn(null)

        assertThatThrownBy { validator.validate(input(amount = BigDecimal.ZERO)) }
            .isInstanceOf(InvalidTransactionAmountException::class.java)
    }

    @Test
    fun `should throw InvalidTransactionAmountException when amount is negative`() {
        whenever(transactionRepository.findByIdempotencyKey("key-123")).thenReturn(null)

        assertThatThrownBy { validator.validate(input(amount = BigDecimal("-10.00"))) }
            .isInstanceOf(InvalidTransactionAmountException::class.java)
    }

    @Test
    fun `should throw TransactionPartnerNotFoundException when partner does not exist`() {
        val partnerId = UUID.randomUUID()
        whenever(transactionRepository.findByIdempotencyKey("key-123")).thenReturn(null)
        whenever(partnerService.existsById(partnerId)).thenReturn(false)

        assertThatThrownBy { validator.validate(input(partnerId = partnerId)) }
            .isInstanceOf(TransactionPartnerNotFoundException::class.java)
    }

    @Test
    fun `should not throw when all conditions are satisfied`() {
        val partnerId = UUID.randomUUID()
        whenever(transactionRepository.findByIdempotencyKey("key-123")).thenReturn(null)
        whenever(partnerService.existsById(partnerId)).thenReturn(true)

        assertThatCode { validator.validate(input(partnerId = partnerId)) }.doesNotThrowAnyException()
    }

}

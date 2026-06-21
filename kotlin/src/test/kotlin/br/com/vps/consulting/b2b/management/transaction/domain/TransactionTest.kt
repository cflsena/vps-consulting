package br.com.vps.consulting.b2b.management.transaction.domain

import br.com.vps.consulting.b2b.management.shared.core.vo.Money
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant
import java.util.*

class TransactionTest {

    private val partnerId = UUID.randomUUID()

    private fun pendingTransaction(
        amount: Money = Money.of(BigDecimal("100.00")),
        description: String = "Compra de créditos",
        idempotencyKey: String = "key-123",
    ) = Transaction.createAsPending(
        partnerId = partnerId,
        type = TransactionType.CREDIT,
        amount = amount,
        description = description,
        idempotencyKey = idempotencyKey,
    )

    @Test
    fun `should create pending transaction via createAsPending with generated id and PENDING status`() {
        val transaction = pendingTransaction()

        assertThat(transaction.id).isNotNull()
        assertThat(transaction.status).isEqualTo(TransactionStatus.PENDING)
        assertThat(transaction.errorDescription).isNull()
    }

    @Test
    fun `should set createdAt equal to updatedAt on creation via createAsPending`() {
        val transaction = pendingTransaction()

        assertThat(transaction.createdAt).isEqualTo(transaction.updatedAt)
    }

    @Test
    fun `should reject zero amount via createAsPending`() {
        assertThatThrownBy { pendingTransaction(amount = Money.of(BigDecimal.ZERO)) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("O valor deve ser positivo")
    }

    @Test
    fun `should reject negative amount via createAsPending`() {
        assertThatThrownBy { pendingTransaction(amount = Money.of(BigDecimal("-10.00"))) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("não pode ser negativo")
    }

    @Test
    fun `should reject blank description via createAsPending`() {
        assertThatThrownBy { pendingTransaction(description = "  ") }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("descrição")
    }

    @Test
    fun `should reject blank idempotencyKey via createAsPending`() {
        assertThatThrownBy { pendingTransaction(idempotencyKey = "  ") }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("idempotência")
    }

    @Test
    fun `should reconstruct transaction via with preserving all fields including errorDescription and status`() {
        val id = TransactionId.generate()
        val createdAt = Instant.now()
        val updatedAt = createdAt.plusSeconds(60)

        val transaction = Transaction.with(
            id = id,
            partnerId = partnerId,
            type = TransactionType.DEBIT,
            amount = Money.of(BigDecimal("50.00")),
            description = "Consumo de créditos",
            idempotencyKey = "key-456",
            status = TransactionStatus.FAILED,
            errorDescription = "Saldo insuficiente",
            createdAt = createdAt,
            updatedAt = updatedAt,
        )

        assertThat(transaction.id).isEqualTo(id)
        assertThat(transaction.partnerId).isEqualTo(partnerId)
        assertThat(transaction.type).isEqualTo(TransactionType.DEBIT)
        assertThat(transaction.amount.value).isEqualByComparingTo("50.00")
        assertThat(transaction.description).isEqualTo("Consumo de créditos")
        assertThat(transaction.idempotencyKey).isEqualTo("key-456")
        assertThat(transaction.status).isEqualTo(TransactionStatus.FAILED)
        assertThat(transaction.errorDescription).isEqualTo("Saldo insuficiente")
        assertThat(transaction.createdAt).isEqualTo(createdAt)
        assertThat(transaction.updatedAt).isEqualTo(updatedAt)
    }

    @Test
    fun `should reject invalid transaction via with`() {
        assertThatThrownBy {
            Transaction.with(
                id = TransactionId.generate(),
                partnerId = partnerId,
                type = TransactionType.CREDIT,
                amount = Money.of(BigDecimal.ZERO),
                description = "Compra",
                idempotencyKey = "key-789",
                status = TransactionStatus.PENDING,
                errorDescription = null,
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
            )
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("O valor deve ser positivo")
    }

    @Test
    fun `should complete set status to COMPLETED clear errorDescription and bump updatedAt`() {
        val transaction = Transaction.with(
            id = TransactionId.generate(),
            partnerId = partnerId,
            type = TransactionType.CREDIT,
            amount = Money.of(BigDecimal("100.00")),
            description = "Compra",
            idempotencyKey = "key-complete",
            status = TransactionStatus.FAILED,
            errorDescription = "Erro anterior",
            createdAt = Instant.now().minusSeconds(120),
            updatedAt = Instant.now().minusSeconds(120),
        )
        val updatedAtBefore = transaction.updatedAt

        transaction.complete()

        assertThat(transaction.status).isEqualTo(TransactionStatus.COMPLETED)
        assertThat(transaction.errorDescription).isNull()
        assertThat(transaction.updatedAt).isAfter(updatedAtBefore)
    }

    @Test
    fun `should fail set status to FAILED set errorDescription and bump updatedAt`() {
        val transaction = pendingTransaction()
        val updatedAtBefore = transaction.updatedAt

        transaction.fail("Saldo insuficiente")

        assertThat(transaction.status).isEqualTo(TransactionStatus.FAILED)
        assertThat(transaction.errorDescription).isEqualTo("Saldo insuficiente")
        assertThat(transaction.updatedAt).isAfterOrEqualTo(updatedAtBefore)
    }

    @Test
    fun `should fail accept null errorDescription`() {
        val transaction = pendingTransaction()

        transaction.fail()

        assertThat(transaction.status).isEqualTo(TransactionStatus.FAILED)
        assertThat(transaction.errorDescription).isNull()
    }

}

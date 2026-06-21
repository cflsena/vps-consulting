package br.com.vps.consulting.b2b.management.transaction.domain.exception

import br.com.vps.consulting.b2b.management.transaction.domain.TransactionId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TransactionExceptionsTest {

    @Test
    fun `TransactionNotFoundException should contain the transaction id in its message`() {
        val transactionId = TransactionId.generate()

        val exception = TransactionNotFoundException(transactionId)

        assertThat(exception.message).contains(transactionId.value.toString())
    }

    @Test
    fun `InvalidCreditAmountException should expose the given message`() {
        val exception = InvalidCreditAmountException("Valor de crédito inválido")

        assertThat(exception.message).isEqualTo("Valor de crédito inválido")
    }

    @Test
    fun `InvalidDebitAmountException should expose the given message`() {
        val exception = InvalidDebitAmountException("Valor de débito inválido")

        assertThat(exception.message).isEqualTo("Valor de débito inválido")
    }

}

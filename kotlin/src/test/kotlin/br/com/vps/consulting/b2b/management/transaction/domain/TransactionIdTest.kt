package br.com.vps.consulting.b2b.management.transaction.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

class TransactionIdTest {

    @Test
    fun `should generate a TransactionId wrapping a random UUID`() {
        assertThat(TransactionId.generate().value).isNotNull()
    }

    @Test
    fun `should create TransactionId from an existing UUID`() {
        val uuid = UUID.randomUUID()

        assertThat(TransactionId.from(uuid).value).isEqualTo(uuid)
    }

    @Test
    fun `should create TransactionId from a UUID string`() {
        val uuid = UUID.randomUUID()

        assertThat(TransactionId.from(uuid.toString()).value).isEqualTo(uuid)
    }

}

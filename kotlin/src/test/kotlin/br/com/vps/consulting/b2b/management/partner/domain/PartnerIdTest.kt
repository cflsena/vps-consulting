package br.com.vps.consulting.b2b.management.partner.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.*

class PartnerIdTest {

    @Test
    fun `should generate a PartnerId wrapping a random UUID`() {
        assertThat(PartnerId.generate().value).isNotNull()
    }

    @Test
    fun `should create PartnerId from an existing UUID`() {
        val uuid = UUID.randomUUID()

        assertThat(PartnerId.from(uuid).value).isEqualTo(uuid)
    }

    @Test
    fun `should create PartnerId from a UUID string`() {
        val uuid = UUID.randomUUID()

        assertThat(PartnerId.from(uuid.toString()).value).isEqualTo(uuid)
    }

}

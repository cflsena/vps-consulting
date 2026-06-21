package br.com.vps.consulting.b2b.management.partner.domain

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.Instant

class PartnerTest {

    @Test
    fun `should create partner via create with generated id`() {
        val partner = Partner.create(name = "Acme Corp", document = "12345678000100")

        assertThat(partner.id).isNotNull()
        assertThat(partner.name).isEqualTo("Acme Corp")
        assertThat(partner.document).isEqualTo("12345678000100")
    }

    @Test
    fun `should create partner via create with current timestamp as createdAt`() {
        val before = Instant.now()

        val partner = Partner.create(name = "Acme Corp", document = "12345678000100")

        assertThat(partner.createdAt).isAfterOrEqualTo(before)
    }

    @Test
    fun `should reject blank name via create`() {
        assertThatThrownBy { Partner.create(name = "   ", document = "12345678000100") }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("não pode estar em branco")
    }

    @Test
    fun `should reject blank document via create`() {
        assertThatThrownBy { Partner.create(name = "Acme Corp", document = "   ") }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("não pode estar em branco")
    }

    @Test
    fun `should reconstruct partner via with preserving all fields`() {
        val id = PartnerId.generate()
        val createdAt = Instant.now()

        val partner = Partner.with(id = id, name = "Acme Corp", document = "12345678000100", createdAt = createdAt)

        assertThat(partner.id).isEqualTo(id)
        assertThat(partner.name).isEqualTo("Acme Corp")
        assertThat(partner.document).isEqualTo("12345678000100")
        assertThat(partner.createdAt).isEqualTo(createdAt)
    }

    @Test
    fun `should reject blank name via with`() {
        assertThatThrownBy {
            Partner.with(id = PartnerId.generate(), name = "  ", document = "12345678000100", createdAt = Instant.now())
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("não pode estar em branco")
    }

    @Test
    fun `should reject blank document via with`() {
        assertThatThrownBy {
            Partner.with(id = PartnerId.generate(), name = "Acme Corp", document = "  ", createdAt = Instant.now())
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("não pode estar em branco")
    }

}

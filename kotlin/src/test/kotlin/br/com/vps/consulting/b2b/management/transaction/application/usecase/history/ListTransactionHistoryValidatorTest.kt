package br.com.vps.consulting.b2b.management.transaction.application.usecase.history

import br.com.vps.consulting.b2b.management.shared.core.exception.DomainException
import br.com.vps.consulting.b2b.management.transaction.application.service.PartnerService
import br.com.vps.consulting.b2b.management.transaction.domain.exception.TransactionPartnerNotFoundException
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class ListTransactionHistoryValidatorTest {

    @Mock
    lateinit var partnerService: PartnerService

    private lateinit var validator: ListTransactionHistoryValidator

    @BeforeEach
    fun setUp() {
        validator = ListTransactionHistoryValidator(partnerService)
    }

    private fun input(partnerId: UUID, from: Instant? = null, to: Instant? = null) = ListTransactionHistoryInput(
        partnerId = partnerId,
        from = from,
        to = to,
        type = null,
        pageSize = 20,
        pageNumber = 0,
    )

    @Test
    fun `should throw TransactionPartnerNotFoundException when partner does not exist`() {
        val partnerId = UUID.randomUUID()
        whenever(partnerService.existsById(partnerId)).thenReturn(false)

        assertThatThrownBy { validator.validate(input(partnerId)) }
            .isInstanceOf(TransactionPartnerNotFoundException::class.java)
    }

    @Test
    fun `should throw DomainException when from is after to`() {
        val partnerId = UUID.randomUUID()
        whenever(partnerService.existsById(partnerId)).thenReturn(true)
        val to = Instant.now()
        val from = to.plusSeconds(60)

        assertThatThrownBy { validator.validate(input(partnerId, from = from, to = to)) }
            .isInstanceOf(DomainException::class.java)
            .hasMessageContaining("data inicial")
    }

    @Test
    fun `should not throw when from and to are both null`() {
        val partnerId = UUID.randomUUID()
        whenever(partnerService.existsById(partnerId)).thenReturn(true)

        assertThatCode { validator.validate(input(partnerId)) }.doesNotThrowAnyException()
    }

    @Test
    fun `should not throw when from is before or equal to to`() {
        val partnerId = UUID.randomUUID()
        whenever(partnerService.existsById(partnerId)).thenReturn(true)
        val from = Instant.now()
        val to = from.plusSeconds(60)

        assertThatCode { validator.validate(input(partnerId, from = from, to = to)) }.doesNotThrowAnyException()
    }

}

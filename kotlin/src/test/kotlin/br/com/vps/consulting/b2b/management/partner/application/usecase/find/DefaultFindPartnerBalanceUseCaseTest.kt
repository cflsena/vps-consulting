package br.com.vps.consulting.b2b.management.partner.application.usecase.find

import br.com.vps.consulting.b2b.management.partner.domain.PartnerBalance
import br.com.vps.consulting.b2b.management.partner.domain.PartnerId
import br.com.vps.consulting.b2b.management.partner.domain.PartnerRepository
import br.com.vps.consulting.b2b.management.partner.domain.exception.PartnerNotFoundException
import br.com.vps.consulting.b2b.management.shared.core.vo.Money
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.time.Instant

@ExtendWith(MockitoExtension::class)
class DefaultFindPartnerBalanceUseCaseTest {

    @Mock
    lateinit var partnerRepository: PartnerRepository

    private lateinit var useCase: DefaultFindPartnerBalanceUseCase

    @BeforeEach
    fun setUp() {
        useCase = DefaultFindPartnerBalanceUseCase(partnerRepository)
    }

    @Test
    fun `should return mapped FindPartnerBalanceOutput when balance exists`() {
        val partnerId = PartnerId.generate()
        val updatedAt = Instant.now()
        val balance = PartnerBalance(
            partnerId = partnerId,
            totalBalance = Money.of(BigDecimal("100.00")),
            availableBalance = Money.of(BigDecimal("60.00")),
            updatedAt = updatedAt,
        )
        whenever(partnerRepository.findBalanceById(eq(partnerId))).thenReturn(balance)

        val result = useCase.execute(FindPartnerBalanceInput(partnerId.value))

        assertThat(result.partnerId).isEqualTo(partnerId.value)
        assertThat(result.totalBalance).isEqualByComparingTo("100.00")
        assertThat(result.availableBalance).isEqualByComparingTo("60.00")
        assertThat(result.updatedAt).isEqualTo(updatedAt)
    }

    @Test
    fun `should throw PartnerNotFoundException when findBalanceById returns null`() {
        val partnerId = PartnerId.generate()
        whenever(partnerRepository.findBalanceById(eq(partnerId))).thenReturn(null)

        assertThatThrownBy { useCase.execute(FindPartnerBalanceInput(partnerId.value)) }
            .isInstanceOf(PartnerNotFoundException::class.java)
    }

}

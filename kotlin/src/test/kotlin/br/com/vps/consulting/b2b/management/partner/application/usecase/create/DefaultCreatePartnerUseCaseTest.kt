package br.com.vps.consulting.b2b.management.partner.application.usecase.create

import br.com.vps.consulting.b2b.management.partner.domain.Partner
import br.com.vps.consulting.b2b.management.partner.domain.PartnerBalance
import br.com.vps.consulting.b2b.management.partner.domain.PartnerBalanceRepository
import br.com.vps.consulting.b2b.management.partner.domain.PartnerRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class DefaultCreatePartnerUseCaseTest {

    @Mock
    lateinit var partnerRepository: PartnerRepository

    @Mock
    lateinit var partnerBalanceRepository: PartnerBalanceRepository

    private lateinit var useCase: DefaultCreatePartnerUseCase

    @BeforeEach
    fun setUp() {
        useCase = DefaultCreatePartnerUseCase(partnerRepository, partnerBalanceRepository)
    }

    @Test
    fun `should create partner and return its id`() {
        val input = CreatePartnerInput(name = "Acme Corp", document = "12345678000100")
        val saved = Partner.with(name = input.name, document = input.document)
        whenever(partnerRepository.save(any())).thenReturn(saved)

        val result = useCase.execute(input)

        assertThat(result).isEqualTo(saved.id.value)
    }

    @Test
    fun `should save a Partner built from the input's name and document`() {
        val input = CreatePartnerInput(name = "Acme Corp", document = "12345678000100")
        whenever(partnerRepository.save(any())).thenAnswer { it.arguments[0] as Partner }

        useCase.execute(input)

        val captor = argumentCaptor<Partner>()
        verify(partnerRepository).save(captor.capture())
        assertThat(captor.firstValue.name).isEqualTo("Acme Corp")
        assertThat(captor.firstValue.document).isEqualTo("12345678000100")
    }

    @Test
    fun `should save a PartnerBalance with zeroed balances for the created partner`() {
        val input = CreatePartnerInput(name = "Acme Corp", document = "12345678000100")
        val saved = Partner.with(name = input.name, document = input.document)
        whenever(partnerRepository.save(any())).thenReturn(saved)

        useCase.execute(input)

        val captor = argumentCaptor<PartnerBalance>()
        verify(partnerBalanceRepository).save(captor.capture())
        assertThat(captor.firstValue.id).isEqualTo(saved.id)
        assertThat(captor.firstValue.totalCredited.value).isEqualByComparingTo("0.00")
        assertThat(captor.firstValue.totalDebited.value).isEqualByComparingTo("0.00")
        assertThat(captor.firstValue.availableBalance.value).isEqualByComparingTo("0.00")
    }

}

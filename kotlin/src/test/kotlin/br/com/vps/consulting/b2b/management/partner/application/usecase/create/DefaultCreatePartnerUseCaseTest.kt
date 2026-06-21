package br.com.vps.consulting.b2b.management.partner.application.usecase.create

import br.com.vps.consulting.b2b.management.partner.domain.Partner
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

    private lateinit var useCase: DefaultCreatePartnerUseCase

    @BeforeEach
    fun setUp() {
        useCase = DefaultCreatePartnerUseCase(partnerRepository)
    }

    @Test
    fun `should create partner and return its id`() {
        val input = CreatePartnerInput(name = "Acme Corp", document = "12345678000100")
        val saved = Partner.create(name = input.name, document = input.document)
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

}

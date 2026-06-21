package br.com.vps.consulting.b2b.management.partner.application.usecase.list

import br.com.vps.consulting.b2b.management.partner.domain.Partner
import br.com.vps.consulting.b2b.management.partner.domain.PartnerRepository
import br.com.vps.consulting.b2b.management.shared.core.page.PageCustom
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class DefaultListPartnersUseCaseTest {

    @Mock
    lateinit var partnerRepository: PartnerRepository

    private lateinit var useCase: DefaultListPartnersUseCase

    @BeforeEach
    fun setUp() {
        useCase = DefaultListPartnersUseCase(partnerRepository)
    }

    @Test
    fun `should return mapped PageCustom of ListPartnersOutput from repository page`() {
        val partner = Partner.create(name = "Acme Corp", document = "12345678000100")
        val page = PageCustom(pageNumber = 0, pageSize = 20, totalPages = 1, totalElements = 1L, items = listOf(partner))
        whenever(partnerRepository.findAll("12345678000100", 20, 0)).thenReturn(page)

        val result = useCase.execute(ListPartnersInput(document = "12345678000100", pageSize = 20, pageNumber = 0))

        assertThat(result.items).hasSize(1)
        assertThat(result.items[0].id).isEqualTo(partner.id.value)
        assertThat(result.items[0].name).isEqualTo("Acme Corp")
        assertThat(result.totalElements).isEqualTo(1L)
    }

    @Test
    fun `should forward document pageSize and pageNumber from input to repository findAll`() {
        whenever(partnerRepository.findAll(null, 10, 2))
            .thenReturn(PageCustom(pageNumber = 2, pageSize = 10, totalPages = 0, totalElements = 0L, items = emptyList()))

        useCase.execute(ListPartnersInput(document = null, pageSize = 10, pageNumber = 2))

        verify(partnerRepository).findAll(null, 10, 2)
    }

    @Test
    fun `should return empty page when no partners exist`() {
        whenever(partnerRepository.findAll(null, 20, 0))
            .thenReturn(PageCustom(pageNumber = 0, pageSize = 20, totalPages = 0, totalElements = 0L, items = emptyList()))

        val result = useCase.execute(ListPartnersInput(document = null, pageSize = 20, pageNumber = 0))

        assertThat(result.items).isEmpty()
        assertThat(result.totalElements).isZero()
    }

}

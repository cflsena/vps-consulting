package br.com.vps.consulting.b2b.management.partner.infrastructure.api

import br.com.vps.consulting.b2b.management.partner.application.usecase.create.CreatePartnerUseCase
import br.com.vps.consulting.b2b.management.partner.application.usecase.find.FindPartnerBalanceOutput
import br.com.vps.consulting.b2b.management.partner.application.usecase.find.FindPartnerBalanceUseCase
import br.com.vps.consulting.b2b.management.partner.application.usecase.list.ListPartnersOutput
import br.com.vps.consulting.b2b.management.partner.application.usecase.list.ListPartnersUseCase
import br.com.vps.consulting.b2b.management.partner.domain.PartnerId
import br.com.vps.consulting.b2b.management.partner.domain.exception.PartnerNotFoundException
import br.com.vps.consulting.b2b.management.shared.core.page.PageCustom
import br.com.vps.consulting.b2b.management.shared.infrastructure.exception.GlobalExceptionHandler
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@WebMvcTest(PartnerController::class)
@Import(GlobalExceptionHandler::class)
class PartnerControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var createPartnerUseCase: CreatePartnerUseCase

    @MockitoBean
    lateinit var findPartnerBalanceUseCase: FindPartnerBalanceUseCase

    @MockitoBean
    lateinit var listPartnersUseCase: ListPartnersUseCase

    @Test
    fun `should create partner and return 201 with id`() {
        val id = UUID.randomUUID()
        whenever(createPartnerUseCase.execute(any())).thenReturn(id)

        mockMvc.perform(
            post("/api/v1/b2b/partners")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"Acme Corp","document":"12345678000100","availableBalance":100.00}""")
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(id.toString()))
    }

    @Test
    fun `should return 400 when name is blank`() {
        mockMvc.perform(
            post("/api/v1/b2b/partners")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"","document":"12345678000100","availableBalance":100.00}""")
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `should return 200 with balance`() {
        val partnerId = UUID.randomUUID()
        val output = FindPartnerBalanceOutput(
            partnerId = partnerId,
            totalBalance = BigDecimal("100.00"),
            availableBalance = BigDecimal("60.00"),
            updatedAt = Instant.now(),
        )
        whenever(findPartnerBalanceUseCase.execute(any())).thenReturn(output)

        mockMvc.perform(get("/api/v1/b2b/partners/$partnerId/balance"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.availableBalance").value(60.00))
    }

    @Test
    fun `should return 404 when balance use case throws PartnerNotFoundException`() {
        val partnerId = UUID.randomUUID()
        whenever(findPartnerBalanceUseCase.execute(any()))
            .thenThrow(PartnerNotFoundException(PartnerId.from(partnerId)))

        mockMvc.perform(get("/api/v1/b2b/partners/$partnerId/balance"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `should return 200 with paginated partner list`() {
        val output = ListPartnersOutput(
            id = UUID.randomUUID(),
            name = "Acme Corp",
            document = "12345678000100",
            createdAt = Instant.now(),
        )
        whenever(listPartnersUseCase.execute(any()))
            .thenReturn(PageCustom(pageNumber = 0, pageSize = 20, totalPages = 1, totalElements = 1L, items = listOf(output)))

        mockMvc.perform(get("/api/v1/b2b/partners"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.items[0].document").value("12345678000100"))
    }

}

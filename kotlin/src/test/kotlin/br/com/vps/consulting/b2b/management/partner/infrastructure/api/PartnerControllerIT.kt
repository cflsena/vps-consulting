package br.com.vps.consulting.b2b.management.partner.infrastructure.api

import br.com.vps.consulting.b2b.management.TestcontainersConfiguration
import br.com.vps.consulting.b2b.management.partner.infrastructure.persistence.jpa.PartnerBalanceJpaRepository
import br.com.vps.consulting.b2b.management.partner.infrastructure.persistence.jpa.PartnerJpaRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.databind.json.JsonMapper
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration::class)
class PartnerControllerIT {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var partnerJpaRepository: PartnerJpaRepository

    @Autowired
    lateinit var partnerBalanceJpaRepository: PartnerBalanceJpaRepository

    private val jsonMapper = JsonMapper.builder().build()
    private val createdIds = mutableListOf<UUID>()

    @AfterEach
    fun cleanup() {
        createdIds.forEach {
            partnerBalanceJpaRepository.deleteById(it)
            partnerJpaRepository.deleteById(it)
        }
        createdIds.clear()
    }

    private fun document() = UUID.randomUUID().toString().replace("-", "").take(14)

    private fun createPartner(name: String = "Acme Corp", document: String = document()): UUID {
        val result = mockMvc.perform(
            post("/api/v1/b2b/partners")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"$name","document":"$document"}""")
        ).andReturn()
        val id = UUID.fromString(jsonMapper.readTree(result.response.contentAsString).get("id").stringValue())
        createdIds.add(id)
        return id
    }

    @Test
    fun `should create partner and return 201 with a zero-balance row`() {
        val document = document()

        val result = mockMvc.perform(
            post("/api/v1/b2b/partners")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"Acme Corp","document":"$document"}""")
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").exists())
            .andReturn()

        val id = UUID.fromString(jsonMapper.readTree(result.response.contentAsString).get("id").stringValue())
        createdIds.add(id)

        assertThat(partnerJpaRepository.findById(id)).isPresent
        val balance = partnerBalanceJpaRepository.findById(id).orElseThrow()
        assertThat(balance.totalBalance).isEqualByComparingTo(java.math.BigDecimal.ZERO)
        assertThat(balance.availableBalance).isEqualByComparingTo(java.math.BigDecimal.ZERO)
    }

    @Test
    fun `should return 400 when name is blank`() {
        mockMvc.perform(
            post("/api/v1/b2b/partners")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"","document":"${document()}"}""")
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `should return 400 when document is blank`() {
        mockMvc.perform(
            post("/api/v1/b2b/partners")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"Acme Corp","document":""}""")
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `should return 200 with zero balances for a freshly created partner`() {
        val id = createPartner()

        mockMvc.perform(get("/api/v1/b2b/partners/$id/balance"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.totalBalance").value(0))
            .andExpect(jsonPath("$.availableBalance").value(0))
    }

    @Test
    fun `should return 404 when partner does not exist`() {
        mockMvc.perform(get("/api/v1/b2b/partners/${UUID.randomUUID()}/balance"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `should filter listing by document`() {
        val document = document()
        val id = createPartner(document = document)
        createPartner()

        mockMvc.perform(get("/api/v1/b2b/partners").param("document", document))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.items.length()").value(1))
            .andExpect(jsonPath("$.items[0].id").value(id.toString()))
    }

    @Test
    fun `should respect pagination parameters`() {
        repeat(3) { createPartner() }

        mockMvc.perform(get("/api/v1/b2b/partners").param("pageSize", "2").param("pageNumber", "0"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.items.length()").value(2))
    }

}

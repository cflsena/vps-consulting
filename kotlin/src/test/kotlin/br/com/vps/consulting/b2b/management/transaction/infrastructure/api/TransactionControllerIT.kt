package br.com.vps.consulting.b2b.management.transaction.infrastructure.api

import br.com.vps.consulting.b2b.management.TestcontainersConfiguration
import br.com.vps.consulting.b2b.management.partner.infrastructure.persistence.jpa.PartnerBalanceJpaRepository
import br.com.vps.consulting.b2b.management.partner.infrastructure.persistence.jpa.PartnerJpaRepository
import br.com.vps.consulting.b2b.management.transaction.infrastructure.persistence.jpa.TransactionJpaRepository
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
class TransactionControllerIT {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var partnerJpaRepository: PartnerJpaRepository

    @Autowired
    lateinit var partnerBalanceJpaRepository: PartnerBalanceJpaRepository

    @Autowired
    lateinit var transactionJpaRepository: TransactionJpaRepository

    private val jsonMapper = JsonMapper.builder().build()
    private val createdPartnerIds = mutableListOf<UUID>()
    private val createdTransactionIds = mutableListOf<UUID>()

    @AfterEach
    fun cleanup() {
        createdTransactionIds.forEach { transactionJpaRepository.deleteById(it) }
        createdTransactionIds.clear()
        createdPartnerIds.forEach {
            partnerBalanceJpaRepository.deleteById(it)
            partnerJpaRepository.deleteById(it)
        }
        createdPartnerIds.clear()
    }

    private fun createPartner(): UUID {
        val document = UUID.randomUUID().toString().replace("-", "").take(14)
        val result = mockMvc.perform(
            post("/api/v1/b2b/partners")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"Acme Corp","document":"$document"}""")
        ).andReturn()
        val id = UUID.fromString(jsonMapper.readTree(result.response.contentAsString).get("id").stringValue())
        createdPartnerIds.add(id)
        return id
    }

    private fun credit(partnerId: UUID, amount: String, idempotencyKey: String = "key-${UUID.randomUUID()}") =
        mockMvc.perform(
            post("/api/v1/b2b/partners/$partnerId/transactions/credit")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"amount":$amount,"description":"Compra de créditos","idempotencyKey":"$idempotencyKey"}""")
        )

    private fun debit(partnerId: UUID, amount: String, idempotencyKey: String = "key-${UUID.randomUUID()}") =
        mockMvc.perform(
            post("/api/v1/b2b/partners/$partnerId/transactions/debit")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"amount":$amount,"description":"Consumo de créditos","idempotencyKey":"$idempotencyKey"}""")
        )

    private fun trackTransaction(responseBody: String): UUID {
        val id = UUID.fromString(jsonMapper.readTree(responseBody).get("transactionId").stringValue())
        createdTransactionIds.add(id)
        return id
    }

    @Test
    fun `should credit partner balance and return 201 COMPLETED`() {
        val partnerId = createPartner()

        val result = credit(partnerId, "100.00")
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.status").value("COMPLETED"))
            .andReturn()
        trackTransaction(result.response.contentAsString)

        mockMvc.perform(get("/api/v1/b2b/partners/$partnerId/balance"))
            .andExpect(jsonPath("$.availableBalance").value(100.00))
    }

    @Test
    fun `should debit partner balance and return 201 COMPLETED when balance is sufficient`() {
        val partnerId = createPartner()
        trackTransaction(credit(partnerId, "100.00").andReturn().response.contentAsString)

        val result = debit(partnerId, "30.00")
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.status").value("COMPLETED"))
            .andReturn()
        trackTransaction(result.response.contentAsString)

        mockMvc.perform(get("/api/v1/b2b/partners/$partnerId/balance"))
            .andExpect(jsonPath("$.availableBalance").value(70.00))
    }

    @Test
    fun `should return 201 FAILED with errorDescription when balance is insufficient`() {
        val partnerId = createPartner()

        val result = debit(partnerId, "50.00")
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.status").value("FAILED"))
            .andExpect(jsonPath("$.errorDescription").exists())
            .andReturn()
        trackTransaction(result.response.contentAsString)
    }

    @Test
    fun `should return 400 when amount is not positive`() {
        val partnerId = createPartner()

        credit(partnerId, "0").andExpect(status().isBadRequest)
    }

    @Test
    fun `should return 404 when partner does not exist`() {
        credit(UUID.randomUUID(), "10.00").andExpect(status().isNotFound)
    }

    @Test
    fun `should return 409 when replaying the same idempotencyKey`() {
        val partnerId = createPartner()
        val key = "key-replay-${UUID.randomUUID()}"

        val first = credit(partnerId, "10.00", key).andExpect(status().isCreated).andReturn()
        trackTransaction(first.response.contentAsString)

        credit(partnerId, "10.00", key).andExpect(status().isConflict)
    }

    @Test
    fun `should return paginated transaction history`() {
        val partnerId = createPartner()
        trackTransaction(credit(partnerId, "10.00").andReturn().response.contentAsString)

        mockMvc.perform(get("/api/v1/b2b/partners/$partnerId/transactions"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.items.length()").value(1))
    }

    @Test
    fun `should filter transaction history by type`() {
        val partnerId = createPartner()
        trackTransaction(credit(partnerId, "100.00").andReturn().response.contentAsString)
        trackTransaction(debit(partnerId, "10.00").andReturn().response.contentAsString)

        mockMvc.perform(get("/api/v1/b2b/partners/$partnerId/transactions").param("type", "CREDIT"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.items.length()").value(1))
            .andExpect(jsonPath("$.items[0].type").value("CREDIT"))
    }

    @Test
    fun `should filter transaction history by date range`() {
        val partnerId = createPartner()
        trackTransaction(credit(partnerId, "10.00").andReturn().response.contentAsString)
        val today = java.time.LocalDate.now().toString()

        mockMvc.perform(
            get("/api/v1/b2b/partners/$partnerId/transactions")
                .param("from", today)
                .param("to", today)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.items.length()").value(1))
    }

    @Test
    fun `should return 404 for history when partner does not exist`() {
        mockMvc.perform(get("/api/v1/b2b/partners/${UUID.randomUUID()}/transactions"))
            .andExpect(status().isNotFound)
    }

}

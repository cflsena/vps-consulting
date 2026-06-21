package br.com.vps.consulting.b2b.management.partner.infrastructure.api;

import br.com.vps.consulting.b2b.management.RabbitMQTestcontainersConfiguration;
import br.com.vps.consulting.b2b.management.TestcontainersConfiguration;
import br.com.vps.consulting.b2b.management.partner.infrastructure.persistence.jpa.PartnerCreditJpaRepository;
import br.com.vps.consulting.b2b.management.partner.infrastructure.persistence.jpa.PartnerJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Import({TestcontainersConfiguration.class, RabbitMQTestcontainersConfiguration.class})
@AutoConfigureMockMvc
class PartnerControllerIT {

    @Autowired
    MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    PartnerJpaRepository partnerRepo;
    @Autowired
    PartnerCreditJpaRepository creditRepo;

    private final Set<UUID> createdIds = new LinkedHashSet<>();

    @AfterEach
    void cleanup() {
        createdIds.forEach(id -> {
            creditRepo.deleteById(id);
            partnerRepo.deleteById(id);
        });
        createdIds.clear();
    }

    @Test
    @DisplayName("POST /api/v1/b2b/partners → 201 cria parceiro com crédito associado")
    void shouldCreatePartnerAndReturn201() throws Exception {
        final var result = mockMvc.perform(post("/api/v1/b2b/partners")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Acme Corp","document":"%s","creditLimit":5000.00}
                                """.formatted(doc())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();

        final var id = UUID.fromString(
                objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText());
        createdIds.add(id);

        assertThat(partnerRepo.findById(id)).isPresent();
        assertThat(creditRepo.findById(id)).isPresent();
        assertThat(creditRepo.findById(id).get().getCreditLimit()).isEqualByComparingTo("5000.00");
    }

    @Test
    @DisplayName("POST /api/v1/b2b/partners → 400 quando name está vazio")
    void shouldReturn400WhenNameIsBlank() throws Exception {
        mockMvc.perform(post("/api/v1/b2b/partners")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"","document":"%s","creditLimit":1000.00}
                                """.formatted(doc())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("POST /api/v1/b2b/partners → 400 quando creditLimit é nulo")
    void shouldReturn400WhenCreditLimitIsNull() throws Exception {
        mockMvc.perform(post("/api/v1/b2b/partners")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Test","document":"%s","creditLimit":null}
                                """.formatted(doc())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("POST /api/v1/b2b/partners → 409 quando documento já existe")
    void shouldReturn409WhenDocumentDuplicated() throws Exception {
        final var document = doc();
        final var result = mockMvc.perform(post("/api/v1/b2b/partners")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"First","document":"%s","creditLimit":1000.00}
                                """.formatted(document)))
                .andExpect(status().isCreated())
                .andReturn();
        createdIds.add(UUID.fromString(
                objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText()));

        mockMvc.perform(post("/api/v1/b2b/partners")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Second","document":"%s","creditLimit":2000.00}
                                """.formatted(document)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("GET /api/v1/b2b/partners → 200 com estrutura de paginação")
    void shouldListPartnersWithPaginationStructure() throws Exception {
        mockMvc.perform(get("/api/v1/b2b/partners"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageNumber").exists())
                .andExpect(jsonPath("$.pageSize").exists())
                .andExpect(jsonPath("$.totalElements").exists())
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/b2b/partners → 200 retorna parceiro recém-criado na lista")
    void shouldListIncludeCreatedPartner() throws Exception {
        final var id = createPartner("5000.00");

        mockMvc.perform(get("/api/v1/b2b/partners"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[?(@.id == '%s')]".formatted(id)).exists());
    }

    @Test
    @DisplayName("GET /api/v1/b2b/partners/{id}/credit → 200 com dados completos de crédito")
    void shouldReturnCreditData() throws Exception {
        final var id = createPartner("10000.00");

        mockMvc.perform(get("/api/v1/b2b/partners/{id}/credit", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.partnerId").value(id.toString()))
                .andExpect(jsonPath("$.creditLimit").exists())
                .andExpect(jsonPath("$.availableBalance").exists())
                .andExpect(jsonPath("$.reservedBalance").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    @DisplayName("GET /api/v1/b2b/partners/{id}/credit → 404 quando parceiro não existe")
    void shouldReturn404WhenPartnerNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/b2b/partners/{id}/credit", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }


    @Test
    @DisplayName("PATCH /api/v1/b2b/partners/{id}/credit-limit → 204 e novo limite persistido")
    void shouldAdjustCreditLimitAndReturn204() throws Exception {
        final var id = createPartner("1000.00");

        mockMvc.perform(patch("/api/v1/b2b/partners/{id}/credit-limit", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"newCreditLimit":2000.00}
                                """))
                .andExpect(status().isNoContent());

        var credit = creditRepo.findById(id).orElseThrow();
        assertThat(credit.getCreditLimit()).isEqualByComparingTo("2000.00");
        assertThat(credit.getAvailableBalance()).isEqualByComparingTo("2000.00");
    }

    @Test
    @DisplayName("PATCH /api/v1/b2b/partners/{id}/credit-limit → 400 quando body inválido")
    void shouldReturn400WhenCreditLimitBodyInvalid() throws Exception {
        final var id = createPartner("1000.00");

        mockMvc.perform(patch("/api/v1/b2b/partners/{id}/credit-limit", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"newCreditLimit":null}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /api/v1/b2b/partners/{id}/credit-limit → 404 quando parceiro não existe")
    void shouldReturn404WhenPartnerNotFoundForCreditLimit() throws Exception {
        mockMvc.perform(patch("/api/v1/b2b/partners/{id}/credit-limit", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"newCreditLimit":5000.00}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("PATCH /api/v1/b2b/partners/{id}/credit-limit → 422 quando novo limite é inferior ao reservado")
    void shouldReturn422WhenLimitBelowReserved() throws Exception {
        final var id = createPartner("1000.00");
        creditRepo.reserveCredit(id, new BigDecimal("600.00"));

        mockMvc.perform(patch("/api/v1/b2b/partners/{id}/credit-limit", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"newCreditLimit":100.00}
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("PATCH /api/v1/b2b/partners/{id}/available-credit → 204 e saldo atualizado")
    void shouldReplenishAvailableCreditAndReturn204() throws Exception {
        final var id = createPartner("1000.00");
        creditRepo.reserveCredit(id, new BigDecimal("300.00"));
        creditRepo.debitReservation(id, new BigDecimal("300.00"));

        mockMvc.perform(patch("/api/v1/b2b/partners/{id}/available-credit", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount":200.00}
                                """))
                .andExpect(status().isNoContent());

        var credit = creditRepo.findById(id).orElseThrow();
        assertThat(credit.getAvailableBalance()).isEqualByComparingTo("900.00");
    }

    @Test
    @DisplayName("PATCH /api/v1/b2b/partners/{id}/available-credit → 400 quando amount é nulo")
    void shouldReturn400WhenReplenishAmountIsNull() throws Exception {
        final var id = createPartner("1000.00");

        mockMvc.perform(patch("/api/v1/b2b/partners/{id}/available-credit", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount":null}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("PATCH /api/v1/b2b/partners/{id}/available-credit → 404 quando parceiro não existe")
    void shouldReturn404WhenPartnerNotFoundForReplenish() throws Exception {
        mockMvc.perform(patch("/api/v1/b2b/partners/{id}/available-credit", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount":500.00}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private UUID createPartner(String creditLimit) throws Exception {
        final var result = mockMvc.perform(post("/api/v1/b2b/partners")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"IT Partner","document":"%s","creditLimit":%s}
                                """.formatted(doc(), creditLimit)))
                .andExpect(status().isCreated())
                .andReturn();
        final var id = UUID.fromString(
                objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText());
        createdIds.add(id);
        return id;
    }

    private String doc() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 14);
    }
}

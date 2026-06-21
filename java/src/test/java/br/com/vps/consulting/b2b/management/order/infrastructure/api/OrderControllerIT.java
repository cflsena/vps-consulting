package br.com.vps.consulting.b2b.management.order.infrastructure.api;

import br.com.vps.consulting.b2b.management.RabbitMQTestcontainersConfiguration;
import br.com.vps.consulting.b2b.management.TestcontainersConfiguration;
import br.com.vps.consulting.b2b.management.order.infrastructure.persistence.jpa.OrderItemJpaRepository;
import br.com.vps.consulting.b2b.management.order.infrastructure.persistence.jpa.OrderJpaRepository;
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
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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
class OrderControllerIT {

    @Autowired MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired PartnerJpaRepository partnerRepo;
    @Autowired PartnerCreditJpaRepository creditRepo;
    @Autowired OrderJpaRepository orderRepo;
    @Autowired OrderItemJpaRepository orderItemRepo;

    private final List<UUID> createdOrderIds = new ArrayList<>();
    private final List<UUID> createdPartnerIds = new ArrayList<>();

    @AfterEach
    void cleanup() {
        createdOrderIds.forEach(id -> {
            orderItemRepo.deleteAll(orderItemRepo.findAllByOrderId(id, Pageable.unpaged()).getContent());
            orderRepo.deleteById(id);
        });
        createdOrderIds.clear();
        createdPartnerIds.forEach(id -> {
            creditRepo.deleteById(id);
            partnerRepo.deleteById(id);
        });
        createdPartnerIds.clear();
    }

    @Test
    @DisplayName("Given a valid order request, when POST /api/v1/b2b/orders is called, should return 201 and reserve credit")
    void shouldCreateOrderAndReturn201WithCreditReserved() throws Exception {
        final var partnerId = createPartner("1000.00");

        final var result = mockMvc.perform(post("/api/v1/b2b/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"partnerId":"%s","items":[{"productId":"PROD-1","quantity":2,"unitPrice":100.00}]}
                                """.formatted(partnerId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();

        final var orderId = UUID.fromString(
                objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText());
        createdOrderIds.add(orderId);

        final var credit = creditRepo.findById(partnerId).orElseThrow();
        assertThat(credit.getReservedBalance()).isEqualByComparingTo("200.00");
    }

    @Test
    @DisplayName("Given an empty items list, when POST /api/v1/b2b/orders is called, should return 400")
    void shouldReturn400WhenItemsIsEmpty() throws Exception {
        final var partnerId = createPartner("1000.00");

        mockMvc.perform(post("/api/v1/b2b/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"partnerId":"%s","items":[]}
                                """.formatted(partnerId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Given a null partnerId, when POST /api/v1/b2b/orders is called, should return 400")
    void shouldReturn400WhenPartnerIdIsNull() throws Exception {
        mockMvc.perform(post("/api/v1/b2b/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"partnerId":null,"items":[{"productId":"P","quantity":1,"unitPrice":10.00}]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Given a non-existing partner, when POST /api/v1/b2b/orders is called, should return 404")
    void shouldReturn404WhenPartnerNotFound() throws Exception {
        mockMvc.perform(post("/api/v1/b2b/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"partnerId":"%s","items":[{"productId":"P","quantity":1,"unitPrice":10.00}]}
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Given insufficient credit, when POST /api/v1/b2b/orders is called, should return 422")
    void shouldReturn422WhenInsufficientCredit() throws Exception {
        final var partnerId = createPartner("100.00");

        mockMvc.perform(post("/api/v1/b2b/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"partnerId":"%s","items":[{"productId":"PROD-X","quantity":10,"unitPrice":50.00}]}
                                """.formatted(partnerId)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Given a request, when GET /api/v1/b2b/orders is called, should return 200 with the pagination structure")
    void shouldListOrdersAndReturn200() throws Exception {
        mockMvc.perform(get("/api/v1/b2b/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageNumber").exists())
                .andExpect(jsonPath("$.pageSize").exists())
                .andExpect(jsonPath("$.totalElements").exists())
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    @DisplayName("Given a status=PENDING filter, when GET /api/v1/b2b/orders is called, should return only PENDING orders")
    void shouldListOrdersFilteredByStatus() throws Exception {
        final var partnerId = createPartner("1000.00");
        final var orderId = createOrder(partnerId, "50.00");

        mockMvc.perform(get("/api/v1/b2b/orders").param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[?(@.id == '%s')]".formatted(orderId)).exists())
                .andExpect(jsonPath("$.items[?(@.status != 'PENDING')]").doesNotExist());
    }

    @Test
    @DisplayName("Given a partnerId filter, when GET /api/v1/b2b/orders is called, should return the partner's orders")
    void shouldListOrdersFilteredByPartnerId() throws Exception {
        final var partnerId = createPartner("1000.00");
        final var orderId = createOrder(partnerId, "50.00");

        mockMvc.perform(get("/api/v1/b2b/orders").param("partnerId", partnerId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[?(@.id == '%s')]".formatted(orderId)).exists())
                .andExpect(jsonPath("$.items[?(@.partnerId != '%s')]".formatted(partnerId)).doesNotExist());
    }

    @Test
    @DisplayName("Given a from/to filter set to today, when GET /api/v1/b2b/orders is called, should return the order created today")
    void shouldListOrdersFilteredByDateRange() throws Exception {
        final var partnerId = createPartner("1000.00");
        final var orderId = createOrder(partnerId, "50.00");
        final var today = LocalDate.now().toString();

        mockMvc.perform(get("/api/v1/b2b/orders").param("from", today).param("to", today))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[?(@.id == '%s')]".formatted(orderId)).exists());
    }

    @Test
    @DisplayName("Given an existing order, when GET /api/v1/b2b/orders/{id} is called, should return 200 with all order fields")
    void shouldFindOrderByIdAndReturn200() throws Exception {
        final var partnerId = createPartner("1000.00");
        final var orderId = createOrder(partnerId, "75.00");

        mockMvc.perform(get("/api/v1/b2b/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId.toString()))
                .andExpect(jsonPath("$.partnerId").value(partnerId.toString()))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalAmount").exists())
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    @DisplayName("Given a non-existing order, when GET /api/v1/b2b/orders/{id} is called, should return 404")
    void shouldReturn404WhenOrderNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/b2b/orders/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Given an existing order, when GET /api/v1/b2b/orders/{id}/items is called, should return 200 with the order items")
    void shouldListOrderItemsAndReturn200() throws Exception {
        final var partnerId = createPartner("1000.00");
        final var orderId = createOrder(partnerId, "100.00");

        mockMvc.perform(get("/api/v1/b2b/orders/{id}/items", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0]").exists())
                .andExpect(jsonPath("$.items[1]").doesNotExist());
    }

    @Test
    @DisplayName("Given a PENDING order, when PATCH /api/v1/b2b/orders/{id}/status to APPROVED is called, should return 204")
    void shouldUpdateOrderStatusToApprovedAndReturn204() throws Exception {
        var partnerId = createPartner("1000.00");
        var orderId = createOrder(partnerId, "100.00");

        mockMvc.perform(patch("/api/v1/b2b/orders/{id}/status", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"targetStatus":"APPROVED"}
                                """))
                .andExpect(status().isNoContent());

        assertThat(orderRepo.findById(orderId).orElseThrow().getStatus().name()).isEqualTo("APPROVED");
    }

    @Test
    @DisplayName("Given an invalid status transition, when PATCH /api/v1/b2b/orders/{id}/status is called, should return 422")
    void shouldReturn422OnInvalidStatusTransition() throws Exception {
        final var partnerId = createPartner("1000.00");
        final var orderId = createOrder(partnerId, "100.00");

        mockMvc.perform(patch("/api/v1/b2b/orders/{id}/status", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"targetStatus":"DELIVERED"}
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Given a non-existing order, when PATCH /api/v1/b2b/orders/{id}/status is called, should return 404")
    void shouldReturn404WhenOrderNotFoundForStatusUpdate() throws Exception {
        mockMvc.perform(patch("/api/v1/b2b/orders/{id}/status", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"targetStatus":"APPROVED"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }


    @Test
    @DisplayName("Given an order going through PENDING→APPROVED→IN_PROCESS→SENT→DELIVERED, when each transition is applied, should debit credit correctly")
    void e2eFullLifecycleDebitsCredit() throws Exception {
        var partnerId = createPartner("1000.00");
        var orderId = createOrder(partnerId, "200.00");

        assertCredit(partnerId, "1000.00", "200.00");

        updateStatus(orderId, "APPROVED");
        assertCredit(partnerId, "800.00", "0.00");

        updateStatus(orderId, "IN_PROCESS");
        assertCredit(partnerId, "800.00", "0.00");

        updateStatus(orderId, "SENT");
        assertCredit(partnerId, "800.00", "0.00");

        updateStatus(orderId, "DELIVERED");
        assertCredit(partnerId, "800.00", "0.00");

        assertThat(orderRepo.findById(orderId).orElseThrow().getStatus().name()).isEqualTo("DELIVERED");
    }

    @Test
    @DisplayName("Given a PENDING order, when transitioned to CANCELED, should release the reserved credit")
    void e2ePendingCancelReleasesCredit() throws Exception {
        final var partnerId = createPartner("1000.00");
        final var orderId = createOrder(partnerId, "300.00");

        assertCredit(partnerId, "1000.00", "300.00");

        updateStatus(orderId, "CANCELED");

        final var credit = creditRepo.findById(partnerId).orElseThrow();
        assertThat(credit.getAvailableBalance()).isEqualByComparingTo("1000.00");
        assertThat(credit.getReservedBalance()).isEqualByComparingTo("0.00");
        assertThat(orderRepo.findById(orderId).orElseThrow().getStatus().name()).isEqualTo("CANCELED");
    }

    @Test
    @DisplayName("Given an APPROVED order, when transitioned to CANCELED, should refund the debited credit")
    void e2eApprovedCancelRefundsCredit() throws Exception {
        final var partnerId = createPartner("1000.00");
        final var orderId = createOrder(partnerId, "500.00");

        updateStatus(orderId, "APPROVED");
        assertCredit(partnerId, "500.00", "0.00");

        updateStatus(orderId, "CANCELED");

        final var credit = creditRepo.findById(partnerId).orElseThrow();
        assertThat(credit.getAvailableBalance()).isEqualByComparingTo("1000.00");
        assertThat(credit.getReservedBalance()).isEqualByComparingTo("0.00");
    }

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
        createdPartnerIds.add(id);
        return id;
    }

    private UUID createOrder(UUID partnerId, String unitPrice) throws Exception {
        final var result = mockMvc.perform(post("/api/v1/b2b/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"partnerId":"%s","items":[{"productId":"PROD-1","quantity":1,"unitPrice":%s}]}
                                """.formatted(partnerId, unitPrice)))
                .andExpect(status().isCreated())
                .andReturn();
        final var id = UUID.fromString(
                objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText());
        createdOrderIds.add(id);
        return id;
    }

    private void updateStatus(UUID orderId, String status) throws Exception {
        mockMvc.perform(patch("/api/v1/b2b/orders/{id}/status", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"targetStatus":"%s"}
                                """.formatted(status)))
                .andExpect(status().isNoContent());
    }

    private void assertCredit(UUID partnerId, String expectedAvailable, String expectedReserved) {
        var credit = creditRepo.findById(partnerId).orElseThrow();
        assertThat(credit.getAvailableBalance()).isEqualByComparingTo(expectedAvailable);
        assertThat(credit.getReservedBalance()).isEqualByComparingTo(expectedReserved);
    }

    private String doc() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 14);
    }
}

package br.com.vps.consulting.b2b.management.order.infrastructure.api;

import br.com.vps.consulting.b2b.management.RabbitMQTestcontainersConfiguration;
import br.com.vps.consulting.b2b.management.TestcontainersConfiguration;
import br.com.vps.consulting.b2b.management.order.infrastructure.persistence.jpa.OrderItemJpaRepository;
import br.com.vps.consulting.b2b.management.order.infrastructure.persistence.jpa.OrderJpaRepository;
import br.com.vps.consulting.b2b.management.partner.infrastructure.persistence.PartnerCreditEntity;
import br.com.vps.consulting.b2b.management.partner.infrastructure.persistence.PartnerEntity;
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@Import({TestcontainersConfiguration.class, RabbitMQTestcontainersConfiguration.class})
@AutoConfigureMockMvc
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class OrderStatusConcurrencyIT {

    @Autowired MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired PartnerJpaRepository partnerRepo;
    @Autowired PartnerCreditJpaRepository creditRepo;
    @Autowired OrderJpaRepository orderRepo;
    @Autowired OrderItemJpaRepository orderItemRepo;

    private final List<UUID> createdOrderIds = new CopyOnWriteArrayList<>();
    private final List<UUID> createdPartnerIds = new CopyOnWriteArrayList<>();

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
    @DisplayName("Given 50 independent orders, when their status transitions are triggered concurrently, should all succeed")
    void shouldHandle50ConcurrentStatusTransitionsForIndependentOrders() throws InterruptedException {
        final int count = 50;
        final var unitPrice = new BigDecimal("100.00");

        final var orderIds = new ArrayList<UUID>();
        for (int i = 0; i < count; i++) {
            var pid = setupPartner(new BigDecimal("500.00"));
            var oid = createOrder(pid, unitPrice);
            orderIds.add(oid);
        }

        final var successCount = new AtomicInteger(0);
        final var latch = new CountDownLatch(count);
        final var startGate = new CountDownLatch(1);
        final var executor = Executors.newFixedThreadPool(count);

        for (UUID oid : orderIds) {
            executor.submit(() -> {
                try {
                    startGate.await();
                    var result = mockMvc.perform(patch("/api/v1/b2b/orders/{id}/status", oid)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{\"targetStatus\":\"APPROVED\"}"))
                            .andReturn();
                    if (result.getResponse().getStatus() == 204) {
                        successCount.incrementAndGet();
                    }
                } catch (Exception ignored) {
                } finally {
                    latch.countDown();
                }
            });
        }

        startGate.countDown();
        assertThat(latch.await(60, TimeUnit.SECONDS)).isTrue();
        executor.shutdown();

        assertThat(successCount.get()).isEqualTo(count);

        for (UUID oid : orderIds) {
            var order = orderRepo.findById(oid).orElseThrow();
            assertThat(order.getStatus().name()).isEqualTo("APPROVED");
        }

        for (UUID pid : createdPartnerIds) {
            final var credit = creditRepo.findById(pid).orElseThrow();
            assertThat(credit.getAvailableBalance()).isEqualByComparingTo("400.00");
            assertThat(credit.getReservedBalance()).isEqualByComparingTo("0.00");
        }
    }

    @Test
    @DisplayName("Given 50 concurrent attempts to cancel the same order, when executed, should succeed only once and not duplicate the credit release")
    void shouldPreventDoubleReleaseOnConcurrentCancelOfSameOrder() throws InterruptedException {
        final int totalAttempts = 50;
        final var creditLimit = new BigDecimal("1000.00");
        final var orderAmount = new BigDecimal("500.00");

        final var pid = setupPartner(creditLimit);
        final var oid = createOrder(pid, orderAmount);

        final var successCount = new AtomicInteger(0);
        final var failCount = new AtomicInteger(0);
        final var latch = new CountDownLatch(totalAttempts);
        final var startGate = new CountDownLatch(1);
        final var executor = Executors.newFixedThreadPool(totalAttempts);

        for (int i = 0; i < totalAttempts; i++) {
            executor.submit(() -> {
                try {
                    startGate.await();
                    final var result = mockMvc.perform(patch("/api/v1/b2b/orders/{id}/status", oid)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{\"targetStatus\":\"CANCELED\"}"))
                            .andReturn();
                    if (result.getResponse().getStatus() == 204) {
                        successCount.incrementAndGet();
                    } else {
                        failCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        startGate.countDown();
        assertThat(latch.await(60, TimeUnit.SECONDS)).isTrue();
        executor.shutdown();

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(successCount.get() + failCount.get()).isEqualTo(totalAttempts);

        final var order = orderRepo.findById(oid).orElseThrow();
        assertThat(order.getStatus().name()).isEqualTo("CANCELED");

        final var credit = creditRepo.findById(pid).orElseThrow();
        assertThat(credit.getReservedBalance()).isEqualByComparingTo("0.00");
        assertThat(credit.getAvailableBalance()).isEqualByComparingTo(creditLimit);
    }

    private UUID setupPartner(BigDecimal creditLimit) {
        final var id = UUID.randomUUID();
        partnerRepo.save(PartnerEntity.builder()
                .id(id)
                .name("Status Concurrency Partner")
                .document(UUID.randomUUID().toString().replace("-", "").substring(0, 14))
                .createdAt(Instant.now())
                .build());
        creditRepo.save(PartnerCreditEntity.builder()
                .id(id)
                .creditLimit(creditLimit)
                .availableBalance(creditLimit)
                .reservedBalance(BigDecimal.ZERO)
                .updatedAt(Instant.now())
                .build());
        createdPartnerIds.add(id);
        return id;
    }

    private UUID createOrder(UUID partnerId, BigDecimal unitPrice) {
        try {
            final var result = mockMvc.perform(post("/api/v1/b2b/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"partnerId":"%s","items":[{"productId":"PROD-SC","quantity":1,"unitPrice":%s}]}
                                    """.formatted(partnerId, unitPrice).strip()))
                    .andReturn();
            assertThat(result.getResponse().getStatus()).isEqualTo(201);
            final var id = UUID.fromString(
                    objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText());
            createdOrderIds.add(id);
            return id;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

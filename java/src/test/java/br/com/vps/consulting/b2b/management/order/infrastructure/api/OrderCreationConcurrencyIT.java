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
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@Import({TestcontainersConfiguration.class, RabbitMQTestcontainersConfiguration.class})
@AutoConfigureMockMvc
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class OrderCreationConcurrencyIT {

    @Autowired MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired PartnerJpaRepository partnerRepo;
    @Autowired PartnerCreditJpaRepository creditRepo;
    @Autowired OrderJpaRepository orderRepo;
    @Autowired OrderItemJpaRepository orderItemRepo;

    private final CopyOnWriteArrayList<UUID> createdOrderIds = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<UUID> createdPartnerIds = new CopyOnWriteArrayList<>();

    @AfterEach
    void cleanup() {
        createdOrderIds.forEach(id -> {
            orderItemRepo.deleteAll(orderItemRepo.findByOrderId(id, Pageable.unpaged()).getContent());
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
    @DisplayName("Nenhum overdraft sob 200 tentativas concorrentes de criação de pedido para o mesmo parceiro")
    void shouldNotExceedCreditLimitUnder200ConcurrentOrders() throws InterruptedException {
        final var creditLimit = new BigDecimal("1000.00");
        final var unitPrice = new BigDecimal("10.00");
        final int totalAttempts = 200;
        final int threads = 50;

        final var pid = setupPartner(creditLimit);

        final var successCount = new AtomicInteger(0);
        final var failCount = new AtomicInteger(0);
        final var latch = new CountDownLatch(totalAttempts);
        final var startGate = new CountDownLatch(1);
        final var executor = Executors.newFixedThreadPool(threads);

        final var body = """
                {"partnerId":"%s","items":[{"productId":"PROD-CC","quantity":1,"unitPrice":%s}]}
                """.formatted(pid, unitPrice).strip();

        for (int i = 0; i < totalAttempts; i++) {
            executor.submit(() -> {
                try {
                    startGate.await();
                    var result = mockMvc.perform(post("/api/v1/b2b/orders")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(body))
                            .andReturn();
                    if (result.getResponse().getStatus() == 201) {
                        var id = UUID.fromString(
                                objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText());
                        createdOrderIds.add(id);
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

        final var credit = creditRepo.findById(pid).orElseThrow();
        final var maxExpectedSuccess = creditLimit.divide(unitPrice, 0, RoundingMode.DOWN).intValue();

        assertThat(successCount.get()).isLessThanOrEqualTo(maxExpectedSuccess);
        assertThat(successCount.get() + failCount.get()).isEqualTo(totalAttempts);
        assertThat(credit.getAvailableBalance()).isGreaterThanOrEqualTo(BigDecimal.ZERO);
        assertThat(credit.getReservedBalance()).isGreaterThanOrEqualTo(BigDecimal.ZERO);
        assertThat(credit.getReservedBalance()).isLessThanOrEqualTo(creditLimit);
        assertThat(credit.getReservedBalance())
                .isEqualByComparingTo(unitPrice.multiply(BigDecimal.valueOf(successCount.get())));
    }

    @Test
    @DisplayName("Pedidos concorrentes para parceiros distintos são independentes e todos aprovados")
    void shouldHandleConcurrentOrderCreationForDifferentPartners() throws InterruptedException {
        final int partnerCount = 20;
        final var unitPrice = new BigDecimal("100.00");
        final var creditLimit = new BigDecimal("500.00");

        var partnerIds = new CopyOnWriteArrayList<UUID>();
        for (int i = 0; i < partnerCount; i++) {
            partnerIds.add(setupPartner(creditLimit));
        }

        final var successCount = new AtomicInteger(0);
        final var latch = new CountDownLatch(partnerCount);
        final var startGate = new CountDownLatch(1);
        final var executor = Executors.newFixedThreadPool(partnerCount);

        for (UUID pid : partnerIds) {
            var body = """
                    {"partnerId":"%s","items":[{"productId":"PROD-CC","quantity":1,"unitPrice":%s}]}
                    """.formatted(pid, unitPrice).strip();
            executor.submit(() -> {
                try {
                    startGate.await();
                    final var result = mockMvc.perform(post("/api/v1/b2b/orders")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(body))
                            .andReturn();
                    if (result.getResponse().getStatus() == 201) {
                        final var id = UUID.fromString(
                                objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText());
                        createdOrderIds.add(id);
                        successCount.incrementAndGet();
                    }
                } catch (Exception ignored) {
                } finally {
                    latch.countDown();
                }
            });
        }

        startGate.countDown();
        assertThat(latch.await(30, TimeUnit.SECONDS)).isTrue();
        executor.shutdown();

        assertThat(successCount.get()).isEqualTo(partnerCount);

        for (UUID pid : partnerIds) {
            final var credit = creditRepo.findById(pid).orElseThrow();
            assertThat(credit.getReservedBalance()).isEqualByComparingTo("100.00");
            assertThat(credit.getAvailableBalance()).isEqualByComparingTo("500.00");
        }
    }

    private UUID setupPartner(BigDecimal creditLimit) {
        final var id = UUID.randomUUID();
        partnerRepo.save(PartnerEntity.builder()
                .id(id)
                .name("Concurrency Partner")
                .document(UUID.randomUUID().toString().replace("-", "").substring(0, 14))
                .createdAt(Instant.now())
                .build());
        creditRepo.save(PartnerCreditEntity.builder()
                .partnerId(id)
                .creditLimit(creditLimit)
                .availableBalance(creditLimit)
                .reservedBalance(BigDecimal.ZERO)
                .updatedAt(Instant.now())
                .build());
        createdPartnerIds.add(id);
        return id;
    }
}

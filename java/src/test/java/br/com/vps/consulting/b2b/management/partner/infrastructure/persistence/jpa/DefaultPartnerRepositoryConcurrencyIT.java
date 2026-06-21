package br.com.vps.consulting.b2b.management.partner.infrastructure.persistence.jpa;

import br.com.vps.consulting.b2b.management.TestcontainersConfiguration;
import br.com.vps.consulting.b2b.management.partner.domain.PartnerId;
import br.com.vps.consulting.b2b.management.partner.infrastructure.persistence.DefaultPartnerRepository;
import br.com.vps.consulting.b2b.management.partner.infrastructure.persistence.PartnerCreditEntity;
import br.com.vps.consulting.b2b.management.partner.infrastructure.persistence.PartnerEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, DefaultPartnerRepository.class})
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class DefaultPartnerRepositoryConcurrencyIT {

    @Autowired private DefaultPartnerRepository adapter;
    @Autowired private PartnerJpaRepository partnerJpaRepository;
    @Autowired private PartnerCreditJpaRepository partnerCreditJpaRepository;

    private UUID partnerId;

    @AfterEach
    void cleanup() {
        if (partnerId != null) {
            partnerCreditJpaRepository.deleteById(partnerId);
            partnerJpaRepository.deleteById(partnerId);
        }
    }

    @Test
    @DisplayName("Given 200 concurrent reservation attempts, when executed, should not allow overdraft")
    void shouldNotOverdraftUnderConcurrentReservations() throws InterruptedException {
        final var limit = new BigDecimal("1000.00");
        final var amountPerRequest = new BigDecimal("10.00");
        final int totalAttempts = 200;

        partnerId = setupPartner(limit);
        final var pid = PartnerId.from(partnerId);

        final var successCount = new AtomicInteger(0);
        final var latch = new CountDownLatch(totalAttempts);
        final var executor = Executors.newFixedThreadPool(50);

        for (int i = 0; i < totalAttempts; i++) {
            executor.submit(() -> {
                try {
                    if (adapter.reserveCredit(pid, amountPerRequest)) {
                        successCount.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        assertThat(latch.await(30, TimeUnit.SECONDS)).isTrue();
        executor.shutdown();

        final var credit = partnerCreditJpaRepository.findById(partnerId).orElseThrow();

        assertThat(credit.getAvailableBalance()).isGreaterThanOrEqualTo(BigDecimal.ZERO);
        assertThat(credit.getReservedBalance()).isLessThanOrEqualTo(limit);
        assertThat(credit.getReservedBalance())
                .isEqualByComparingTo(amountPerRequest.multiply(BigDecimal.valueOf(successCount.get())));
        assertThat(successCount.get())
                .isLessThanOrEqualTo(limit.divide(amountPerRequest, 0, RoundingMode.DOWN).intValue());

    }

    private UUID setupPartner(final BigDecimal creditLimit) {
        final var id = UUID.randomUUID();
        partnerJpaRepository.save(PartnerEntity.builder()
                .id(id)
                .name("Concurrency Test Partner")
                .document(UUID.randomUUID().toString().replace("-", "").substring(0, 14))
                .createdAt(Instant.now())
                .build());
        partnerCreditJpaRepository.save(PartnerCreditEntity.builder()
                .id(id)
                .creditLimit(creditLimit)
                .availableBalance(creditLimit)
                .reservedBalance(BigDecimal.ZERO)
                .updatedAt(Instant.now())
                .build());
        return id;
    }
}

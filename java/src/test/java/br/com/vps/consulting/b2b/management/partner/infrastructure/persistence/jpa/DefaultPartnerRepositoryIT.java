package br.com.vps.consulting.b2b.management.partner.infrastructure.persistence.jpa;

import br.com.vps.consulting.b2b.management.TestcontainersConfiguration;
import br.com.vps.consulting.b2b.management.partner.domain.PartnerId;
import br.com.vps.consulting.b2b.management.partner.infrastructure.persistence.DefaultPartnerCreditRepository;
import br.com.vps.consulting.b2b.management.partner.infrastructure.persistence.PartnerCreditEntity;
import br.com.vps.consulting.b2b.management.partner.infrastructure.persistence.PartnerEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, DefaultPartnerCreditRepository.class})
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
class DefaultPartnerRepositoryIT {

    @Autowired private DefaultPartnerCreditRepository adapter;
    @Autowired private PartnerJpaRepository partnerJpaRepository;
    @Autowired private PartnerCreditJpaRepository partnerCreditJpaRepository;

    @Test
    @DisplayName("Given a sufficient free balance, when reserveCredit is called, should reserve the credit")
    void shouldReserveCredit_whenFreeBalanceSufficient() {
        final var partnerId = setup("1000.00", "0.00");

        final var result = adapter.reserveCredit(PartnerId.from(partnerId), new BigDecimal("300.00"));

        assertThat(result).isTrue();
        final var credit = findCredit(partnerId);
        assertThat(credit.getReservedBalance()).isEqualByComparingTo("300.00");
        assertThat(credit.getAvailableBalance()).isEqualByComparingTo("1000.00");
    }

    @Test
    @DisplayName("Given an amount that exactly equals the free balance, when reserveCredit is called, should reserve the credit")
    void shouldReserveCredit_whenAmountEqualsExactFreeBalance() {
        final var partnerId = setup("1000.00", "700.00");

        final var result = adapter.reserveCredit(PartnerId.from(partnerId), new BigDecimal("300.00"));

        assertThat(result).isTrue();
        final var credit = findCredit(partnerId);
        assertThat(credit.getReservedBalance()).isEqualByComparingTo("1000.00");
    }

    @Test
    @DisplayName("Given an insufficient free balance, when reserveCredit is called, should not reserve the credit")
    void shouldNotReserveCredit_whenFreeBalanceInsufficient() {
        final var partnerId = setup("1000.00", "800.00");

        final var result = adapter.reserveCredit(PartnerId.from(partnerId), new BigDecimal("300.00"));

        assertThat(result).isFalse();
        final var credit = findCredit(partnerId);
        assertThat(credit.getReservedBalance()).isEqualByComparingTo("800.00");
    }

    @Test
    @DisplayName("Given all balance already reserved, when reserveCredit is called, should not reserve any more credit")
    void shouldNotReserveCredit_whenNoFreeBalance() {
        final var partnerId = setup("500.00", "500.00");

        final var result = adapter.reserveCredit(PartnerId.from(partnerId), new BigDecimal("1.00"));

        assertThat(result).isFalse();
        assertThat(findCredit(partnerId).getReservedBalance()).isEqualByComparingTo("500.00");
    }

    @Test
    @DisplayName("Given a reserved balance, when debitReservation is called, should convert it into a real debit")
    void shouldDebitReservation_whenCalled() {
        final var partnerId = setup("1000.00", "300.00");

        adapter.debitReservation(PartnerId.from(partnerId), new BigDecimal("300.00"));

        final var credit = findCredit(partnerId);
        assertThat(credit.getAvailableBalance()).isEqualByComparingTo("700.00");
        assertThat(credit.getReservedBalance()).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("Given an amount above the available balance, when debitReservation is called, should throw DataIntegrityViolationException")
    void shouldThrow_whenDebitReservationViolatesConstraint() {
        final var partnerId = setup("100.00", "100.00");

        assertThatThrownBy(() -> adapter.debitReservation(PartnerId.from(partnerId), new BigDecimal("200.00")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    // ── releaseReservation ───────────────────────────────────────────────────

    @Test
    @DisplayName("Given a reserved balance, when releaseReservation is called, should reduce it without touching the available balance")
    void shouldReleaseReservation_whenCalled() {
        final var partnerId = setup("1000.00", "300.00");

        adapter.releaseReservation(PartnerId.from(partnerId), new BigDecimal("300.00"));

        final var credit = findCredit(partnerId);
        assertThat(credit.getReservedBalance()).isEqualByComparingTo("0.00");
        assertThat(credit.getAvailableBalance()).isEqualByComparingTo("1000.00");
    }

    @Test
    @DisplayName("Given an amount above the reserved balance, when releaseReservation is called, should throw DataIntegrityViolationException")
    void shouldThrow_whenReleaseReservationViolatesConstraint() {
        final var partnerId = setup("1000.00", "100.00");

        assertThatThrownBy(() -> adapter.releaseReservation(PartnerId.from(partnerId), new BigDecimal("200.00")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    // ── refundCredit ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("Given a debited amount, when refundCredit is called, should restore the available balance")
    void shouldRefundCredit_whenCalled() {
        final var partnerId = setup("700.00", "0.00");

        adapter.refundCredit(PartnerId.from(partnerId), new BigDecimal("300.00"));

        final var credit = findCredit(partnerId);
        assertThat(credit.getAvailableBalance()).isEqualByComparingTo("1000.00");
        assertThat(credit.getReservedBalance()).isEqualByComparingTo("0.00");
    }

    // ── Ciclos completos ──────────────────────────────────────────────────────

    @Test
    @DisplayName("Given a reserve followed by a debit, when the create-to-approve cycle completes, should leave the net credit consumed")
    void shouldCompleteCycle_createThenApprove() {
        final var partnerId = setup("1000.00", "0.00");
        final var pid = PartnerId.from(partnerId);

        adapter.reserveCredit(pid, new BigDecimal("300.00"));
        adapter.debitReservation(pid, new BigDecimal("300.00"));

        final var credit = findCredit(partnerId);
        assertThat(credit.getAvailableBalance()).isEqualByComparingTo("700.00");
        assertThat(credit.getReservedBalance()).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("Given a reserve followed by a release, when the create-to-cancel-pending cycle completes, should leave the balance unchanged")
    void shouldCompleteCycle_createThenCancelPending() {
        final var partnerId = setup("1000.00", "0.00");
        final var pid = PartnerId.from(partnerId);

        adapter.reserveCredit(pid, new BigDecimal("300.00"));
        adapter.releaseReservation(pid, new BigDecimal("300.00"));

        final var credit = findCredit(partnerId);
        assertThat(credit.getAvailableBalance()).isEqualByComparingTo("1000.00");
        assertThat(credit.getReservedBalance()).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("Given a reserve, debit and refund, when the create-to-approve-to-cancel cycle completes, should leave the balance unchanged")
    void shouldCompleteCycle_createApproveCancel() {
        final var partnerId = setup("1000.00", "0.00");
        final var pid = PartnerId.from(partnerId);

        adapter.reserveCredit(pid, new BigDecimal("300.00"));
        adapter.debitReservation(pid, new BigDecimal("300.00"));
        adapter.refundCredit(pid, new BigDecimal("300.00"));

        final var credit = findCredit(partnerId);
        assertThat(credit.getAvailableBalance()).isEqualByComparingTo("1000.00");
        assertThat(credit.getReservedBalance()).isEqualByComparingTo("0.00");
    }

    private UUID setup(final String availableBalance, final String reservedBalance) {
        final var partnerId = UUID.randomUUID();
        partnerJpaRepository.save(PartnerEntity.builder()
                .id(partnerId)
                .name("Test Partner")
                .document(UUID.randomUUID().toString().replace("-", "").substring(0, 14))
                .createdAt(Instant.now())
                .build());
        partnerCreditJpaRepository.save(PartnerCreditEntity.builder()
                .id(partnerId)
                .creditLimit(new BigDecimal("1000.00"))
                .availableBalance(new BigDecimal(availableBalance))
                .reservedBalance(new BigDecimal(reservedBalance))
                .updatedAt(Instant.now())
                .build());
        return partnerId;
    }

    private PartnerCreditEntity findCredit(final UUID partnerId) {
        return partnerCreditJpaRepository.findById(partnerId).orElseThrow();
    }

}

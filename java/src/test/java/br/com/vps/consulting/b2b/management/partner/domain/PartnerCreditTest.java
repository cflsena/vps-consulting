package br.com.vps.consulting.b2b.management.partner.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PartnerCreditTest {

    @Test
    @DisplayName("Given valid credit data, when constructing PartnerCredit, should create with all fields")
    void shouldCreateWithValidData() {
        final var id = UUID.randomUUID();
        final var credit = new PartnerCredit(id, new BigDecimal("10000.00"), new BigDecimal("7500.00"), new BigDecimal("2500.00"), Instant.now());

        assertThat(credit.getId().value()).isEqualTo(id);
        assertThat(credit.getCreditLimit().value()).isEqualByComparingTo("10000.00");
        assertThat(credit.getAvailableBalance().value()).isEqualByComparingTo("7500.00");
        assertThat(credit.getReservedBalance().value()).isEqualByComparingTo("2500.00");
    }

    @Test
    @DisplayName("Given null availableBalance, when constructing PartnerCredit, should default to Money.zero()")
    void shouldDefaultAvailableBalanceToZero() {
        final var id = UUID.randomUUID();
        final var credit = new PartnerCredit(id, new BigDecimal("10000.00"), null, null, Instant.now());

        assertThat(credit.getAvailableBalance().isZero()).isTrue();
    }

    @Test
    @DisplayName("Given null reservedBalance, when constructing PartnerCredit, should default to Money.zero()")
    void shouldDefaultReservedBalanceToZero() {
        final var id = UUID.randomUUID();
        final var credit = new PartnerCredit(id, new BigDecimal("10000.00"), new BigDecimal("10000.00"), null, Instant.now());

        assertThat(credit.getReservedBalance().isZero()).isTrue();
    }

    @Test
    @DisplayName("Given null updatedAt, when constructing PartnerCredit, should set the current time")
    void shouldSetCurrentTimeWhenUpdatedAtIsNull() {
        final var before = Instant.now();
        final var id = UUID.randomUUID();
        final var credit = new PartnerCredit(id, new BigDecimal("10000.00"), new BigDecimal("10000.00"), BigDecimal.ZERO, null);

        assertThat(credit.getUpdatedAt()).isAfterOrEqualTo(before);
    }

    @Test
    @DisplayName("Given a null id, when constructing PartnerCredit, should reject it with NullPointerException")
    void shouldRejectNullId() {
        assertThrows(NullPointerException.class, () -> new PartnerCredit(null, new BigDecimal("10000.00"), new BigDecimal("10000.00"), BigDecimal.ZERO, Instant.now()));
    }

    @Test
    @DisplayName("Given a null creditLimit, when constructing PartnerCredit, should reject it with NullPointerException")
    void shouldRejectNullCreditLimit() {
        final var id = UUID.randomUUID();
        assertThrows(NullPointerException.class, () -> new PartnerCredit(id, null, new BigDecimal("10000.00"), BigDecimal.ZERO, Instant.now()));
    }

    @Test
    @DisplayName("Given a negative creditLimit, when constructing PartnerCredit, should throw IllegalArgumentException")
    void shouldRejectNegativeCreditLimit() {
        final var id = UUID.randomUUID();
        assertThatThrownBy(() -> new PartnerCredit(id, new BigDecimal("-1.00"), new BigDecimal("10000.00"), BigDecimal.ZERO, Instant.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be negative");
    }

    @Test
    @DisplayName("Given a valid new limit, when adjustCreditLimit is called, should update the credit limit")
    void shouldAdjustCreditLimit() {
        final var id = UUID.randomUUID();
        final var credit = new PartnerCredit(id, new BigDecimal("10000.00"), new BigDecimal("10000.00"), BigDecimal.ZERO, Instant.now());

        assertDoesNotThrow(() -> credit.adjustCreditLimit(new BigDecimal("25000.00")));
        assertThat(credit.getCreditLimit().value()).isEqualByComparingTo("25000.00");
    }

    @Test
    @DisplayName("Given a null value, when adjustCreditLimit is called, should reject it with NullPointerException")
    void shouldRejectNullInAdjustCreditLimit() {
        final var id = UUID.randomUUID();
        final var credit = new PartnerCredit(id, new BigDecimal("10000.00"), new BigDecimal("10000.00"), BigDecimal.ZERO, Instant.now());

        assertThrows(NullPointerException.class, () -> credit.adjustCreditLimit(null));
    }

    @Test
    @DisplayName("Given a negative value, when adjustCreditLimit is called, should throw IllegalArgumentException")
    void shouldRejectNegativeValueInAdjustCreditLimit() {
        final var id = UUID.randomUUID();
        final var credit = new PartnerCredit(id, new BigDecimal("10000.00"), new BigDecimal("10000.00"), BigDecimal.ZERO, Instant.now());

        assertThatThrownBy(() -> credit.adjustCreditLimit(new BigDecimal("-500.00")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be negative");
    }
}

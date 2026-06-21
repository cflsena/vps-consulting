package br.com.vps.consulting.b2b.management.partner.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PartnerTest {

    @Test
    @DisplayName("Should create partner with provided id")
    void shouldCreateWithProvidedId() {
        final var id = PartnerId.generate();
        final var partner = newPartner(id);
        assertThat(partner.getId()).isEqualTo(id);
    }

    @Test
    @DisplayName("Should generate id when null is provided")
    void shouldGenerateIdWhenNull() {
        final var partner = Partner.builder()
                .id(null)
                .name("Acme Corp")
                .document("12345678000100")
                .creditLimit(new BigDecimal("10000.00"))
                .build();
        assertThat(partner.getId()).isNotNull();
    }

    @Test
    @DisplayName("Should set current time as createdAt when null is provided")
    void shouldSetCurrentTimeWhenCreatedAtIsNull() {
        final var before = Instant.now();
        final var partner = Partner.builder()
                .name("Acme Corp")
                .document("12345678000100")
                .creditLimit(new BigDecimal("5000.00"))
                .createdAt(null)
                .build();
        assertThat(partner.getCreatedAt()).isAfterOrEqualTo(before);
    }

    @Test
    @DisplayName("Should store creditLimit as Money")
    void shouldStoreCreditLimitAsMoney() {
        final var partner = newPartner();
        assertThat(partner.getCreditLimit().value()).isEqualByComparingTo("10000.00");
        assertThat(partner.getCreditLimit().currency()).isEqualTo("BRL");
    }

    @Test
    @DisplayName("Should reject null name")
    void shouldRejectNullName() {
        assertThrows(NullPointerException.class, () -> Partner.builder()
                .name(null)
                .document("12345678000100")
                .creditLimit(new BigDecimal("1000.00"))
                .build());
    }

    @Test
    @DisplayName("Should reject blank name")
    void shouldRejectBlankName() {
        assertThatThrownBy(() -> Partner.builder()
                .name("   ")
                .document("12345678000100")
                .creditLimit(new BigDecimal("1000.00"))
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name cannot be blank");
    }

    @Test
    @DisplayName("Should reject null document")
    void shouldRejectNullDocument() {
        assertThrows(NullPointerException.class, () -> Partner.builder()
                .name("Acme Corp")
                .document(null)
                .creditLimit(new BigDecimal("1000.00"))
                .build());
    }

    @Test
    @DisplayName("Should reject blank document")
    void shouldRejectBlankDocument() {
        assertThatThrownBy(() -> Partner.builder()
                .name("Acme Corp")
                .document("   ")
                .creditLimit(new BigDecimal("1000.00"))
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("document cannot be blank");
    }

    @Test
    @DisplayName("Should reject null creditLimit")
    void shouldRejectNullCreditLimit() {
        assertThrows(NullPointerException.class, () -> Partner.builder()
                .name("Acme Corp")
                .document("12345678000100")
                .creditLimit(null)
                .build());
    }

    @Test
    @DisplayName("Should reject negative creditLimit")
    void shouldRejectNegativeCreditLimit() {
        assertThatThrownBy(() -> Partner.builder()
                .name("Acme Corp")
                .document("12345678000100")
                .creditLimit(new BigDecimal("-1.00"))
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be negative");
    }

    @Test
    @DisplayName("Should adjust credit limit to new value")
    void shouldAdjustCreditLimit() {
        final var partner = newPartner();
        assertDoesNotThrow(() -> partner.adjustCreditLimit(new BigDecimal("25000.00")));
        assertThat(partner.getCreditLimit().value()).isEqualByComparingTo("25000.00");
    }

    @Test
    @DisplayName("Should reject null in adjustCreditLimit")
    void shouldRejectNullInAdjustCreditLimit() {
        final var partner = newPartner();
        assertThrows(NullPointerException.class, () -> partner.adjustCreditLimit(null));
    }

    @Test
    @DisplayName("Should reject negative value in adjustCreditLimit")
    void shouldRejectNegativeValueInAdjustCreditLimit() {
        final var partner = newPartner();
        assertThatThrownBy(() -> partner.adjustCreditLimit(new BigDecimal("-500.00")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be negative");
    }

    private static Partner newPartner() {
        return newPartner(PartnerId.generate());
    }

    private static Partner newPartner(PartnerId id) {
        return Partner.builder()
                .id(id)
                .name("Acme Corp")
                .document("12345678000100")
                .creditLimit(new BigDecimal("10000.00"))
                .createdAt(Instant.now())
                .build();
    }
}

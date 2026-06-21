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
    @DisplayName("Given a provided id, when building a Partner, should create it with that id")
    void shouldCreateWithProvidedId() {
        final var id = PartnerId.generate();
        final var partner = newPartner(id);
        assertThat(partner.getId()).isEqualTo(id);
    }

    @Test
    @DisplayName("Given a null id, when building a Partner, should generate one")
    void shouldGenerateIdWhenNull() {
        final var partner = Partner.builder()
                .id(null)
                .name("Acme Corp")
                .document("12345678000100")
                .build();
        assertThat(partner.getId()).isNotNull();
    }

    @Test
    @DisplayName("Given a null createdAt, when building a Partner, should set the current time")
    void shouldSetCurrentTimeWhenCreatedAtIsNull() {
        final var before = Instant.now();
        final var partner = Partner.builder()
                .name("Acme Corp")
                .document("12345678000100")
                .createdAt(null)
                .build();
        assertThat(partner.getCreatedAt()).isAfterOrEqualTo(before);
    }

    @Test
    @DisplayName("Given a null name, when building a Partner, should reject it with NullPointerException")
    void shouldRejectNullName() {
        assertThrows(NullPointerException.class, () -> Partner.builder()
                .name(null)
                .document("12345678000100")
                .build());
    }

    @Test
    @DisplayName("Given a blank name, when building a Partner, should throw IllegalArgumentException")
    void shouldRejectBlankName() {
        assertThatThrownBy(() -> Partner.builder()
                .name("   ")
                .document("12345678000100")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não pode estar vazio");
    }

    @Test
    @DisplayName("Given a null document, when building a Partner, should reject it with NullPointerException")
    void shouldRejectNullDocument() {
        assertThrows(NullPointerException.class, () -> Partner.builder()
                .name("Acme Corp")
                .document(null)
                .build());
    }

    @Test
    @DisplayName("Given a blank document, when building a Partner, should throw IllegalArgumentException")
    void shouldRejectBlankDocument() {
        assertThatThrownBy(() -> Partner.builder()
                .name("Acme Corp")
                .document("   ")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não pode estar vazio");
    }

    private static Partner newPartner() {
        return newPartner(PartnerId.generate());
    }

    private static Partner newPartner(PartnerId id) {
        return Partner.builder()
                .id(id)
                .name("Acme Corp")
                .document("12345678000100")
                .createdAt(Instant.now())
                .build();
    }
}

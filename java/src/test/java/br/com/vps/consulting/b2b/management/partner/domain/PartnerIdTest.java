package br.com.vps.consulting.b2b.management.partner.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PartnerIdTest {

    @Test
    @DisplayName("Should generate unique PartnerId")
    void shouldGenerateUniquePartnerId() {
        final var id1 = PartnerId.generate();
        final var id2 = PartnerId.generate();
        assertThat(id1).isNotEqualTo(id2);
        assertThat(id1.value()).isNotNull();
    }

    @Test
    @DisplayName("Should create PartnerId from UUID")
    void shouldCreateFromUuid() {
        final var uuid = UUID.randomUUID();
        final var id = PartnerId.from(uuid);
        assertThat(id.value()).isEqualTo(uuid);
    }

    @Test
    @DisplayName("Should create PartnerId from String")
    void shouldCreateFromString() {
        var uuid = UUID.randomUUID();
        var id = PartnerId.from(uuid.toString());
        assertThat(id.value()).isEqualTo(uuid);
    }

    @Test
    @DisplayName("Should reject null UUID value")
    void shouldRejectNullValue() {
        assertThrows(NullPointerException.class, () -> new PartnerId(null));
    }

    @Test
    @DisplayName("Should reject malformed UUID string")
    void shouldRejectMalformedUuidString() {
        assertThatThrownBy(() -> PartnerId.from("not-a-uuid"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should return UUID string via toString")
    void shouldReturnUuidStringViaToString() {
        final var uuid = UUID.randomUUID();
        final var id = PartnerId.from(uuid);
        assertThat(id.toString()).isEqualTo(uuid.toString());
    }

    @Test
    @DisplayName("Should be equal when wrapping the same UUID")
    void shouldBeEqualForSameUuid() {
        final var uuid = UUID.randomUUID();
        assertThat(PartnerId.from(uuid)).isEqualTo(PartnerId.from(uuid));
    }
}

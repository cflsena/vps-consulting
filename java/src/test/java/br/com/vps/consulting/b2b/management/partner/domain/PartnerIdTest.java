package br.com.vps.consulting.b2b.management.partner.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PartnerIdTest {

    @Test
    @DisplayName("Given multiple calls, when generate is called, should return unique PartnerId values")
    void shouldGenerateUniquePartnerId() {
        final var id1 = PartnerId.generate();
        final var id2 = PartnerId.generate();
        assertThat(id1).isNotEqualTo(id2);
        assertThat(id1.value()).isNotNull();
    }

    @Test
    @DisplayName("Given a UUID, when from is called, should create a PartnerId wrapping it")
    void shouldCreateFromUuid() {
        final var uuid = UUID.randomUUID();
        final var id = PartnerId.from(uuid);
        assertThat(id.value()).isEqualTo(uuid);
    }

    @Test
    @DisplayName("Given a UUID string, when from is called, should create a PartnerId wrapping the parsed UUID")
    void shouldCreateFromString() {
        var uuid = UUID.randomUUID();
        var id = PartnerId.from(uuid.toString());
        assertThat(id.value()).isEqualTo(uuid);
    }

    @Test
    @DisplayName("Given a null UUID value, when constructing PartnerId, should reject it with NullPointerException")
    void shouldRejectNullValue() {
        assertThrows(NullPointerException.class, () -> new PartnerId(null));
    }

    @Test
    @DisplayName("Given a malformed UUID string, when from is called, should throw IllegalArgumentException")
    void shouldRejectMalformedUuidString() {
        assertThatThrownBy(() -> PartnerId.from("not-a-uuid"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Given a PartnerId, when toString is called, should return the UUID string")
    void shouldReturnUuidStringViaToString() {
        final var uuid = UUID.randomUUID();
        final var id = PartnerId.from(uuid);
        assertThat(id.toString()).isEqualTo(uuid.toString());
    }

    @Test
    @DisplayName("Given two PartnerId wrapping the same UUID, when compared, should be equal")
    void shouldBeEqualForSameUuid() {
        final var uuid = UUID.randomUUID();
        assertThat(PartnerId.from(uuid)).isEqualTo(PartnerId.from(uuid));
    }
}

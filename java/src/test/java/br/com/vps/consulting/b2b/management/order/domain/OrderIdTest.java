package br.com.vps.consulting.b2b.management.order.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderIdTest {

    @Test
    @DisplayName("Given multiple calls, when generate is called, should return unique OrderId values")
    void shouldGenerateUniqueOrderId() {
        final var id1 = OrderId.generate();
        final var id2 = OrderId.generate();
        assertThat(id1).isNotEqualTo(id2);
        assertThat(id1.value()).isNotNull();
    }

    @Test
    @DisplayName("Given a UUID, when from is called, should create an OrderId wrapping it")
    void shouldCreateFromUuid() {
        final var uuid = UUID.randomUUID();
        final var id = OrderId.from(uuid);
        assertThat(id.value()).isEqualTo(uuid);
    }

    @Test
    @DisplayName("Given a UUID string, when from is called, should create an OrderId wrapping the parsed UUID")
    void shouldCreateFromString() {
        final var uuid = UUID.randomUUID();
        final var id = OrderId.from(uuid.toString());
        assertThat(id.value()).isEqualTo(uuid);
    }

    @Test
    @DisplayName("Given a null UUID value, when constructing OrderId, should reject it with NullPointerException")
    void shouldRejectNullValue() {
        assertThrows(NullPointerException.class, () -> new OrderId(null));
    }

    @Test
    @DisplayName("Given a malformed UUID string, when from is called, should throw IllegalArgumentException")
    void shouldRejectMalformedUuidString() {
        assertThatThrownBy(() -> OrderId.from("not-a-uuid"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Given two OrderId wrapping the same UUID, when compared, should be equal")
    void shouldBeEqualForSameUuid() {
        final var uuid = UUID.randomUUID();
        assertThat(OrderId.from(uuid)).isEqualTo(OrderId.from(uuid));
    }
}

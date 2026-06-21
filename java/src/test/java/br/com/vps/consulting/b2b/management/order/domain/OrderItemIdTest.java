package br.com.vps.consulting.b2b.management.order.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderItemIdTest {

    @Test
    @DisplayName("Given multiple calls, when generate is called, should return unique OrderItemId values")
    void shouldGenerateUniqueOrderItemId() {
        final var id1 = OrderItemId.generate();
        final var id2 = OrderItemId.generate();
        assertThat(id1).isNotEqualTo(id2);
        assertThat(id1.value()).isNotNull();
    }

    @Test
    @DisplayName("Given a UUID, when from is called, should create an OrderItemId wrapping it")
    void shouldCreateFromUuid() {
        final var uuid = UUID.randomUUID();
        final var id = OrderItemId.from(uuid);
        assertThat(id.value()).isEqualTo(uuid);
    }

    @Test
    @DisplayName("Given a UUID string, when from is called, should create an OrderItemId wrapping the parsed UUID")
    void shouldCreateFromString() {
        final var uuid = UUID.randomUUID();
        final var id = OrderItemId.from(uuid.toString());
        assertThat(id.value()).isEqualTo(uuid);
    }

    @Test
    @DisplayName("Given a null UUID value, when constructing OrderItemId, should reject it with NullPointerException")
    void shouldRejectNullValue() {
        assertThrows(NullPointerException.class, () -> new OrderItemId(null));
    }

    @Test
    @DisplayName("Given a malformed UUID string, when from is called, should throw IllegalArgumentException")
    void shouldRejectMalformedUuidString() {
        assertThatThrownBy(() -> OrderItemId.from("not-a-uuid"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Given two OrderItemId wrapping the same UUID, when compared, should be equal")
    void shouldBeEqualForSameUuid() {
        final var uuid = UUID.randomUUID();
        assertThat(OrderItemId.from(uuid)).isEqualTo(OrderItemId.from(uuid));
    }
}

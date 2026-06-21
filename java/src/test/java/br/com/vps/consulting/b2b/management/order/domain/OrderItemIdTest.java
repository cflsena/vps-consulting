package br.com.vps.consulting.b2b.management.order.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderItemIdTest {

    @Test
    @DisplayName("Should generate unique OrderItemId")
    void shouldGenerateUniqueOrderItemId() {
        final var id1 = OrderItemId.generate();
        final var id2 = OrderItemId.generate();
        assertThat(id1).isNotEqualTo(id2);
        assertThat(id1.value()).isNotNull();
    }

    @Test
    @DisplayName("Should create OrderItemId from UUID")
    void shouldCreateFromUuid() {
        final var uuid = UUID.randomUUID();
        final var id = OrderItemId.from(uuid);
        assertThat(id.value()).isEqualTo(uuid);
    }

    @Test
    @DisplayName("Should create OrderItemId from String")
    void shouldCreateFromString() {
        final var uuid = UUID.randomUUID();
        final var id = OrderItemId.from(uuid.toString());
        assertThat(id.value()).isEqualTo(uuid);
    }

    @Test
    @DisplayName("Should reject null UUID value")
    void shouldRejectNullValue() {
        assertThrows(NullPointerException.class, () -> new OrderItemId(null));
    }

    @Test
    @DisplayName("Should reject malformed UUID string")
    void shouldRejectMalformedUuidString() {
        assertThatThrownBy(() -> OrderItemId.from("not-a-uuid"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should be equal when wrapping the same UUID")
    void shouldBeEqualForSameUuid() {
        final var uuid = UUID.randomUUID();
        assertThat(OrderItemId.from(uuid)).isEqualTo(OrderItemId.from(uuid));
    }
}

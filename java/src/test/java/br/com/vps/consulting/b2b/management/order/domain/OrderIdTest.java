package br.com.vps.consulting.b2b.management.order.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderIdTest {

    @Test
    @DisplayName("Should generate unique OrderId")
    void shouldGenerateUniqueOrderId() {
        final var id1 = OrderId.generate();
        final var id2 = OrderId.generate();
        assertThat(id1).isNotEqualTo(id2);
        assertThat(id1.value()).isNotNull();
    }

    @Test
    @DisplayName("Should create OrderId from UUID")
    void shouldCreateFromUuid() {
        final var uuid = UUID.randomUUID();
        final var id = OrderId.from(uuid);
        assertThat(id.value()).isEqualTo(uuid);
    }

    @Test
    @DisplayName("Should create OrderId from String")
    void shouldCreateFromString() {
        final var uuid = UUID.randomUUID();
        final var id = OrderId.from(uuid.toString());
        assertThat(id.value()).isEqualTo(uuid);
    }

    @Test
    @DisplayName("Should reject null UUID value")
    void shouldRejectNullValue() {
        assertThrows(NullPointerException.class, () -> new OrderId(null));
    }

    @Test
    @DisplayName("Should reject malformed UUID string")
    void shouldRejectMalformedUuidString() {
        assertThatThrownBy(() -> OrderId.from("not-a-uuid"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should be equal when wrapping the same UUID")
    void shouldBeEqualForSameUuid() {
        final var uuid = UUID.randomUUID();
        assertThat(OrderId.from(uuid)).isEqualTo(OrderId.from(uuid));
    }
}

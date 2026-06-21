package br.com.vps.consulting.b2b.management.order.domain;

import br.com.vps.consulting.b2b.management.shared.core.vo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderItemTest {

    @Test
    @DisplayName("Given all required fields, when building an OrderItem, should create it with all fields set")
    void shouldCreateWithAllFields() {
        final var item = newItem();
        assertThat(item.getId()).isNotNull();
        assertThat(item.getProductId()).isEqualTo("PROD-001");
        assertThat(item.getQuantity()).isEqualTo(2);
        assertThat(item.getUnitPrice().value()).isEqualByComparingTo("50.00");
    }

    @Test
    @DisplayName("Given a null id, when building an OrderItem, should generate one")
    void shouldGenerateIdWhenNull() {
        final var item = OrderItem.builder()
                .id(null)
                .productId("PROD-001")
                .quantity(1)
                .unitPrice(Money.of("10.00"))
                .build();
        assertThat(item.getId()).isNotNull();
    }

    @Test
    @DisplayName("Given a quantity and unit price, when subtotal is called, should calculate unit price times quantity")
    void shouldCalculateSubtotal() {
        final var item = newItem();
        assertThat(item.subtotal().value()).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("Given a null productId, when building an OrderItem, should reject it with NullPointerException")
    void shouldRejectNullProductId() {
        assertThrows(NullPointerException.class, () -> OrderItem.builder()
                .productId(null)
                .quantity(1)
                .unitPrice(Money.of("10.00"))
                .build());
    }

    @Test
    @DisplayName("Given a blank productId, when building an OrderItem, should throw IllegalArgumentException")
    void shouldRejectBlankProductId() {
        assertThatThrownBy(() -> OrderItem.builder()
                .productId("   ")
                .quantity(1)
                .unitPrice(Money.of("10.00"))
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("productId cannot be blank");
    }

    @Test
    @DisplayName("Given a null unitPrice, when building an OrderItem, should reject it with NullPointerException")
    void shouldRejectNullUnitPrice() {
        assertThrows(NullPointerException.class, () -> OrderItem.builder()
                .productId("PROD-001")
                .quantity(1)
                .unitPrice(null)
                .build());
    }

    @Test
    @DisplayName("Given a zero quantity, when building an OrderItem, should throw IllegalArgumentException")
    void shouldRejectZeroQuantity() {
        assertThatThrownBy(() -> OrderItem.builder()
                .productId("PROD-001")
                .quantity(0)
                .unitPrice(Money.of("10.00"))
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("quantity must be positive");
    }

    @Test
    @DisplayName("Given a negative quantity, when building an OrderItem, should throw IllegalArgumentException")
    void shouldRejectNegativeQuantity() {
        assertThatThrownBy(() -> OrderItem.builder()
                .productId("PROD-001")
                .quantity(-1)
                .unitPrice(Money.of("10.00"))
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("quantity must be positive");
    }

    private static OrderItem newItem() {
        return OrderItem.builder()
                .productId("PROD-001")
                .quantity(2)
                .unitPrice(Money.of("50.00"))
                .build();
    }
}

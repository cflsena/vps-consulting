package br.com.vps.consulting.b2b.management.order.domain;

import br.com.vps.consulting.b2b.management.shared.core.vo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderItemTest {

    @Test
    @DisplayName("Should create OrderItem with all fields")
    void shouldCreateWithAllFields() {
        final var item = newItem();
        assertThat(item.getId()).isNotNull();
        assertThat(item.getProductId()).isEqualTo("PROD-001");
        assertThat(item.getQuantity()).isEqualTo(2);
        assertThat(item.getUnitPrice().value()).isEqualByComparingTo("50.00");
    }

    @Test
    @DisplayName("Should generate id when null is provided")
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
    @DisplayName("Should calculate subtotal as unit price times quantity")
    void shouldCalculateSubtotal() {
        final var item = newItem();
        assertThat(item.subtotal().value()).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("Should reject null productId")
    void shouldRejectNullProductId() {
        assertThrows(NullPointerException.class, () -> OrderItem.builder()
                .productId(null)
                .quantity(1)
                .unitPrice(Money.of("10.00"))
                .build());
    }

    @Test
    @DisplayName("Should reject blank productId")
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
    @DisplayName("Should reject null unitPrice")
    void shouldRejectNullUnitPrice() {
        assertThrows(NullPointerException.class, () -> OrderItem.builder()
                .productId("PROD-001")
                .quantity(1)
                .unitPrice(null)
                .build());
    }

    @Test
    @DisplayName("Should reject zero quantity")
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
    @DisplayName("Should reject negative quantity")
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

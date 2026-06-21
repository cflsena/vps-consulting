package br.com.vps.consulting.b2b.management.order.domain;

import br.com.vps.consulting.b2b.management.order.domain.exception.InvalidOrderTransitionException;
import br.com.vps.consulting.b2b.management.partner.domain.PartnerId;
import br.com.vps.consulting.b2b.management.shared.core.vo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderTest {

    @Test
    @DisplayName("Should create pending order with generated id")
    void shouldCreatePendingOrderWithGeneratedId() {
        final var order = newPendingOrder();
        assertThat(order.getId()).isNotNull();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    @DisplayName("Should set createdAt and updatedAt on creation")
    void shouldSetTimestampsOnCreation() {
        final var order = newPendingOrder();
        assertThat(order.getCreatedAt()).isNotNull();
        assertThat(order.getUpdatedAt()).isEqualTo(order.getCreatedAt());
    }

    @Test
    @DisplayName("Should calculate total value from items")
    void shouldCalculateTotalAmountFromItems() {
        final var items = List.of(
                item("PROD-A", 2, "30.00"),
                item("PROD-B", 1, "40.00")
        );
        final var order = Order.createPending()
                .partnerId(PartnerId.generate())
                .items(items)
                .build();
        assertThat(order.getTotalAmount().value()).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("Should reject null partnerId")
    void shouldRejectNullPartnerId() {
        assertThrows(NullPointerException.class, () -> Order.createPending()
                .partnerId(null)
                .items(List.of(newItem()))
                .build());
    }

    @Test
    @DisplayName("Should reject null items list")
    void shouldRejectNullItems() {
        assertThrows(NullPointerException.class, () -> Order.createPending()
                .partnerId(PartnerId.generate())
                .items(null)
                .build());
    }

    @Test
    @DisplayName("Should reject empty items list")
    void shouldRejectEmptyItems() {
        assertThatThrownBy(() -> Order.createPending()
                .partnerId(PartnerId.generate())
                .items(List.of())
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least one item");
    }

    @Test
    @DisplayName("Should approve order from PENDING status")
    void shouldApproveFromPending() {
        final var order = newPendingOrder();
        assertDoesNotThrow(() -> order.transitionTo(OrderStatus.APPROVED));
        assertThat(order.getStatus()).isEqualTo(OrderStatus.APPROVED);
    }

    @Test
    @DisplayName("Should mark processing from APPROVED status")
    void shouldMarkProcessingFromApproved() {
        final var order = newPendingOrder();
        order.transitionTo(OrderStatus.APPROVED);
        assertDoesNotThrow(() -> order.transitionTo(OrderStatus.IN_PROCESS));
        assertThat(order.getStatus()).isEqualTo(OrderStatus.IN_PROCESS);
    }

    @Test
    @DisplayName("Should mark shipped from IN_PROCESS status")
    void shouldMarkShippedFromInProcess() {
        final var order = newPendingOrder();
        order.transitionTo(OrderStatus.APPROVED);
        order.transitionTo(OrderStatus.IN_PROCESS);
        assertDoesNotThrow(() -> order.transitionTo(OrderStatus.SENT));
        assertThat(order.getStatus()).isEqualTo(OrderStatus.SENT);
    }

    @Test
    @DisplayName("Should mark delivered from SENT status")
    void shouldMarkDeliveredFromSent() {
        final var order = newPendingOrder();
        order.transitionTo(OrderStatus.APPROVED);
        order.transitionTo(OrderStatus.IN_PROCESS);
        order.transitionTo(OrderStatus.SENT);
        assertDoesNotThrow(() -> order.transitionTo(OrderStatus.DELIVERED));
        assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);
    }

    @Test
    @DisplayName("Should cancel order from PENDING status")
    void shouldCancelFromPending() {
        final var order = newPendingOrder();
        assertDoesNotThrow(() -> order.transitionTo(OrderStatus.CANCELED));
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELED);
    }

    @Test
    @DisplayName("Should cancel order from APPROVED status")
    void shouldCancelFromApproved() {
        final var order = newPendingOrder();
        order.transitionTo(OrderStatus.APPROVED);
        assertDoesNotThrow(() -> order.transitionTo(OrderStatus.CANCELED));
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELED);
    }

    @Test
    @DisplayName("Should update updatedAt after status transition")
    void shouldUpdateUpdatedAtAfterTransitionTo() {
        final var order = newPendingOrder();
        final var before = order.getUpdatedAt();
        order.transitionTo(OrderStatus.APPROVED);
        assertThat(order.getUpdatedAt()).isAfterOrEqualTo(before);
    }

    @Test
    @DisplayName("Should reject invalid transition from PENDING to DELIVERED")
    void shouldRejectInvalidTransitionToFromPendingToDelivered() {
        final var order = newPendingOrder();
        assertThatThrownBy(() -> order.transitionTo(OrderStatus.DELIVERED))
                .isInstanceOf(InvalidOrderTransitionException.class)
                .hasMessageContaining("Transição de status inválida");
    }

    @Test
    @DisplayName("Should reject transition from DELIVERED (terminal state)")
    void shouldRejectTransitionToFromDelivered() {
        final var order = newPendingOrder();
        order.transitionTo(OrderStatus.APPROVED);
        order.transitionTo(OrderStatus.IN_PROCESS);
        order.transitionTo(OrderStatus.SENT);
        order.transitionTo(OrderStatus.DELIVERED);
        assertThatThrownBy(() -> order.transitionTo(OrderStatus.CANCELED))
                .isInstanceOf(InvalidOrderTransitionException.class);
    }

    @Test
    @DisplayName("Should reject transition from CANCELED (terminal state)")
    void shouldRejectTransitionToFromCanceled() {
        final var order = newPendingOrder();
        order.transitionTo(OrderStatus.CANCELED);
        assertThatThrownBy(() -> order.transitionTo(OrderStatus.APPROVED))
                .isInstanceOf(InvalidOrderTransitionException.class);
    }

    @Test
    @DisplayName("Should reconstitute order from all fields via default builder")
    void shouldReconstituteFromBuilder() {
        final var id = OrderId.generate();
        final var partnerId = PartnerId.generate();
        final var items = List.of(newItem());
        final var totalAmount = Money.of("50.00");
        final var createdAt = Instant.now();

        final var order = Order.builder()
                .id(id)
                .partnerId(partnerId)
                .items(items)
                .totalAmount(totalAmount)
                .status(OrderStatus.APPROVED)
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .build();

        assertThat(order.getId()).isEqualTo(id);
        assertThat(order.getPartnerId()).isEqualTo(partnerId);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.APPROVED);
        assertThat(order.getTotalAmount().value()).isEqualByComparingTo("50.00");
    }

    @Test
    @DisplayName("Should store immutable copy of items list")
    void shouldStoreImmutableItemsList() {
        final var order = newPendingOrder();
        assertThatThrownBy(() -> order.getItems().add(newItem()))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    private static Order newPendingOrder() {
        return Order.createPending()
                .partnerId(PartnerId.generate())
                .items(List.of(newItem()))
                .build();
    }

    private static OrderItem newItem() {
        return item("PROD-001", 2, "25.00");
    }

    private static OrderItem item(String productId, int quantity, String unitPrice) {
        return OrderItem.builder()
                .productId(productId)
                .quantity(quantity)
                .unitPrice(Money.of(new BigDecimal(unitPrice)))
                .build();
    }

}

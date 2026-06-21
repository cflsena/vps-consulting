package br.com.vps.consulting.b2b.management.order.domain;

import br.com.vps.consulting.b2b.management.order.domain.exception.InvalidOrderTransitionException;
import br.com.vps.consulting.b2b.management.shared.core.vo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderTest {

    @Test
    @DisplayName("Given a valid partnerId and items, when createPending is called, should create a pending order with a generated id")
    void shouldCreatePendingOrderWithGeneratedId() {
        final var order = newPendingOrder();
        assertThat(order.getId()).isNotNull();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    @DisplayName("Given a new pending order, when created, should set createdAt and updatedAt")
    void shouldSetTimestampsOnCreation() {
        final var order = newPendingOrder();
        assertThat(order.getCreatedAt()).isNotNull();
        assertThat(order.getUpdatedAt()).isEqualTo(order.getCreatedAt());
    }

    @Test
    @DisplayName("Given a list of items, when createPending is called, should calculate the total amount from them")
    void shouldCalculateTotalAmountFromItems() {
        final var items = List.of(
                item("PROD-A", 2, "30.00"),
                item("PROD-B", 1, "40.00")
        );
        final var order = Order.createPending()
                .partnerId(UUID.randomUUID())
                .items(items)
                .build();
        assertThat(order.getTotalAmount().value()).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("Given a null partnerId, when createPending is called, should reject it with NullPointerException")
    void shouldRejectNullPartnerId() {
        assertThrows(NullPointerException.class, () -> Order.createPending()
                .partnerId(null)
                .items(List.of(newItem()))
                .build());
    }

    @Test
    @DisplayName("Given a null items list, when createPending is called, should reject it with NullPointerException")
    void shouldRejectNullItems() {
        assertThrows(NullPointerException.class, () -> Order.createPending()
                .partnerId(UUID.randomUUID())
                .items(null)
                .build());
    }

    @Test
    @DisplayName("Given an empty items list, when createPending is called, should reject it because the total amount would be zero")
    void shouldRejectEmptyItems() {
        assertThatThrownBy(() -> Order.createPending()
                .partnerId(UUID.randomUUID())
                .items(List.of())
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Order totalAmount must be greater than zero");
    }

    @Test
    @DisplayName("Given a PENDING order, when transitionTo APPROVED is called, should approve it")
    void shouldApproveFromPending() {
        final var order = newPendingOrder();
        assertDoesNotThrow(() -> order.transitionTo(OrderStatus.APPROVED));
        assertThat(order.getStatus()).isEqualTo(OrderStatus.APPROVED);
    }

    @Test
    @DisplayName("Given an APPROVED order, when transitionTo IN_PROCESS is called, should mark it as processing")
    void shouldMarkProcessingFromApproved() {
        final var order = newPendingOrder();
        order.transitionTo(OrderStatus.APPROVED);
        assertDoesNotThrow(() -> order.transitionTo(OrderStatus.IN_PROCESS));
        assertThat(order.getStatus()).isEqualTo(OrderStatus.IN_PROCESS);
    }

    @Test
    @DisplayName("Given an IN_PROCESS order, when transitionTo SENT is called, should mark it as shipped")
    void shouldMarkShippedFromInProcess() {
        final var order = newPendingOrder();
        order.transitionTo(OrderStatus.APPROVED);
        order.transitionTo(OrderStatus.IN_PROCESS);
        assertDoesNotThrow(() -> order.transitionTo(OrderStatus.SENT));
        assertThat(order.getStatus()).isEqualTo(OrderStatus.SENT);
    }

    @Test
    @DisplayName("Given a SENT order, when transitionTo DELIVERED is called, should mark it as delivered")
    void shouldMarkDeliveredFromSent() {
        final var order = newPendingOrder();
        order.transitionTo(OrderStatus.APPROVED);
        order.transitionTo(OrderStatus.IN_PROCESS);
        order.transitionTo(OrderStatus.SENT);
        assertDoesNotThrow(() -> order.transitionTo(OrderStatus.DELIVERED));
        assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);
    }

    @Test
    @DisplayName("Given a PENDING order, when transitionTo CANCELED is called, should cancel it")
    void shouldCancelFromPending() {
        final var order = newPendingOrder();
        assertDoesNotThrow(() -> order.transitionTo(OrderStatus.CANCELED));
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELED);
    }

    @Test
    @DisplayName("Given an APPROVED order, when transitionTo CANCELED is called, should cancel it")
    void shouldCancelFromApproved() {
        final var order = newPendingOrder();
        order.transitionTo(OrderStatus.APPROVED);
        assertDoesNotThrow(() -> order.transitionTo(OrderStatus.CANCELED));
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELED);
    }

    @Test
    @DisplayName("Given a status transition, when transitionTo is called, should update updatedAt")
    void shouldUpdateUpdatedAtAfterTransitionTo() {
        final var order = newPendingOrder();
        final var before = order.getUpdatedAt();
        order.transitionTo(OrderStatus.APPROVED);
        assertThat(order.getUpdatedAt()).isAfterOrEqualTo(before);
    }

    @Test
    @DisplayName("Given a PENDING order, when transitionTo DELIVERED is called, should throw InvalidOrderTransitionException")
    void shouldRejectInvalidTransitionToFromPendingToDelivered() {
        final var order = newPendingOrder();
        assertThatThrownBy(() -> order.transitionTo(OrderStatus.DELIVERED))
                .isInstanceOf(InvalidOrderTransitionException.class)
                .hasMessageContaining("Transição de status inválida");
    }

    @Test
    @DisplayName("Given a DELIVERED order, when any transitionTo is called, should throw InvalidOrderTransitionException")
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
    @DisplayName("Given a CANCELED order, when any transitionTo is called, should throw InvalidOrderTransitionException")
    void shouldRejectTransitionToFromCanceled() {
        final var order = newPendingOrder();
        order.transitionTo(OrderStatus.CANCELED);
        assertThatThrownBy(() -> order.transitionTo(OrderStatus.APPROVED))
                .isInstanceOf(InvalidOrderTransitionException.class);
    }

    @Test
    @DisplayName("Given all fields, when using the default builder, should reconstitute the order")
    void shouldReconstituteFromBuilder() {
        final var id = OrderId.generate();
        final var partnerId = UUID.randomUUID();
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

    private static Order newPendingOrder() {
        return Order.createPending()
                .partnerId(UUID.randomUUID())
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

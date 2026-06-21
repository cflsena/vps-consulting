package br.com.vps.consulting.b2b.management.order.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static br.com.vps.consulting.b2b.management.order.domain.OrderStatus.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class OrderStatusTest {

    @ParameterizedTest(name = "{0} → {1}")
    @MethodSource("validTransitions")
    @DisplayName("Given a valid status pair, when canTransitionTo is called, should return true")
    void shouldAllowValidTransitions(OrderStatus from, OrderStatus to) {
        assertThat(from.canTransitionTo(to)).isTrue();
    }

    @ParameterizedTest(name = "{0} → {1}")
    @MethodSource("invalidTransitions")
    @DisplayName("Given an invalid status pair, when canTransitionTo is called, should return false")
    void shouldRejectInvalidTransitions(OrderStatus from, OrderStatus to) {
        assertThat(from.canTransitionTo(to)).isFalse();
    }

    @Test
    @DisplayName("Given the DELIVERED status, when canTransitionTo is called for any status, should always return false")
    void shouldDeliveredBeTerminal() {
        for (OrderStatus next : OrderStatus.values()) {
            assertThat(DELIVERED.canTransitionTo(next)).isFalse();
        }
    }

    @Test
    @DisplayName("Given the CANCELED status, when canTransitionTo is called for any status, should always return false")
    void shouldCanceledBeTerminal() {
        for (OrderStatus next : OrderStatus.values()) {
            assertThat(CANCELED.canTransitionTo(next)).isFalse();
        }
    }

    static Stream<Arguments> validTransitions() {
        return Stream.of(
                arguments(PENDING, APPROVED),
                arguments(PENDING, CANCELED),
                arguments(APPROVED, IN_PROCESS),
                arguments(APPROVED, CANCELED),
                arguments(IN_PROCESS, SENT),
                arguments(IN_PROCESS, CANCELED),
                arguments(SENT, DELIVERED),
                arguments(SENT, CANCELED)
        );
    }

    static Stream<Arguments> invalidTransitions() {
        return Stream.of(
                arguments(PENDING, IN_PROCESS),
                arguments(PENDING, SENT),
                arguments(PENDING, DELIVERED),
                arguments(APPROVED, PENDING),
                arguments(APPROVED, SENT),
                arguments(APPROVED, DELIVERED),
                arguments(IN_PROCESS, PENDING),
                arguments(IN_PROCESS, APPROVED),
                arguments(IN_PROCESS, DELIVERED),
                arguments(SENT, PENDING),
                arguments(SENT, APPROVED),
                arguments(SENT, IN_PROCESS),
                arguments(DELIVERED, PENDING),
                arguments(DELIVERED, APPROVED),
                arguments(DELIVERED, IN_PROCESS),
                arguments(DELIVERED, SENT),
                arguments(DELIVERED, CANCELED),
                arguments(CANCELED, PENDING),
                arguments(CANCELED, APPROVED),
                arguments(CANCELED, IN_PROCESS),
                arguments(CANCELED, SENT),
                arguments(CANCELED, DELIVERED)
        );
    }
}

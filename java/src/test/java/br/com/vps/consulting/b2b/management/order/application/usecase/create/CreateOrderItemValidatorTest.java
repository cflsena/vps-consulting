package br.com.vps.consulting.b2b.management.order.application.usecase.create;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CreateOrderItemValidatorTest {

    @Test
    @DisplayName("Given a null items list, when validate is called, should throw NullPointerException")
    void shouldRejectNullItems() {
        assertThatThrownBy(() -> CreateOrderItemValidator.validate(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("items é obrigatório");
    }

    @Test
    @DisplayName("Given an empty items list, when validate is called, should throw IllegalArgumentException")
    void shouldRejectEmptyItems() {
        assertThatThrownBy(() -> CreateOrderItemValidator.validate(List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("pelo menos um item");
    }

    @Test
    @DisplayName("Given a non-empty items list, when validate is called, should not throw")
    void shouldAcceptNonEmptyItems() {
        final var items = List.of(new CreateOrderInput.Item("PROD-001", 1, new BigDecimal("10.00")));

        assertThatNoException().isThrownBy(() -> CreateOrderItemValidator.validate(items));
    }

}

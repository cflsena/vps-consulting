package br.com.vps.consulting.b2b.management.order.application.usecase.create;

import java.util.List;
import java.util.Objects;

public interface CreateOrderItemValidator {
    static void validate(final List<CreateOrderInput.Item> items) {
        Objects.requireNonNull(items, "items é obrigatório");
        if (items.isEmpty()) throw new IllegalArgumentException("O pedido deve ter pelo menos um item");
    }
}

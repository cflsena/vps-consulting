package br.com.vps.consulting.b2b.management.order.application.usecase.create;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateOrderInput(
        UUID partnerId,
        List<Item> items
) {
    public record Item(String productId, int quantity, BigDecimal unitPrice) {}
}

package br.com.vps.consulting.b2b.management.order.domain;

import br.com.vps.consulting.b2b.management.shared.core.entity.Entity;
import br.com.vps.consulting.b2b.management.shared.core.vo.Money;
import lombok.Builder;
import lombok.Getter;

import java.util.Objects;
import java.util.UUID;

@Getter
public class OrderItem extends Entity<OrderItemId> {

    private final OrderItemId id;
    private final String productId;
    private final int quantity;
    private final Money unitPrice;

    @Builder
    public OrderItem(final UUID id, final String productId, final int quantity, final Money unitPrice) {
        this.id = id == null ? OrderItemId.generate() : OrderItemId.from(id);
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.validate();
    }

    public Money subtotal() {
        return unitPrice.multiply(quantity);
    }

    @Override
    protected void validate() {
        Objects.requireNonNull(this.id, "OrderItem id é obrigatório");
        Objects.requireNonNull(this.productId, "productId é obrigatório");
        Objects.requireNonNull(this.unitPrice, "unitPrice é obrigatório");
        if (this.productId.isBlank()) throw new IllegalArgumentException("O productId não pode estar vazio");
        if (this.quantity <= 0) throw new IllegalArgumentException("A quantidade deve ser positiva: " + quantity);
    }

}

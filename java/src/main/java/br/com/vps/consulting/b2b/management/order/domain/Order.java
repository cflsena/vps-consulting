package br.com.vps.consulting.b2b.management.order.domain;

import br.com.vps.consulting.b2b.management.order.domain.exception.InvalidOrderTransitionException;
import br.com.vps.consulting.b2b.management.partner.domain.PartnerId;
import br.com.vps.consulting.b2b.management.shared.core.entity.Entity;
import br.com.vps.consulting.b2b.management.shared.core.vo.Money;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Getter
public class Order extends Entity<OrderId> {

    private final OrderId id;
    private final PartnerId partnerId;
    private final List<OrderItem> items;
    private final Money totalAmount;
    private OrderStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    @Builder(builderMethodName = "createPending", builderClassName = "CreatePendingBuilder")
    public Order(final PartnerId partnerId, final List<OrderItem> items) {

        this.id = OrderId.generate();
        this.partnerId = partnerId;
        this.items = List.copyOf(items);
        this.totalAmount = calculateTotal(items);
        this.status = OrderStatus.PENDING;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        this.validate();

    }

    @Builder
    public Order(final OrderId id, final PartnerId partnerId, final List<OrderItem> items,
                 final Money totalAmount, final OrderStatus status, final Instant createdAt,
                 final Instant updatedAt) {
        this.id = id;
        this.partnerId = partnerId;
        this.items = List.copyOf(items);
        this.totalAmount = totalAmount;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.validate();
    }

    public void transitionTo(final OrderStatus next) {
        if (!status.canTransitionTo(next)) {
            throw new InvalidOrderTransitionException(id.value(), status, next);
        }
        this.status = next;
        this.updatedAt = Instant.now();
    }

    private static Money calculateTotal(final List<OrderItem> items) {
        return items.stream()
                .map(OrderItem::subtotal)
                .reduce(Money::add)
                .orElse(Money.of("0"));
    }

    @Override
    protected void validate() {
        Objects.requireNonNull(this.id, "OrderId é obrigatório");
        Objects.requireNonNull(this.partnerId, "PartnerId é obrigatório");
        Objects.requireNonNull(this.items, "items é obrigatório");
        if (items.isEmpty()) throw new IllegalArgumentException("Order must have at least one item");
        if (this.totalAmount.isZero())
            throw new IllegalArgumentException("Order totalAmount must be greater than zero");
    }

}

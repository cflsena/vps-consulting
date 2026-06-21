package br.com.vps.consulting.b2b.management.order.domain.event;

import br.com.vps.consulting.b2b.management.shared.core.event.DomainEvent;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderCreated(
        String aggregateId,
        UUID partnerId,
        BigDecimal totalAmount,
        String currency
) implements DomainEvent {

    public static OrderCreated of(final UUID orderId, final UUID partnerId, final BigDecimal totalAmount, final String currency) {
        return new OrderCreated(orderId.toString(), partnerId, totalAmount, currency);
    }
}

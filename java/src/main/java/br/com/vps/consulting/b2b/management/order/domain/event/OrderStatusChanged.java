package br.com.vps.consulting.b2b.management.order.domain.event;

import br.com.vps.consulting.b2b.management.shared.core.event.DomainEvent;
import br.com.vps.consulting.b2b.management.order.domain.OrderStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderStatusChanged(
        String aggregateId,
        UUID partnerId,
        OrderStatus previousStatus,
        OrderStatus newStatus,
        BigDecimal refundedAmount,
        String currency
) implements DomainEvent {

    public static OrderStatusChanged of(final UUID orderId, final UUID partnerId,
                                        final OrderStatus from, final OrderStatus to) {
        return new OrderStatusChanged(orderId.toString(), partnerId, from, to, null, null);
    }

    public static OrderStatusChanged ofCancellation(final UUID orderId, final UUID partnerId,
                                                    final OrderStatus from,
                                                    final BigDecimal refundedAmount,
                                                    final String currency) {
        return new OrderStatusChanged(orderId.toString(), partnerId, from, OrderStatus.CANCELED,
                refundedAmount, currency);
    }
}

package br.com.vps.consulting.b2b.management.order.domain.exception;

import br.com.vps.consulting.b2b.management.shared.core.exception.DomainException;
import br.com.vps.consulting.b2b.management.order.domain.OrderStatus;

import java.util.UUID;

public class InvalidOrderTransitionException extends DomainException {
    public InvalidOrderTransitionException(UUID orderId, OrderStatus from, OrderStatus to) {
        super("Transição de status inválida para o pedido %s: %s → %s".formatted(orderId, from, to));
    }
}

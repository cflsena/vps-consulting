package br.com.vps.consulting.b2b.management.order.domain.exception;

import br.com.vps.consulting.b2b.management.shared.core.exception.NotFoundException;

import java.util.UUID;

public class OrderNotFoundException extends NotFoundException {
    public OrderNotFoundException(UUID orderId) {
        super("Pedido não encontrado: " + orderId);
    }
}

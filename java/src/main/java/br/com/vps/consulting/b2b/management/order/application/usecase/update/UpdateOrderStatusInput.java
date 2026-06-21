package br.com.vps.consulting.b2b.management.order.application.usecase.update;

import br.com.vps.consulting.b2b.management.order.domain.OrderStatus;

import java.util.UUID;

public record UpdateOrderStatusInput(UUID orderId, OrderStatus targetStatus) {}

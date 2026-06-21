package br.com.vps.consulting.b2b.management.order.application.usecase.list.order;

import br.com.vps.consulting.b2b.management.order.domain.OrderStatus;

import java.time.Instant;
import java.util.UUID;

public record ListOrdersInput(
        Instant from,
        Instant to,
        OrderStatus status,
        UUID partnerId,
        long pageSize,
        long pageNumber
) {}

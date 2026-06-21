package br.com.vps.consulting.b2b.management.order.domain;

import br.com.vps.consulting.b2b.management.shared.core.page.PageCustom;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(OrderId id);
    PageCustom<Order> findByFilter(Instant from, Instant to, OrderStatus status, UUID partnerId, long pageSize, long pageNumber);
}

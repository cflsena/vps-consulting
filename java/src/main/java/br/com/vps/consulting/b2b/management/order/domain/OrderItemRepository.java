package br.com.vps.consulting.b2b.management.order.domain;

import br.com.vps.consulting.b2b.management.shared.core.page.PageCustom;

import java.util.List;
import java.util.UUID;

public interface OrderItemRepository {
    PageCustom<OrderItem> findByOrderId(OrderId orderId, long pageSize, long pageNumber);
    void saveAll(UUID orderId, List<OrderItem> items);
}

package br.com.vps.consulting.b2b.management.order.domain;

import br.com.vps.consulting.b2b.management.shared.core.page.PageCustom;

public interface OrderItemRepository {
    PageCustom<OrderItem> findByOrderId(OrderId orderId, long pageSize, long pageNumber);
}

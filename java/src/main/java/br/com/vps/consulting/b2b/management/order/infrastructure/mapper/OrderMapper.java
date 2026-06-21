package br.com.vps.consulting.b2b.management.order.infrastructure.mapper;

import br.com.vps.consulting.b2b.management.order.domain.Order;
import br.com.vps.consulting.b2b.management.order.domain.OrderId;
import br.com.vps.consulting.b2b.management.order.domain.OrderItem;
import br.com.vps.consulting.b2b.management.order.infrastructure.persistence.OrderEntity;
import br.com.vps.consulting.b2b.management.partner.domain.PartnerId;
import br.com.vps.consulting.b2b.management.shared.core.page.PageCustom;
import br.com.vps.consulting.b2b.management.shared.core.vo.Money;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

public final class OrderMapper {

    private OrderMapper() {}

    public static OrderEntity toEntity(final Order order) {
        return OrderEntity.builder()
                .id(order.getId().value())
                .partnerId(order.getPartnerId().value())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount().value())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    public static Order toDomain(final OrderEntity entity, final List<OrderItem> items) {
        return Order.builder()
                .id(OrderId.from(entity.getId()))
                .partnerId(PartnerId.from(entity.getPartnerId()))
                .items(items)
                .totalAmount(Money.of(entity.getTotalAmount()))
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static PageCustom<Order> toPage(final Page<OrderEntity> page, final Function<OrderEntity, Order> fn) {
        return PageCustom.<Order>builder()
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .numberOfElements(page.getNumberOfElements())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .items(page.getContent().stream().map(fn).toList())
                .build();
    }

}

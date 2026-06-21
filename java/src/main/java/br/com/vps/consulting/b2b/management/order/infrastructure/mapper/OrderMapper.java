package br.com.vps.consulting.b2b.management.order.infrastructure.mapper;

import br.com.vps.consulting.b2b.management.order.domain.Order;
import br.com.vps.consulting.b2b.management.order.domain.OrderId;
import br.com.vps.consulting.b2b.management.order.domain.projection.OrderProjection;
import br.com.vps.consulting.b2b.management.order.infrastructure.persistence.OrderEntity;
import br.com.vps.consulting.b2b.management.shared.core.page.PageCustom;
import br.com.vps.consulting.b2b.management.shared.core.vo.Money;
import org.springframework.data.domain.Page;

public final class OrderMapper {

    private OrderMapper() {}

    public static OrderEntity toEntity(final Order order) {
        return OrderEntity.builder()
                .id(order.getId().value())
                .partnerId(order.getPartnerId())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount().value())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    public static Order toDomain(final OrderEntity entity) {
        return Order.builder()
                .id(OrderId.from(entity.getId()))
                .partnerId(entity.getPartnerId())
                .totalAmount(Money.of(entity.getTotalAmount()))
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static PageCustom<OrderProjection> toPage(final Page<OrderProjection> page) {
        return PageCustom.<OrderProjection>builder()
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .numberOfElements(page.getNumberOfElements())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .items(page.getContent())
                .build();
    }

}

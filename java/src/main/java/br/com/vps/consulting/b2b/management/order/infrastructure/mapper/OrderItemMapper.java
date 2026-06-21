package br.com.vps.consulting.b2b.management.order.infrastructure.mapper;

import br.com.vps.consulting.b2b.management.order.domain.OrderItem;
import br.com.vps.consulting.b2b.management.order.infrastructure.persistence.OrderItemEntity;
import br.com.vps.consulting.b2b.management.shared.core.page.PageCustom;
import br.com.vps.consulting.b2b.management.shared.core.vo.Money;
import org.springframework.data.domain.Page;

import java.util.UUID;

public final class OrderItemMapper {

    private OrderItemMapper() {}

    public static OrderItemEntity toEntity(final UUID orderId, final OrderItem item) {
        return OrderItemEntity.builder()
                .id(item.getId().value())
                .orderId(orderId)
                .productId(item.getProductId())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice().value())
                .build();
    }

    public static OrderItem toDomain(final OrderItemEntity entity) {
        return OrderItem.builder()
                .id(entity.getId())
                .productId(entity.getProductId())
                .quantity(entity.getQuantity())
                .unitPrice(Money.of(entity.getUnitPrice()))
                .build();
    }

    public static PageCustom<OrderItem> toPage(final Page<OrderItemEntity> page) {
        return PageCustom.<OrderItem>builder()
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .numberOfElements(page.getNumberOfElements())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .items(page.getContent().stream().map(OrderItemMapper::toDomain).toList())
                .build();
    }

}

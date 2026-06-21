package br.com.vps.consulting.b2b.management.order.application.usecase.list.item;

import br.com.vps.consulting.b2b.management.order.domain.OrderItem;
import br.com.vps.consulting.b2b.management.shared.core.page.PageCustom;
import lombok.AccessLevel;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder(access = AccessLevel.PRIVATE)
public record OrderItemListOutput(
        UUID id,
        String productId,
        Integer quantity,
        BigDecimal unitPrice
) {

    public static PageCustom<OrderItemListOutput> from(final PageCustom<OrderItem> page) {
        return PageCustom.<OrderItemListOutput>builder()
                .pageNumber(page.pageNumber())
                .pageSize(page.pageSize())
                .numberOfElements(page.numberOfElements())
                .totalPages(page.totalPages())
                .totalElements(page.totalElements())
                .items(page.items().stream().map(OrderItemListOutput::from).toList())
                .build();
    }

    public static OrderItemListOutput from(final OrderItem item) {
        return OrderItemListOutput.builder()
                .id(item.getId().value())
                .productId(item.getProductId())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice().value())
                .build();
    }

}

package br.com.vps.consulting.b2b.management.order.application.usecase.list.order;

import br.com.vps.consulting.b2b.management.order.domain.Order;
import br.com.vps.consulting.b2b.management.order.domain.projection.OrderProjection;
import br.com.vps.consulting.b2b.management.shared.core.page.PageCustom;
import lombok.AccessLevel;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static br.com.vps.consulting.b2b.management.shared.core.utils.ManagementConstants.BRASILIA_TIME_ZONE;

@Builder(access = AccessLevel.PRIVATE)
public record OrderListOutput(
        UUID id,
        UUID partnerId,
        BigDecimal totalAmount,
        String status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static PageCustom<OrderListOutput> from(final PageCustom<OrderProjection> page) {
        return PageCustom.<OrderListOutput>builder()
                .pageNumber(page.pageNumber())
                .pageSize(page.pageSize())
                .numberOfElements(page.numberOfElements())
                .totalPages(page.totalPages())
                .totalElements(page.totalElements())
                .items(page.items().stream().map(OrderListOutput::from).toList())
                .build();
    }

    public static OrderListOutput from(final OrderProjection order) {
        return OrderListOutput.builder()
                .id(order.getId())
                .partnerId(order.getPartnerId())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().name())
                .createdAt(order.getCreatedAt().atOffset(BRASILIA_TIME_ZONE))
                .updatedAt(order.getUpdatedAt().atOffset(BRASILIA_TIME_ZONE))
                .build();
    }

}

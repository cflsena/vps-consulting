package br.com.vps.consulting.b2b.management.order.infrastructure.api.response;

import br.com.vps.consulting.b2b.management.order.application.usecase.list.item.OrderItemListOutput;
import br.com.vps.consulting.b2b.management.shared.core.page.PageCustom;
import br.com.vps.consulting.b2b.management.shared.infrastructure.api.pagination.PageResponseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder(access = AccessLevel.PRIVATE)
public record OrderItemListResponse(
        @Schema(description = "Identificador único do item do pedido")
        UUID id,

        @Schema(description = "Identificador do produto")
        String productId,

        @Schema(description = "Quantidade do item no pedido")
        Integer quantity,

        @Schema(description = "Preço unitário do produto")
        BigDecimal unitPrice
) {

    public static PageResponseDTO<OrderItemListResponse> from(final PageCustom<OrderItemListOutput> page) {
        return PageResponseDTO.<OrderItemListResponse>builder()
                .pageNumber(page.pageNumber())
                .pageSize(page.pageSize())
                .numberOfElements(page.numberOfElements())
                .totalPages(page.totalPages())
                .totalElements(page.totalElements())
                .items(page.items().stream().map(OrderItemListResponse::from).toList())
                .build();
    }

    public static OrderItemListResponse from(final OrderItemListOutput item) {
        return OrderItemListResponse.builder()
                .id(item.id())
                .productId(item.productId())
                .quantity(item.quantity())
                .unitPrice(item.unitPrice())
                .build();
    }

}

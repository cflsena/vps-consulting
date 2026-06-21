package br.com.vps.consulting.b2b.management.order.infrastructure.api.response;

import br.com.vps.consulting.b2b.management.order.application.usecase.list.order.OrderListOutput;
import br.com.vps.consulting.b2b.management.shared.core.page.PageCustom;
import br.com.vps.consulting.b2b.management.shared.infrastructure.api.pagination.PageResponseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Builder(access = AccessLevel.PRIVATE)
public record OrderListResponse(
        @Schema(description = "Identificador único do pedido")
        UUID id,

        @Schema(description = "ID do parceiro vinculado ao pedido")
        UUID partnerId,

        @Schema(description = "Valor total do pedido")
        BigDecimal totalAmount,

        @Schema(description = "Status atual do pedido", allowableValues = {"PENDING", "APPROVED", "IN_PROCESS", "SENT", "DELIVERED", "CANCELED"})
        String status,

        @Schema(description = "Data e hora de criação do pedido (fuso horário Brasília, GMT-3)")
        OffsetDateTime createdAt,

        @Schema(description = "Data e hora da última atualização do pedido (fuso horário Brasília, GMT-3)")
        OffsetDateTime updatedAt
) {

    public static PageResponseDTO<OrderListResponse> from(final PageCustom<OrderListOutput> page) {
        return PageResponseDTO.<OrderListResponse>builder()
                .pageNumber(page.pageNumber())
                .pageSize(page.pageSize())
                .numberOfElements(page.numberOfElements())
                .totalPages(page.totalPages())
                .totalElements(page.totalElements())
                .items(page.items().stream().map(OrderListResponse::from).toList())
                .build();
    }

    public static OrderListResponse from(final OrderListOutput order) {
        return OrderListResponse.builder()
                .id(order.id())
                .partnerId(order.partnerId())
                .totalAmount(order.totalAmount())
                .status(order.status())
                .createdAt(order.createdAt())
                .updatedAt(order.updatedAt())
                .build();
    }

}

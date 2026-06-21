package br.com.vps.consulting.b2b.management.order.application.usecase.list.order;

import br.com.vps.consulting.b2b.management.order.domain.Order;
import br.com.vps.consulting.b2b.management.shared.core.page.PageCustom;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static br.com.vps.consulting.b2b.management.shared.core.utils.ManagementConstants.BRASILIA_TIME_ZONE;

@Builder(access = AccessLevel.PRIVATE)
public record OrderListOutput(
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

    public static PageCustom<OrderListOutput> from(final PageCustom<Order> page) {
        return PageCustom.<OrderListOutput>builder()
                .pageNumber(page.pageNumber())
                .pageSize(page.pageSize())
                .numberOfElements(page.numberOfElements())
                .totalPages(page.totalPages())
                .totalElements(page.totalElements())
                .items(page.items().stream().map(OrderListOutput::from).toList())
                .build();
    }

    public static OrderListOutput from(final Order order) {
        return OrderListOutput.builder()
                .id(order.getId().value())
                .partnerId(order.getPartnerId().value())
                .totalAmount(order.getTotalAmount().value())
                .status(order.getStatus().name())
                .createdAt(order.getCreatedAt().atOffset(BRASILIA_TIME_ZONE))
                .updatedAt(order.getUpdatedAt().atOffset(BRASILIA_TIME_ZONE))
                .build();
    }

}

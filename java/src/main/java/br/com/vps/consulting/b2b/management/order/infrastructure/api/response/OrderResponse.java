package br.com.vps.consulting.b2b.management.order.infrastructure.api.response;

import br.com.vps.consulting.b2b.management.order.application.usecase.find.OrderOutput;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static br.com.vps.consulting.b2b.management.shared.core.utils.ManagementConstants.BRASILIA_TIME_ZONE;

@Builder(access = AccessLevel.PRIVATE)
public record OrderResponse(
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

    public static OrderResponse from(final OrderOutput orderOutput) {
        return OrderResponse.builder()
                .id(orderOutput.id())
                .partnerId(orderOutput.partnerId())
                .totalAmount(orderOutput.totalAmount())
                .status(orderOutput.status())
                .createdAt(orderOutput.createdAt().atOffset(BRASILIA_TIME_ZONE))
                .updatedAt(orderOutput.updatedAt().atOffset(BRASILIA_TIME_ZONE))
                .build();
    }

}

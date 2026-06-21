package br.com.vps.consulting.b2b.management.order.application.usecase.find;

import br.com.vps.consulting.b2b.management.order.domain.Order;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static br.com.vps.consulting.b2b.management.shared.core.utils.ManagementConstants.BRASILIA_TIME_ZONE;

@Builder(access = AccessLevel.PRIVATE)
public record OrderOutput(
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

    public static OrderOutput from(final Order order) {
        return OrderOutput.builder()
                .id(order.getId().value())
                .partnerId(order.getPartnerId().value())
                .totalAmount(order.getTotalAmount().value())
                .status(order.getStatus().name())
                .createdAt(order.getCreatedAt().atOffset(BRASILIA_TIME_ZONE))
                .updatedAt(order.getUpdatedAt().atOffset(BRASILIA_TIME_ZONE))
                .build();
    }

}

package br.com.vps.consulting.b2b.management.order.application.usecase.find;

import br.com.vps.consulting.b2b.management.order.domain.projection.OrderProjection;
import lombok.AccessLevel;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static br.com.vps.consulting.b2b.management.shared.core.utils.ManagementConstants.BRASILIA_TIME_ZONE;

@Builder(access = AccessLevel.PRIVATE)
public record OrderOutput(
        UUID id,
        UUID partnerId,
        BigDecimal totalAmount,
        String status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static OrderOutput from(final OrderProjection order) {
        return OrderOutput.builder()
                .id(order.getId())
                .partnerId(order.getPartnerId())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().name())
                .createdAt(order.getCreatedAt().atOffset(BRASILIA_TIME_ZONE))
                .updatedAt(order.getUpdatedAt().atOffset(BRASILIA_TIME_ZONE))
                .build();
    }
}

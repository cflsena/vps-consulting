package br.com.vps.consulting.b2b.management.order.application.usecase.find;

import br.com.vps.consulting.b2b.management.order.domain.projection.OrderProjection;
import lombok.AccessLevel;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Builder(access = AccessLevel.PRIVATE)
public record OrderOutput(
        UUID id,
        UUID partnerId,
        BigDecimal totalAmount,
        String status,
        Instant createdAt,
        Instant updatedAt
) {
    public static OrderOutput from(final OrderProjection order) {
        return OrderOutput.builder()
                .id(order.getId())
                .partnerId(order.getPartnerId())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().name())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}

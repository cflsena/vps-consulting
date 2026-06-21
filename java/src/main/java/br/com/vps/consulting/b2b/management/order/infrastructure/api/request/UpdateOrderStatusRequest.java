package br.com.vps.consulting.b2b.management.order.infrastructure.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record UpdateOrderStatusRequest(
        @Schema(
                description = "Novo status desejado para o pedido.",
                allowableValues = {"APPROVED", "IN_PROCESS", "SENT", "DELIVERED", "CANCELED"}
        )
        @NotBlank String targetStatus
) {}

package br.com.vps.consulting.b2b.management.order.infrastructure.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record OrderItemRequest(
        @Schema(description = "Identificador único do produto")
        @NotBlank String productId,

        @Schema(description = "Quantidade do item no pedido", minimum = "1")
        @Min(1) int quantity,

        @Schema(description = "Preço unitário do produto", example = "199.90")
        @NotNull @Positive BigDecimal unitPrice
) {}

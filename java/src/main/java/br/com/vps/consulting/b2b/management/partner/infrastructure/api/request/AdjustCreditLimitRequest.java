package br.com.vps.consulting.b2b.management.partner.infrastructure.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record AdjustCreditLimitRequest(
        @Schema(description = "Novo limite de crédito a ser configurado", example = "15000.00")
        @NotNull @Positive BigDecimal newCreditLimit
) {}

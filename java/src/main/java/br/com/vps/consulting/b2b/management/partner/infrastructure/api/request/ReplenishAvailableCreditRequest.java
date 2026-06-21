package br.com.vps.consulting.b2b.management.partner.infrastructure.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ReplenishAvailableCreditRequest(
        @Schema(description = "Valor a ser reposto no saldo disponível do parceiro", example = "2000.00")
        @NotNull @Positive BigDecimal amount
) {}

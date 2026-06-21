package br.com.vps.consulting.b2b.management.partner.infrastructure.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreatePartnerRequest(
        @Schema(description = "Nome do parceiro")
        @NotBlank String name,

        @Schema(description = "Documento do parceiro (CPF ou CNPJ)")
        @NotBlank String document,

        @Schema(description = "Limite de crédito inicial do parceiro", example = "10000.00")
        @NotNull @Positive BigDecimal creditLimit
) {}

package br.com.vps.consulting.b2b.management.partner.infrastructure.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PartnerCreditResponse(
        @Schema(description = "ID do parceiro")
        UUID partnerId,

        @Schema(description = "Limite de crédito total configurado para o parceiro")
        BigDecimal creditLimit,

        @Schema(description = "Saldo disponível para utilização em novos pedidos")
        BigDecimal availableBalance,

        @Schema(description = "Saldo reservado por pedidos com status PENDING")
        BigDecimal reservedBalance,

        @Schema(description = "Data e hora da última atualização do crédito (fuso horário Brasília, GMT-3)")
        OffsetDateTime updatedAt
) {}

package br.com.vps.consulting.b2b.management.partner.infrastructure.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record PartnerCreatedResponse(
        @Schema(description = "ID único do parceiro criado")
        UUID id
) {}

package br.com.vps.consulting.b2b.management.order.infrastructure.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record OrderCreatedResponse(
        @Schema(description = "ID único do pedido criado")
        UUID id
) {}

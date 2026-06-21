package br.com.vps.consulting.b2b.management.order.infrastructure.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record CreateOrderRequest(
        @Schema(description = "ID do parceiro responsável pelo pedido")
        @NotNull UUID partnerId,

        @Schema(description = "Lista de itens do pedido. Deve conter ao menos 1 item.")
        @NotNull @Valid @Size(min = 1) List<OrderItemRequest> items
) {}

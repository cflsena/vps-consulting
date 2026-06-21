package br.com.vps.consulting.b2b.management.order.infrastructure.api;

import br.com.vps.consulting.b2b.management.order.application.usecase.find.OrderOutput;
import br.com.vps.consulting.b2b.management.order.application.usecase.list.item.OrderItemListOutput;
import br.com.vps.consulting.b2b.management.order.application.usecase.list.order.OrderListOutput;
import br.com.vps.consulting.b2b.management.order.domain.OrderStatus;
import br.com.vps.consulting.b2b.management.shared.infrastructure.api.ApiBaseDocumentation;
import br.com.vps.consulting.b2b.management.shared.infrastructure.api.pagination.PageResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RequestMapping("api/v1/b2b/orders")
@Tag(name = "Pedido", description = "API para gerenciamento de pedidos B2B")
public interface OrderApi extends ApiBaseDocumentation {

    record OrderItemRequest(
            @Schema(description = "Identificador único do produto")
            @NotBlank String productId,

            @Schema(description = "Quantidade do item no pedido", minimum = "1")
            @Min(1) int quantity,

            @Schema(description = "Preço unitário do produto", example = "199.90")
            @NotNull @Positive BigDecimal unitPrice
    ) {}

    record CreateOrderRequest(
            @Schema(description = "ID do parceiro responsável pelo pedido")
            @NotNull UUID partnerId,

            @Schema(description = "Lista de itens do pedido. Deve conter ao menos 1 item.")
            @NotNull @Valid @Size(min = 1) List<OrderItemRequest> items
    ) {}

    record UpdateOrderStatusRequest(
            @Schema(
                description = "Novo status desejado para o pedido.",
                allowableValues = {"APPROVED", "IN_PROCESS", "SENT", "DELIVERED", "CANCELED"}
            )
            @NotBlank String targetStatus
    ) {}

    record OrderCreatedResponse(
            @Schema(description = "ID único do pedido criado")
            UUID id
    ) {}

    @Operation(
        summary = "Criar um novo pedido",
        description = "Cria um pedido B2B para o parceiro informado, reservando o crédito necessário no momento da criação. O pedido é criado com status PENDING."
    )
    @ApiResponse(responseCode = "201", description = "Pedido criado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos")
    @ApiResponse(responseCode = "404", description = "Parceiro não encontrado")
    @ApiResponse(responseCode = "422", description = "Crédito insuficiente ou regra de negócio violada")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<OrderCreatedResponse> createOrder(@RequestBody @Valid CreateOrderRequest request);

    @Operation(
        summary = "Listar pedidos",
        description = "Retorna uma lista paginada de pedidos com filtros opcionais por período, status e parceiro. Todos os filtros são combinados (AND)."
    )
    @ApiResponse(responseCode = "200", description = "Pedidos listados com sucesso")
    @ApiResponse(responseCode = "400", description = "Parâmetros de filtro inválidos (ex: data inicial posterior à data final)")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<PageResponseDTO<OrderListOutput>> listOrders(
            @Parameter(description = "Data inicial do filtro no fuso horário de Brasília (GMT-3). Formato: yyyy-MM-dd")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "Data final do filtro no fuso horário de Brasília (GMT-3). Formato: yyyy-MM-dd")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @Parameter(description = "Filtro por status do pedido. Valores válidos: PENDING, APPROVED, IN_PROCESS, SENT, DELIVERED, CANCELED")
            @RequestParam(required = false) OrderStatus status,
            @Parameter(description = "Filtro pelo ID do parceiro vinculado ao pedido")
            @RequestParam(required = false) UUID partnerId,
            @Parameter(description = "Número da página (base 0)", example = "0")
            @RequestParam(defaultValue = "0") int pageNumber,
            @Parameter(description = "Quantidade de registros por página", example = "20")
            @RequestParam(defaultValue = "20") int pageSize);

    @Operation(
        summary = "Buscar pedido por ID",
        description = "Retorna os detalhes completos de um pedido pelo seu identificador único."
    )
    @ApiResponse(responseCode = "200", description = "Pedido encontrado com sucesso")
    @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<OrderOutput> findOrderById(
            @Parameter(description = "ID único do pedido") @PathVariable UUID id);

    @Operation(
        summary = "Listar itens do pedido",
        description = "Retorna a lista paginada de itens de um pedido específico."
    )
    @ApiResponse(responseCode = "200", description = "Itens listados com sucesso")
    @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
    @GetMapping(value = "/{id}/items", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<PageResponseDTO<OrderItemListOutput>> listOrderItems(
            @Parameter(description = "ID único do pedido") @PathVariable UUID id,
            @Parameter(description = "Número da página (base 0)", example = "0")
            @RequestParam(defaultValue = "0") int pageNumber,
            @Parameter(description = "Quantidade de registros por página", example = "20")
            @RequestParam(defaultValue = "20") int pageSize);

    @Operation(
        summary = "Atualizar status do pedido",
        description = "Atualiza o status do pedido seguindo as transições permitidas: PENDING→APPROVED/CANCELED, APPROVED→IN_PROCESS/CANCELED, IN_PROCESS→SENT/CANCELED, SENT→DELIVERED/CANCELED. Transições de status disparam operações de crédito correspondentes."
    )
    @ApiResponse(responseCode = "204", description = "Status atualizado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos")
    @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
    @ApiResponse(responseCode = "422", description = "Transição de status inválida")
    @PatchMapping(value = "/{id}/status", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> updateOrderStatus(
            @Parameter(description = "ID único do pedido") @PathVariable UUID id,
            @RequestBody @Valid UpdateOrderStatusRequest request);

}

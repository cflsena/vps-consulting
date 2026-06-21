package br.com.vps.consulting.b2b.management.order.infrastructure.api;

import br.com.vps.consulting.b2b.management.order.domain.OrderStatus;
import br.com.vps.consulting.b2b.management.order.infrastructure.api.request.CreateOrderRequest;
import br.com.vps.consulting.b2b.management.order.infrastructure.api.request.UpdateOrderStatusRequest;
import br.com.vps.consulting.b2b.management.order.infrastructure.api.response.OrderCreatedResponse;
import br.com.vps.consulting.b2b.management.order.infrastructure.api.response.OrderItemListResponse;
import br.com.vps.consulting.b2b.management.order.infrastructure.api.response.OrderListResponse;
import br.com.vps.consulting.b2b.management.order.infrastructure.api.response.OrderResponse;
import br.com.vps.consulting.b2b.management.shared.infrastructure.api.ApiBaseDocumentation;
import br.com.vps.consulting.b2b.management.shared.infrastructure.api.pagination.PageResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RequestMapping("api/v1/b2b/orders")
@Tag(name = "Pedido", description = "API para gerenciamento de pedidos B2B")
public interface OrderApi extends ApiBaseDocumentation {

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
    ResponseEntity<PageResponseDTO<OrderListResponse>> listOrders(
            @Parameter(description = "Data inicial do filtro. Formato: yyyy-MM-dd")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "Data final do filtro. Formato: yyyy-MM-dd")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @Parameter(description = "Filtro por status do pedido.")
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
    ResponseEntity<OrderResponse> findOrderById(@Parameter(description = "ID único do pedido") @PathVariable UUID id);

    @Operation(
        summary = "Listar itens do pedido",
        description = "Retorna a lista paginada de itens de um pedido específico."
    )
    @ApiResponse(responseCode = "200", description = "Itens listados com sucesso")
    @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
    @GetMapping(value = "/{id}/items", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<PageResponseDTO<OrderItemListResponse>> listOrderItems(
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

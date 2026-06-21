package br.com.vps.consulting.b2b.management.order.infrastructure.api;

import br.com.vps.consulting.b2b.management.order.application.usecase.create.CreateOrderInput;
import br.com.vps.consulting.b2b.management.order.application.usecase.create.CreateOrderUseCase;
import br.com.vps.consulting.b2b.management.order.application.usecase.find.FindOrderByIdUseCase;
import br.com.vps.consulting.b2b.management.order.application.usecase.find.OrderOutput;
import br.com.vps.consulting.b2b.management.order.application.usecase.list.item.ListOrderItemsInput;
import br.com.vps.consulting.b2b.management.order.application.usecase.list.item.ListOrderItemsUseCase;
import br.com.vps.consulting.b2b.management.order.application.usecase.list.item.OrderItemListOutput;
import br.com.vps.consulting.b2b.management.order.application.usecase.list.order.ListOrdersUseCase;
import br.com.vps.consulting.b2b.management.order.application.usecase.list.order.OrderListOutput;
import br.com.vps.consulting.b2b.management.order.application.usecase.update.UpdateOrderStatusInput;
import br.com.vps.consulting.b2b.management.order.application.usecase.update.UpdateOrderStatusUseCase;
import br.com.vps.consulting.b2b.management.order.domain.OrderStatus;
import br.com.vps.consulting.b2b.management.order.infrastructure.mapper.OrderRequestMapper;
import br.com.vps.consulting.b2b.management.shared.infrastructure.api.pagination.PageResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class OrderController implements OrderApi {

    private final CreateOrderUseCase createOrderUseCase;
    private final FindOrderByIdUseCase findOrderByIdUseCase;
    private final ListOrdersUseCase listOrdersUseCase;
    private final ListOrderItemsUseCase listOrderItemsUseCase;
    private final UpdateOrderStatusUseCase updateOrderStatusUseCase;

    @Override
    public ResponseEntity<OrderCreatedResponse> createOrder(final CreateOrderRequest request) {
        final var items = request.items().stream()
                .map(i -> new CreateOrderInput.Item(i.productId(), i.quantity(), i.unitPrice()))
                .toList();
        final var id = createOrderUseCase.execute(new CreateOrderInput(request.partnerId(), items));
        return ResponseEntity.status(HttpStatus.CREATED).body(new OrderCreatedResponse(id));
    }

    @Override
    public ResponseEntity<PageResponseDTO<OrderListOutput>> listOrders(
            final LocalDate from, final LocalDate to, final OrderStatus status,
            final UUID partnerId, final int pageNumber, final int pageSize) {
        final var page = listOrdersUseCase.execute(
                OrderRequestMapper.toListInput(from, to, status, partnerId, pageSize, pageNumber));
        return ResponseEntity.ok(PageResponseDTO.from(page));
    }

    @Override
    public ResponseEntity<OrderOutput> findOrderById(final UUID id) {
        return ResponseEntity.ok(findOrderByIdUseCase.execute(id));
    }

    @Override
    public ResponseEntity<PageResponseDTO<OrderItemListOutput>> listOrderItems(
            final UUID id, final int pageNumber, final int pageSize) {
        final var page = listOrderItemsUseCase.execute(new ListOrderItemsInput(id, pageSize, pageNumber));
        return ResponseEntity.ok(PageResponseDTO.from(page));
    }

    @Override
    public ResponseEntity<Void> updateOrderStatus(final UUID id, final UpdateOrderStatusRequest request) {
        updateOrderStatusUseCase.execute(
                new UpdateOrderStatusInput(id, OrderStatus.valueOf(request.targetStatus())));
        return ResponseEntity.noContent().build();
    }

}

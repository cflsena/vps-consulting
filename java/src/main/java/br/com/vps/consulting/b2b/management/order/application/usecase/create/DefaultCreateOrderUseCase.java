package br.com.vps.consulting.b2b.management.order.application.usecase.create;

import br.com.vps.consulting.b2b.management.order.application.service.PartnerCreditService;
import br.com.vps.consulting.b2b.management.order.domain.Order;
import br.com.vps.consulting.b2b.management.order.domain.OrderItem;
import br.com.vps.consulting.b2b.management.order.domain.OrderItemRepository;
import br.com.vps.consulting.b2b.management.order.domain.OrderRepository;
import br.com.vps.consulting.b2b.management.order.domain.event.OrderCreated;
import br.com.vps.consulting.b2b.management.shared.core.event.EventPublisher;
import br.com.vps.consulting.b2b.management.shared.core.vo.Money;
import jakarta.inject.Named;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

@Slf4j
@Named
@RequiredArgsConstructor
public class DefaultCreateOrderUseCase implements CreateOrderUseCase {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PartnerCreditService partnerCreditService;
    private final EventPublisher eventPublisher;

    @Override
    @Transactional
    public UUID execute(final CreateOrderInput input) {

        final var itemsToCreate = buildItems(input);
        final var order = Order.createPending()
                .partnerId(input.partnerId())
                .items(itemsToCreate)
                .build();

        log.info("Creating order [partnerId={}, itemCount={}, totalAmount={}]",
                input.partnerId(), input.items().size(), order.getTotalAmount().value());

        partnerCreditService.reserveCredit(input.partnerId(), order.getTotalAmount().value());

        final var orderCreated = orderRepository.save(order);
        orderItemRepository.saveAll(orderCreated.getId().value(), itemsToCreate);

        log.info("Order created successfully [orderId={}, partnerId={}, totalAmount={}]",
                orderCreated.getId().value(), orderCreated.getPartnerId(), orderCreated.getTotalAmount().value());

        eventPublisher.publish(OrderCreated.of(
                orderCreated.getId().value(),
                orderCreated.getPartnerId(),
                orderCreated.getTotalAmount().value(),
                orderCreated.getTotalAmount().currency()
        ));

        return orderCreated.getId().value();

    }

    private List<OrderItem> buildItems(final CreateOrderInput input) {
        CreateOrderItemValidator.validate(input.items());
        return input.items().stream()
                .map(item -> OrderItem.builder()
                        .productId(item.productId())
                        .quantity(item.quantity())
                        .unitPrice(Money.of(item.unitPrice()))
                        .build())
                .toList();
    }

}

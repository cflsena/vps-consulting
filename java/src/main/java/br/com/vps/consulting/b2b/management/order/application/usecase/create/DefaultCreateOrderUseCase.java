package br.com.vps.consulting.b2b.management.order.application.usecase.create;

import br.com.vps.consulting.b2b.management.order.application.service.PartnerCreditService;
import br.com.vps.consulting.b2b.management.order.domain.Order;
import br.com.vps.consulting.b2b.management.order.domain.OrderItem;
import br.com.vps.consulting.b2b.management.order.domain.OrderRepository;
import br.com.vps.consulting.b2b.management.order.domain.event.OrderCreated;
import br.com.vps.consulting.b2b.management.partner.domain.PartnerId;
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
    private final PartnerCreditService partnerCreditService;
    private final EventPublisher eventPublisher;

    @Override
    @Transactional
    public UUID execute(final CreateOrderInput input) {

        final var partnerId = PartnerId.from(input.partnerId());
        final var items = buildItems(input.items());
        final var order = Order.createPending()
                .partnerId(partnerId)
                .items(items)
                .build();

        log.info("Creating order [partnerId={}, itemCount={}, totalAmount={}]",
                input.partnerId(), items.size(), order.getTotalAmount().value());

        partnerCreditService.reserveCredit(input.partnerId(), order.getTotalAmount().value());

        final var saved = orderRepository.save(order);

        log.info("Order created successfully [orderId={}, partnerId={}, totalAmount={}]",
                saved.getId().value(), saved.getPartnerId().value(), saved.getTotalAmount().value());

        eventPublisher.publish(OrderCreated.of(
                saved.getId().value(),
                saved.getPartnerId().value(),
                saved.getTotalAmount().value(),
                saved.getTotalAmount().currency()
        ));

        return saved.getId().value();

    }

    private List<OrderItem> buildItems(final List<CreateOrderInput.Item> items) {
        return items.stream()
                .map(item -> OrderItem.builder()
                        .productId(item.productId())
                        .quantity(item.quantity())
                        .unitPrice(Money.of(item.unitPrice()))
                        .build())
                .toList();
    }

}

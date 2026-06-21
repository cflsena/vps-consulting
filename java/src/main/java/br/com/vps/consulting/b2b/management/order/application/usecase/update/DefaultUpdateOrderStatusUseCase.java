package br.com.vps.consulting.b2b.management.order.application.usecase.update;

import br.com.vps.consulting.b2b.management.order.application.service.PartnerCreditService;
import br.com.vps.consulting.b2b.management.order.domain.OrderId;
import br.com.vps.consulting.b2b.management.order.domain.OrderRepository;
import br.com.vps.consulting.b2b.management.order.domain.OrderStatus;
import br.com.vps.consulting.b2b.management.order.domain.event.OrderStatusChanged;
import br.com.vps.consulting.b2b.management.order.domain.exception.OrderNotFoundException;
import br.com.vps.consulting.b2b.management.shared.core.event.EventPublisher;
import jakarta.inject.Named;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Named
@RequiredArgsConstructor
public class DefaultUpdateOrderStatusUseCase implements UpdateOrderStatusUseCase {

    private final OrderRepository orderRepository;
    private final PartnerCreditService partnerCreditService;
    private final EventPublisher eventPublisher;

    @Override
    @Transactional
    public void execute(final UpdateOrderStatusInput command) {

        log.info("Updating order status [orderId={}, targetStatus={}]", command.orderId(), command.targetStatus());

        final var order = orderRepository.findById(OrderId.from(command.orderId()))
                .orElseThrow(() -> new OrderNotFoundException(command.orderId()));

        final var previousStatus = order.getStatus();
        order.transitionTo(command.targetStatus());
        orderRepository.save(order);

        final var currentStatus = order.getStatus();
        final var partnerId = order.getPartnerId();
        final var amount = order.getTotalAmount().value();
        final var currency = order.getTotalAmount().currency();

        log.info("Order status updated [orderId={}, from={}, to={}, partnerId={}]",
                order.getId().value(), previousStatus, currentStatus, partnerId);

        processCredit(command, previousStatus, currentStatus, partnerId, amount);

        final var event = currentStatus == OrderStatus.CANCELED
                ? OrderStatusChanged.ofCancellation(order.getId().value(), partnerId, previousStatus, amount, currency)
                : OrderStatusChanged.of(order.getId().value(), partnerId, previousStatus, currentStatus);

        eventPublisher.publish(event);

    }

    private void processCredit(UpdateOrderStatusInput command, OrderStatus previousStatus, OrderStatus currentStatus, UUID partnerId, BigDecimal amount) {

        if (previousStatus == OrderStatus.PENDING && currentStatus == OrderStatus.APPROVED) {
            log.info("Debiting credit reservation [orderId={}, partnerId={}, amount={}]", command.orderId(), partnerId, amount);
            partnerCreditService.debitReservation(partnerId, amount);
            return;
        }

        if (currentStatus == OrderStatus.CANCELED) {
            if (previousStatus == OrderStatus.PENDING) {
                log.info("Releasing credit reservation [orderId={}, partnerId={}, amount={}]", command.orderId(), partnerId, amount);
                partnerCreditService.releaseReservation(partnerId, amount);
            } else {
                log.info("Refunding credit debit [orderId={}, partnerId={}, amount={}]", command.orderId(), partnerId, amount);
                partnerCreditService.refundDebit(partnerId, amount);
            }
        }

    }

}

package br.com.vps.consulting.b2b.management.order.application.usecase.find;

import br.com.vps.consulting.b2b.management.order.domain.OrderId;
import br.com.vps.consulting.b2b.management.order.domain.OrderRepository;
import br.com.vps.consulting.b2b.management.order.domain.exception.OrderNotFoundException;
import jakarta.inject.Named;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
@Named
@RequiredArgsConstructor
public class DefaultFindOrderByIdUseCase implements FindOrderByIdUseCase {

    private final OrderRepository orderRepository;

    @Override
    public OrderOutput execute(final UUID orderId) {
        log.info("Finding order by id [orderId={}]", orderId);
        final var output = orderRepository.findById(OrderId.from(orderId))
                .map(OrderOutput::from)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        log.info("Order found [orderId={}]", orderId);
        return output;
    }

}

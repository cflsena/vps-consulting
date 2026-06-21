package br.com.vps.consulting.b2b.management.order.application.usecase.list.item;

import br.com.vps.consulting.b2b.management.order.domain.OrderId;
import br.com.vps.consulting.b2b.management.order.domain.OrderItemRepository;
import br.com.vps.consulting.b2b.management.shared.core.page.PageCustom;
import jakarta.inject.Named;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Named
@RequiredArgsConstructor
public class DefaultListOrderItemsUseCase implements ListOrderItemsUseCase {

    private final OrderItemRepository orderItemRepository;

    @Override
    public PageCustom<OrderItemListOutput> execute(final ListOrderItemsInput input) {
        log.info("Listing items for order [orderId={}, page={}, size={}]",
                input.orderId(), input.pageNumber(), input.pageSize());
        final var page = orderItemRepository.findByOrderId(
                OrderId.from(input.orderId()),
                input.pageSize(),
                input.pageNumber()
        );
        return OrderItemListOutput.from(page);
    }

}

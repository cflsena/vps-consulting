package br.com.vps.consulting.b2b.management.order.application.usecase.list.order;

import br.com.vps.consulting.b2b.management.order.domain.OrderRepository;
import br.com.vps.consulting.b2b.management.shared.core.exception.DomainException;
import br.com.vps.consulting.b2b.management.shared.core.page.PageCustom;
import jakarta.inject.Named;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Named
@RequiredArgsConstructor
public class DefaultListOrdersUseCase implements ListOrdersUseCase {

    private final OrderRepository orderRepository;

    @Override
    public PageCustom<OrderListOutput> execute(final ListOrdersInput input) {
        log.debug("Listing orders [from={}, to={}, status={}, partnerId={}, page={}, size={}]",
                input.from(), input.to(), input.status(), input.partnerId(), input.pageNumber(), input.pageSize());

        if (input.from() != null && input.to() != null && input.from().isAfter(input.to())) {
            log.warn("Invalid date range: from [{}] is after to [{}]", input.from(), input.to());
            throw new DomainException("A data inicial não pode ser posterior à data final");
        }
        final var page = orderRepository.findByFilter(
                input.from(),
                input.to(),
                input.status(),
                input.partnerId(),
                input.pageSize(),
                input.pageNumber()
        );
        return OrderListOutput.from(page);
    }

}

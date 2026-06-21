package br.com.vps.consulting.b2b.management.order.infrastructure.persistence;

import br.com.vps.consulting.b2b.management.order.domain.OrderId;
import br.com.vps.consulting.b2b.management.order.domain.OrderItem;
import br.com.vps.consulting.b2b.management.order.domain.OrderItemRepository;
import br.com.vps.consulting.b2b.management.order.infrastructure.mapper.OrderItemMapper;
import br.com.vps.consulting.b2b.management.order.infrastructure.persistence.jpa.OrderItemJpaRepository;
import br.com.vps.consulting.b2b.management.shared.core.page.PageCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DefaultOrderItemRepository implements OrderItemRepository {

    private final OrderItemJpaRepository orderItemJpaRepository;

    @Override
    public PageCustom<OrderItem> findByOrderId(final OrderId orderId, final long pageSize, final long pageNumber) {
        final var pageable = PageRequest.of((int) pageNumber, (int) pageSize);
        return OrderItemMapper.toPage(orderItemJpaRepository.findByOrderId(orderId.value(), pageable));
    }

}

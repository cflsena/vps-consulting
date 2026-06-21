package br.com.vps.consulting.b2b.management.order.infrastructure.persistence;

import br.com.vps.consulting.b2b.management.order.domain.*;
import br.com.vps.consulting.b2b.management.order.domain.projection.OrderProjection;
import br.com.vps.consulting.b2b.management.order.infrastructure.mapper.OrderItemMapper;
import br.com.vps.consulting.b2b.management.order.infrastructure.mapper.OrderMapper;
import br.com.vps.consulting.b2b.management.order.infrastructure.persistence.jpa.OrderItemJpaRepository;
import br.com.vps.consulting.b2b.management.order.infrastructure.persistence.jpa.OrderJpaRepository;
import br.com.vps.consulting.b2b.management.order.infrastructure.persistence.jpa.OrderSpecification;
import br.com.vps.consulting.b2b.management.shared.core.page.PageCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DefaultOrderRepository implements OrderRepository {

    private final OrderJpaRepository orderJpaRepository;
    private final OrderItemJpaRepository orderItemJpaRepository;

    @Override
    public Order save(final Order order) {
        orderJpaRepository.save(OrderMapper.toEntity(order));
        final var items = order.getItems().stream()
                .map(item -> OrderItemMapper.toEntity(item, order.getId().value()))
                .toList();
        orderItemJpaRepository.saveAll(items);
        return order;
    }

    @Override
    public Optional<Order> findById(final OrderId id) {
        return orderJpaRepository.findById(id.value()).map(OrderMapper::toDomain);
    }

    @Override
    public Optional<OrderProjection> findOrderDetailsById(final OrderId id) {
        return orderJpaRepository.findProjectedById(id.value());
    }

    @Override
    public PageCustom<OrderProjection> findByFilter(final Instant from, final Instant to, final OrderStatus status,
                                                    final UUID partnerId, final long pageSize, final long pageNumber) {
        final Specification<OrderEntity> spec = Specification
                .where(OrderSpecification.hasDateFrom(from))
                .and(OrderSpecification.hasDateTo(to))
                .and(OrderSpecification.hasStatus(status))
                .and(OrderSpecification.hasPartnerId(partnerId));

        final Pageable pageable = PageRequest.of((int) pageNumber, (int) pageSize,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        return OrderMapper.toPage(orderJpaRepository.findByFilter(spec, pageable));
    }

}

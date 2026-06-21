package br.com.vps.consulting.b2b.management.order.infrastructure.persistence;

import br.com.vps.consulting.b2b.management.order.domain.Order;
import br.com.vps.consulting.b2b.management.order.domain.OrderId;
import br.com.vps.consulting.b2b.management.order.domain.OrderRepository;
import br.com.vps.consulting.b2b.management.order.domain.OrderStatus;
import br.com.vps.consulting.b2b.management.order.domain.projection.OrderProjection;
import br.com.vps.consulting.b2b.management.order.infrastructure.mapper.OrderMapper;
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

    @Override
    public Order save(final Order order) {
        final var orderSaved = orderJpaRepository.save(OrderMapper.toEntity(order));
        return OrderMapper.toDomain(orderSaved);
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

package br.com.vps.consulting.b2b.management.order.infrastructure.persistence.jpa;

import br.com.vps.consulting.b2b.management.order.domain.OrderStatus;
import br.com.vps.consulting.b2b.management.order.infrastructure.persistence.OrderEntity;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.UUID;

public final class OrderSpecification {

    private OrderSpecification() {}

    public static Specification<OrderEntity> hasDateFrom(final Instant from) {
        return (root, query, cb) ->
                from == null ? cb.conjunction() : cb.greaterThanOrEqualTo(root.get("createdAt"), from);
    }

    public static Specification<OrderEntity> hasDateTo(final Instant to) {
        return (root, query, cb) ->
                to == null ? cb.conjunction() : cb.lessThanOrEqualTo(root.get("createdAt"), to);
    }

    public static Specification<OrderEntity> hasStatus(final OrderStatus status) {
        return (root, query, cb) ->
                status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }

    public static Specification<OrderEntity> hasPartnerId(final UUID partnerId) {
        return (root, query, cb) ->
                partnerId == null ? cb.conjunction() : cb.equal(root.get("partnerId"), partnerId);
    }

}

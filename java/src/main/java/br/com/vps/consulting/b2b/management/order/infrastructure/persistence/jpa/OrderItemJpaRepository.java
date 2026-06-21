package br.com.vps.consulting.b2b.management.order.infrastructure.persistence.jpa;

import br.com.vps.consulting.b2b.management.order.infrastructure.persistence.OrderItemEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OrderItemJpaRepository extends JpaRepository<OrderItemEntity, UUID> {
    Page<OrderItemEntity> findByOrderId(UUID orderId, Pageable pageable);
}

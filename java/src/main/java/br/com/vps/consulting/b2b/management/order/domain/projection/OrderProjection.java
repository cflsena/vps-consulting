package br.com.vps.consulting.b2b.management.order.domain.projection;

import br.com.vps.consulting.b2b.management.order.domain.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public interface OrderProjection {
    UUID getId();
    UUID getPartnerId();
    BigDecimal getTotalAmount();
    OrderStatus getStatus();
    Instant getCreatedAt();
    Instant getUpdatedAt();
}

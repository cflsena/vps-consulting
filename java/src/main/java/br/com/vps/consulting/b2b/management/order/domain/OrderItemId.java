package br.com.vps.consulting.b2b.management.order.domain;

import br.com.vps.consulting.b2b.management.shared.core.entity.Identifier;

import java.util.Objects;
import java.util.UUID;

public record OrderItemId(UUID value) implements Identifier<UUID> {

    public OrderItemId {
        Objects.requireNonNull(value, "OrderItemId value é obrigatório");
    }

    public static OrderItemId generate() {
        return new OrderItemId(UUID.randomUUID());
    }

    public static OrderItemId from(final UUID uuid) {
        return new OrderItemId(uuid);
    }

    public static OrderItemId from(final String uuid) {
        return new OrderItemId(UUID.fromString(uuid));
    }

}

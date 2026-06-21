package br.com.vps.consulting.b2b.management.order.domain;

import br.com.vps.consulting.b2b.management.shared.core.entity.Identifier;

import java.util.Objects;
import java.util.UUID;

public record OrderId(UUID value) implements Identifier<UUID> {

    public OrderId {
        Objects.requireNonNull(value, "OrderId value é obrigatório");
    }

    public static OrderId generate() {
        return new OrderId(UUID.randomUUID());
    }

    public static OrderId from(final UUID uuid) {
        return new OrderId(uuid);
    }

    public static OrderId from(final String uuid) {
        return new OrderId(UUID.fromString(uuid));
    }

}

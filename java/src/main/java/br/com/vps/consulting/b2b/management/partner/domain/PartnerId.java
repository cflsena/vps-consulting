package br.com.vps.consulting.b2b.management.partner.domain;

import br.com.vps.consulting.b2b.management.shared.core.entity.Identifier;

import java.util.Objects;
import java.util.UUID;

public record PartnerId(UUID value) implements Identifier<UUID> {

    public PartnerId {
        Objects.requireNonNull(value, "PartnerId value is required");
    }

    public static PartnerId generate() {
        return new PartnerId(UUID.randomUUID());
    }

    public static PartnerId from(UUID uuid) {
        return new PartnerId(uuid);
    }

    public static PartnerId from(String uuid) {
        return new PartnerId(UUID.fromString(uuid));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}

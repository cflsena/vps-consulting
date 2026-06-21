package br.com.vps.consulting.b2b.management.shared.core.event;

import java.time.Instant;
import java.util.UUID;

public record EventMessage<T extends DomainEvent>(UUID eventId, Instant occurredAt, T payload) {

    public String eventType() {
        return payload.getClass().getSimpleName();
    }

    public static <T extends DomainEvent> EventMessage<T> of(final T event) {
        return new EventMessage<>(UUID.randomUUID(), Instant.now(), event);
    }

}

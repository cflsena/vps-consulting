package br.com.vps.consulting.b2b.management.shared.core.event;

import java.util.List;

public interface EventPublisher {
    void publish(DomainEvent event);
    default void publishAll(List<DomainEvent> events) {
        events.forEach(this::publish);
    }
}

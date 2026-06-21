package br.com.vps.consulting.b2b.management.shared.core.event;

public interface EventPublisher {
    void publish(DomainEvent event);
}

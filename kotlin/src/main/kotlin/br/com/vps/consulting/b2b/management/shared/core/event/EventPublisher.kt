package br.com.vps.consulting.b2b.management.shared.core.event

interface EventPublisher {
    fun publish(event: DomainEvent)
}

package br.com.vps.consulting.b2b.management.shared.core.event

import java.time.Instant
import java.util.*

data class EventMessage<T: DomainEvent>(
    val eventId: UUID = UUID.randomUUID(),
    val occurredAt: Instant = Instant.now(),
    val payload: T
)

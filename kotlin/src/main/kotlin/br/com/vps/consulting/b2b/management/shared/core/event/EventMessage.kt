package br.com.vps.consulting.b2b.management.shared.core.event

import java.time.Instant
import java.util.UUID

data class EventMessage<T: DomainEvent>(
    val eventId: UUID,
    val occurredAt: Instant,
    val payload: T
)

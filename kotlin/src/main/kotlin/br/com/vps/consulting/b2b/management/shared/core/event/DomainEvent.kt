package br.com.vps.consulting.b2b.management.shared.core.event

import java.util.UUID

interface DomainEvent {
    val aggregateId: UUID
}

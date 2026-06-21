package br.com.vps.consulting.b2b.management.partner.domain

import br.com.vps.consulting.b2b.management.shared.core.entity.Identifier
import java.util.*

@JvmInline
value class PartnerId(override val value: UUID) : Identifier<UUID> {
    companion object {
        fun generate() = PartnerId(UUID.randomUUID())
        fun from(value: String) = PartnerId(UUID.fromString(value))
        fun from(value: UUID) = PartnerId(value)
    }
}

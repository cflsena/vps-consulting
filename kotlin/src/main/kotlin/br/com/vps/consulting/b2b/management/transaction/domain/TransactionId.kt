package br.com.vps.consulting.b2b.management.transaction.domain

import br.com.vps.consulting.b2b.management.shared.core.entity.Identifier
import java.util.UUID

@JvmInline
value class TransactionId(override val value: UUID) : Identifier<UUID> {
    companion object {
        fun generate() = TransactionId(UUID.randomUUID())
        fun from(value: String) = TransactionId(UUID.fromString(value))
        fun from(value: UUID) = TransactionId(value)
    }
}

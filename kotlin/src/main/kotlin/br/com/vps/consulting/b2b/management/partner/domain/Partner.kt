package br.com.vps.consulting.b2b.management.partner.domain

import br.com.vps.consulting.b2b.management.shared.core.entity.Entity
import java.time.Instant

class Partner private constructor(
    id: PartnerId,
    val name: String,
    val document: String,
    val createdAt: Instant,
) : Entity<PartnerId>(id) {

    init {
        validate()
    }

    override fun validate() {
        require(name.isNotBlank()) { "O nome não pode estar em branco" }
        require(document.isNotBlank()) { "O documento não pode estar em branco" }
    }

    companion object {
        fun create(name: String, document: String): Partner = Partner(
            id = PartnerId.generate(),
            name = name,
            document = document,
            createdAt = Instant.now(),
        )

        fun with(id: PartnerId, name: String, document: String, createdAt: Instant) =
            Partner(id, name, document, createdAt)
    }

}

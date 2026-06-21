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
        require(name.isNotBlank()) { "name não pode estar em branco" }
        require(document.isNotBlank()) { "document não pode estar em branco" }
    }

    companion object {
        fun with(id: PartnerId? = null, name: String, document: String, createdAt: Instant = Instant.now()) =
            Partner(
                id = id ?: PartnerId.generate(),
                name = name,
                document = document,
                createdAt = createdAt
            )
    }

}

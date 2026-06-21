package br.com.vps.consulting.b2b.management.partner.infrastructure.persistence.jpa

import br.com.vps.consulting.b2b.management.partner.infrastructure.persistence.PartnerEntity
import org.springframework.data.jpa.domain.Specification

object PartnerSpecification {

    fun hasDocument(document: String?): Specification<PartnerEntity> =
        Specification { root, _, cb ->
            if (document == null) cb.conjunction() else cb.equal(root.get<String>("document"), document)
        }
}

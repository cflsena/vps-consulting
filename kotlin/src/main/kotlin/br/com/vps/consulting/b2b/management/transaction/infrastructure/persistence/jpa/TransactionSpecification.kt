package br.com.vps.consulting.b2b.management.transaction.infrastructure.persistence.jpa

import br.com.vps.consulting.b2b.management.transaction.domain.TransactionType
import br.com.vps.consulting.b2b.management.transaction.infrastructure.persistence.TransactionEntity
import org.springframework.data.jpa.domain.Specification
import java.time.Instant
import java.util.UUID

object TransactionSpecification {

    fun hasPartnerId(partnerId: UUID): Specification<TransactionEntity> =
        Specification { root, _, cb -> cb.equal(root.get<UUID>("partnerId"), partnerId) }

    fun hasDateFrom(from: Instant?): Specification<TransactionEntity> =
        Specification { root, _, cb ->
            if (from == null) cb.conjunction() else cb.greaterThanOrEqualTo(root.get("createdAt"), from)
        }

    fun hasDateTo(to: Instant?): Specification<TransactionEntity> =
        Specification { root, _, cb ->
            if (to == null) cb.conjunction() else cb.lessThanOrEqualTo(root.get("createdAt"), to)
        }

    fun hasType(type: TransactionType?): Specification<TransactionEntity> =
        Specification { root, _, cb ->
            if (type == null) cb.conjunction() else cb.equal(root.get<TransactionType>("type"), type)
        }
}

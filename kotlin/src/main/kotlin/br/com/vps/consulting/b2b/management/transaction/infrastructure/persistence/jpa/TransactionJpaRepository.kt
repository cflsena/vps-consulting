package br.com.vps.consulting.b2b.management.transaction.infrastructure.persistence.jpa

import br.com.vps.consulting.b2b.management.transaction.domain.TransactionStatus
import br.com.vps.consulting.b2b.management.transaction.infrastructure.persistence.TransactionEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.transaction.annotation.Transactional
import java.util.*

interface TransactionJpaRepository : JpaRepository<TransactionEntity, UUID>, JpaSpecificationExecutor<TransactionEntity> {

    @Transactional(readOnly = true)
    fun findByIdempotencyKey(idempotencyKey: String): TransactionEntity?

    @Transactional(readOnly = true)
    fun findByStatusOrderByCreatedAtAsc(status: TransactionStatus, pageable: Pageable): List<TransactionEntity>

}

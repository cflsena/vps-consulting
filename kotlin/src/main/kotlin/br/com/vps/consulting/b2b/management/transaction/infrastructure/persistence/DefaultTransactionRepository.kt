package br.com.vps.consulting.b2b.management.transaction.infrastructure.persistence

import br.com.vps.consulting.b2b.management.shared.core.page.PageCustom
import br.com.vps.consulting.b2b.management.transaction.domain.*
import br.com.vps.consulting.b2b.management.transaction.infrastructure.mapper.toDomain
import br.com.vps.consulting.b2b.management.transaction.infrastructure.mapper.toEntity
import br.com.vps.consulting.b2b.management.transaction.infrastructure.persistence.jpa.TransactionJpaRepository
import br.com.vps.consulting.b2b.management.transaction.infrastructure.persistence.jpa.TransactionSpecification
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*

@Component
class DefaultTransactionRepository(
    private val transactionJpaRepository: TransactionJpaRepository,
) : TransactionRepository {

    override fun save(transaction: Transaction): Transaction {
        transactionJpaRepository.save(transaction.toEntity())
        return transaction
    }

    override fun saveAll(processedTransactions: List<Transaction>) {
        transactionJpaRepository.saveAll(processedTransactions.map(Transaction::toEntity))
    }

    override fun findById(id: TransactionId): Transaction? =
        transactionJpaRepository.findById(id.value).map { it.toDomain() }.orElse(null)

    override fun findByIdempotencyKey(key: String): Transaction? =
        transactionJpaRepository.findByIdempotencyKey(key)?.toDomain()

    override fun findPending(limit: Int): List<Transaction> =
        transactionJpaRepository
            .findByStatusOrderByCreatedAtAsc(TransactionStatus.PENDING, PageRequest.of(0, limit))
            .map { it.toDomain() }

    override fun findByPartnerId(
        partnerId: UUID,
        from: Instant?,
        to: Instant?,
        type: TransactionType?,
        pageSize: Int,
        pageNumber: Int,
    ): PageCustom<Transaction> {
        val spec = Specification.where(TransactionSpecification.hasPartnerId(partnerId))
            .and(TransactionSpecification.hasDateFrom(from))
            .and(TransactionSpecification.hasDateTo(to))
            .and(TransactionSpecification.hasType(type))
        val pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"))
        val result = transactionJpaRepository.findAll(spec, pageable)
        return PageCustom(
            pageNumber = result.number,
            pageSize = result.size,
            totalPages = result.totalPages,
            totalElements = result.totalElements,
            items = result.content.map { it.toDomain() },
        )
    }
}

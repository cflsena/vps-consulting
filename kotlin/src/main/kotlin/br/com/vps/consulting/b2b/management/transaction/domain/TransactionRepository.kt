package br.com.vps.consulting.b2b.management.transaction.domain

import br.com.vps.consulting.b2b.management.partner.domain.PartnerId
import br.com.vps.consulting.b2b.management.shared.core.page.PageCustom
import java.time.Instant

interface TransactionRepository {
    fun save(transaction: Transaction): Transaction
    fun findById(id: TransactionId): Transaction?
    fun findByIdempotencyKey(key: String): Transaction?
    fun findPending(limit: Int): List<Transaction>
    fun findByPartnerId(
        partnerId: PartnerId,
        from: Instant?,
        to: Instant?,
        type: TransactionType?,
        pageSize: Int,
        pageNumber: Int
    ): PageCustom<Transaction>
}

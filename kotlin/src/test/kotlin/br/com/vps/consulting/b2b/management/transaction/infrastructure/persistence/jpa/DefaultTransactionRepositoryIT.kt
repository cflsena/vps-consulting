package br.com.vps.consulting.b2b.management.transaction.infrastructure.persistence.jpa

import br.com.vps.consulting.b2b.management.TestcontainersConfiguration
import br.com.vps.consulting.b2b.management.partner.infrastructure.persistence.PartnerEntity
import br.com.vps.consulting.b2b.management.partner.infrastructure.persistence.jpa.PartnerJpaRepository
import br.com.vps.consulting.b2b.management.shared.core.vo.Money
import br.com.vps.consulting.b2b.management.transaction.domain.Transaction
import br.com.vps.consulting.b2b.management.transaction.domain.TransactionId
import br.com.vps.consulting.b2b.management.transaction.domain.TransactionStatus
import br.com.vps.consulting.b2b.management.transaction.domain.TransactionType
import br.com.vps.consulting.b2b.management.transaction.infrastructure.mapper.toEntity
import br.com.vps.consulting.b2b.management.transaction.infrastructure.persistence.DefaultTransactionRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.context.annotation.Import
import org.springframework.dao.DataIntegrityViolationException
import java.math.BigDecimal
import java.time.Instant
import java.util.*

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration::class, DefaultTransactionRepository::class)
@ImportAutoConfiguration(FlywayAutoConfiguration::class)
class DefaultTransactionRepositoryIT {

    @Autowired
    lateinit var adapter: DefaultTransactionRepository

    @Autowired
    lateinit var transactionJpaRepository: TransactionJpaRepository

    @Autowired
    lateinit var partnerJpaRepository: PartnerJpaRepository

    @Test
    fun `should save and find transaction by id`() {
        val transaction = newTransaction(newPartnerId())

        adapter.save(transaction)
        val found = adapter.findById(transaction.id)

        assertThat(found).isNotNull
        assertThat(found!!.idempotencyKey).isEqualTo(transaction.idempotencyKey)
    }

    @Test
    fun `should return null when transaction not found by id`() {
        assertThat(adapter.findById(TransactionId.generate())).isNull()
    }

    @Test
    fun `should find transaction by idempotencyKey`() {
        val transaction = newTransaction(newPartnerId())
        adapter.save(transaction)

        val found = adapter.findByIdempotencyKey(transaction.idempotencyKey)

        assertThat(found).isNotNull
        assertThat(found!!.id.value).isEqualTo(transaction.id.value)
    }

    @Test
    fun `should return null when idempotencyKey not found`() {
        assertThat(adapter.findByIdempotencyKey("nao-existe")).isNull()
    }

    @Test
    fun `should findPending returning only PENDING transactions ordered by createdAt ascending respecting limit`() {
        val partnerId = newPartnerId()
        val oldest = newTransaction(partnerId, status = TransactionStatus.PENDING, createdAt = Instant.now().minusSeconds(120))
        val newest = newTransaction(partnerId, status = TransactionStatus.PENDING, createdAt = Instant.now().minusSeconds(10))
        val completed = newTransaction(partnerId, status = TransactionStatus.COMPLETED, createdAt = Instant.now().minusSeconds(60))
        adapter.save(oldest)
        adapter.save(newest)
        adapter.save(completed)

        val pending = adapter.findPending(limit = 10)

        assertThat(pending.map { it.id.value }).contains(oldest.id.value, newest.id.value)
        assertThat(pending.map { it.id.value }).doesNotContain(completed.id.value)
        assertThat(pending.indexOf(pending.first { it.id.value == oldest.id.value }))
            .isLessThan(pending.indexOf(pending.first { it.id.value == newest.id.value }))
    }

    @Test
    fun `should findByPartnerId returning only that partner's transactions`() {
        val partnerId = newPartnerId()
        val otherPartnerId = newPartnerId()
        val transaction = newTransaction(partnerId)
        adapter.save(transaction)
        adapter.save(newTransaction(otherPartnerId))

        val page = adapter.findByPartnerId(partnerId, null, null, null, 20, 0)

        assertThat(page.items).hasSize(1)
        assertThat(page.items[0].partnerId).isEqualTo(partnerId)
    }

    @Test
    fun `should findByPartnerId filtered by type`() {
        val partnerId = newPartnerId()
        adapter.save(newTransaction(partnerId, type = TransactionType.CREDIT))
        adapter.save(newTransaction(partnerId, type = TransactionType.DEBIT))

        val page = adapter.findByPartnerId(partnerId, null, null, TransactionType.CREDIT, 20, 0)

        assertThat(page.items).allMatch { it.type == TransactionType.CREDIT }
    }

    @Test
    fun `should findByPartnerId filtered by date range`() {
        val partnerId = newPartnerId()
        val inRange = newTransaction(partnerId, createdAt = Instant.now().minusSeconds(30))
        val outOfRange = newTransaction(partnerId, createdAt = Instant.now().minusSeconds(3600))
        adapter.save(inRange)
        adapter.save(outOfRange)

        val page = adapter.findByPartnerId(partnerId, Instant.now().minusSeconds(60), Instant.now(), null, 20, 0)

        assertThat(page.items.map { it.id.value }).contains(inRange.id.value)
        assertThat(page.items.map { it.id.value }).doesNotContain(outOfRange.id.value)
    }

    @Test
    fun `should findByPartnerId respecting pageSize and pageNumber`() {
        val partnerId = newPartnerId()
        repeat(3) { adapter.save(newTransaction(partnerId)) }

        val page = adapter.findByPartnerId(partnerId, null, null, null, 2, 0)

        assertThat(page.items).hasSize(2)
        assertThat(page.totalElements).isGreaterThanOrEqualTo(3L)
    }

    @Test
    fun `should enforce idempotencyKey uniqueness at the database level`() {
        val partnerId = newPartnerId()
        val key = "key-duplicada"
        val first = Transaction.createAsPending(partnerId, TransactionType.CREDIT, Money.of(BigDecimal("10.00")), "Compra", key)
        val second = Transaction.createAsPending(partnerId, TransactionType.CREDIT, Money.of(BigDecimal("10.00")), "Compra", key)
        transactionJpaRepository.saveAndFlush(first.toEntity())

        assertThatThrownBy { transactionJpaRepository.saveAndFlush(second.toEntity()) }
            .isInstanceOf(DataIntegrityViolationException::class.java)
    }

    private fun newPartnerId(): UUID {
        val id = UUID.randomUUID()
        partnerJpaRepository.save(
            PartnerEntity(
                id = id,
                name = "Acme Corp",
                document = UUID.randomUUID().toString().replace("-", "").take(14),
                createdAt = Instant.now(),
            )
        )
        return id
    }

    private fun newTransaction(
        partnerId: UUID,
        type: TransactionType = TransactionType.CREDIT,
        status: TransactionStatus = TransactionStatus.PENDING,
        createdAt: Instant = Instant.now(),
    ): Transaction = Transaction.with(
        id = TransactionId.generate(),
        partnerId = partnerId,
        type = type,
        amount = Money.of(BigDecimal("100.00")),
        description = "Compra de créditos",
        idempotencyKey = "key-${UUID.randomUUID()}",
        status = status,
        errorDescription = null,
        createdAt = createdAt,
        updatedAt = createdAt,
    )

}

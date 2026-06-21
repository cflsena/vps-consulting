package br.com.vps.consulting.b2b.management.partner.infrastructure.persistence.jpa

import br.com.vps.consulting.b2b.management.TestcontainersConfiguration
import br.com.vps.consulting.b2b.management.partner.infrastructure.persistence.PartnerBalanceEntity
import br.com.vps.consulting.b2b.management.partner.infrastructure.persistence.PartnerEntity
import br.com.vps.consulting.b2b.management.partner.infrastructure.persistence.DefaultPartnerRepository
import br.com.vps.consulting.b2b.management.partner.domain.PartnerId
import br.com.vps.consulting.b2b.management.shared.core.vo.Money
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.context.annotation.Import
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration::class, DefaultPartnerRepository::class)
@ImportAutoConfiguration(FlywayAutoConfiguration::class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class DefaultPartnerRepositoryConcurrencyIT {

    @Autowired
    lateinit var adapter: DefaultPartnerRepository

    @Autowired
    lateinit var partnerJpaRepository: PartnerJpaRepository

    @Autowired
    lateinit var partnerBalanceJpaRepository: PartnerBalanceJpaRepository

    private var partnerId: UUID? = null

    @AfterEach
    fun cleanup() {
        partnerId?.let {
            partnerBalanceJpaRepository.deleteById(it)
            partnerJpaRepository.deleteById(it)
        }
        partnerId = null
    }

    @Test
    fun `should not allow overdraft under 100 concurrent debit attempts and succeed exactly 50 times`() {
        val initialBalance = BigDecimal("1000.00")
        val amountPerRequest = Money.of(BigDecimal("20.00"))
        val totalAttempts = 100

        val id = setupPartner(initialBalance)
        val pid = PartnerId.from(id)

        val successCount = AtomicInteger(0)
        val startGate = CountDownLatch(1)
        val completionLatch = CountDownLatch(totalAttempts)
        val executor = Executors.newFixedThreadPool(20)

        repeat(totalAttempts) {
            executor.submit {
                try {
                    startGate.await()
                    if (adapter.debitBalance(pid, amountPerRequest)) {
                        successCount.incrementAndGet()
                    }
                } finally {
                    completionLatch.countDown()
                }
            }
        }

        startGate.countDown()
        assertThat(completionLatch.await(30, TimeUnit.SECONDS)).isTrue()
        executor.shutdown()

        val balance = partnerBalanceJpaRepository.findById(id).orElseThrow()

        assertThat(successCount.get()).isEqualTo(50)
        assertThat(balance.availableBalance).isEqualByComparingTo(BigDecimal.ZERO)
        assertThat(balance.availableBalance).isGreaterThanOrEqualTo(BigDecimal.ZERO)
        assertThat(balance.totalBalance).isEqualByComparingTo(initialBalance)
    }

    @Test
    fun `should accumulate all concurrent credits without lost updates`() {
        val amountPerRequest = Money.of(BigDecimal("10.00"))
        val totalAttempts = 100

        val id = setupPartner(BigDecimal.ZERO)
        val pid = PartnerId.from(id)

        val successCount = AtomicInteger(0)
        val startGate = CountDownLatch(1)
        val completionLatch = CountDownLatch(totalAttempts)
        val executor = Executors.newFixedThreadPool(20)

        repeat(totalAttempts) {
            executor.submit {
                try {
                    startGate.await()
                    if (adapter.creditBalance(pid, amountPerRequest)) {
                        successCount.incrementAndGet()
                    }
                } finally {
                    completionLatch.countDown()
                }
            }
        }

        startGate.countDown()
        assertThat(completionLatch.await(30, TimeUnit.SECONDS)).isTrue()
        executor.shutdown()

        val balance = partnerBalanceJpaRepository.findById(id).orElseThrow()
        val expectedTotal = amountPerRequest.value.multiply(BigDecimal(totalAttempts))

        assertThat(successCount.get()).isEqualTo(totalAttempts)
        assertThat(balance.totalBalance).isEqualByComparingTo(expectedTotal)
        assertThat(balance.availableBalance).isEqualByComparingTo(expectedTotal)
    }

    private fun setupPartner(initialBalance: BigDecimal): UUID {
        val id = UUID.randomUUID()
        partnerJpaRepository.save(
            PartnerEntity(
                id = id,
                name = "Concurrency Test Partner",
                document = UUID.randomUUID().toString().replace("-", "").take(14),
                createdAt = Instant.now(),
            )
        )
        partnerBalanceJpaRepository.save(
            PartnerBalanceEntity(
                partnerId = id,
                totalBalance = initialBalance,
                availableBalance = initialBalance,
                updatedAt = Instant.now(),
            )
        )
        partnerId = id
        return id
    }

}

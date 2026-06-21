package br.com.vps.consulting.b2b.management.partner.infrastructure.persistence.jpa

import br.com.vps.consulting.b2b.management.TestcontainersConfiguration
import br.com.vps.consulting.b2b.management.partner.domain.PartnerBalance
import br.com.vps.consulting.b2b.management.partner.domain.PartnerId
import br.com.vps.consulting.b2b.management.partner.infrastructure.persistence.DefaultPartnerBalanceRepository
import br.com.vps.consulting.b2b.management.partner.infrastructure.persistence.PartnerEntity
import br.com.vps.consulting.b2b.management.shared.core.vo.Money
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.context.annotation.Import
import java.math.BigDecimal
import java.time.Instant
import java.util.*

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration::class, DefaultPartnerBalanceRepository::class)
@ImportAutoConfiguration(FlywayAutoConfiguration::class)
class DefaultPartnerBalanceRepositoryIT {

    @Autowired
    lateinit var adapter: DefaultPartnerBalanceRepository

    @Autowired
    lateinit var partnerJpaRepository: PartnerJpaRepository

    private fun newPartnerId(): PartnerId {
        val id = UUID.randomUUID()
        partnerJpaRepository.save(
            PartnerEntity(
                id = id,
                name = "Acme Corp",
                document = UUID.randomUUID().toString().replace("-", "").take(14),
                createdAt = Instant.now(),
            )
        )
        return PartnerId.from(id)
    }

    @Test
    fun `should save and find balance by id`() {
        val partnerId = newPartnerId()
        adapter.save(PartnerBalance.with(id = partnerId, availableBalance = BigDecimal("100.00")))

        val balance = adapter.findBalanceById(partnerId)

        assertThat(balance).isNotNull
        assertThat(balance!!.totalCredited.value).isEqualByComparingTo("0.00")
        assertThat(balance.totalDebited.value).isEqualByComparingTo("0.00")
        assertThat(balance.availableBalance.value).isEqualByComparingTo("100.00")
    }

    @Test
    fun `should return null when balance not found by id`() {
        assertThat(adapter.findBalanceById(PartnerId.generate())).isNull()
    }

    @Test
    fun `should creditBalance increase both totalCredited and availableBalance and return true`() {
        val partnerId = newPartnerId()
        adapter.save(PartnerBalance.with(id = partnerId))

        val result = adapter.creditBalance(partnerId, Money.of(BigDecimal("50.00")))

        assertThat(result).isTrue()
        val balance = adapter.findBalanceById(partnerId)!!
        assertThat(balance.totalCredited.value).isEqualByComparingTo("50.00")
        assertThat(balance.availableBalance.value).isEqualByComparingTo("50.00")
    }

    @Test
    fun `should debitBalance decrease availableBalance and increase totalDebited when sufficient balance`() {
        val partnerId = newPartnerId()
        adapter.save(PartnerBalance.with(id = partnerId))
        adapter.creditBalance(partnerId, Money.of(BigDecimal("100.00")))

        val result = adapter.debitBalance(partnerId, Money.of(BigDecimal("30.00")))

        assertThat(result).isTrue()
        val balance = adapter.findBalanceById(partnerId)!!
        assertThat(balance.availableBalance.value).isEqualByComparingTo("70.00")
        assertThat(balance.totalCredited.value).isEqualByComparingTo("100.00")
        assertThat(balance.totalDebited.value).isEqualByComparingTo("30.00")
    }

    @Test
    fun `should debitBalance return false and leave balance unchanged when insufficient balance`() {
        val partnerId = newPartnerId()
        adapter.save(PartnerBalance.with(id = partnerId))
        adapter.creditBalance(partnerId, Money.of(BigDecimal("10.00")))

        val result = adapter.debitBalance(partnerId, Money.of(BigDecimal("50.00")))

        assertThat(result).isFalse()
        val balance = adapter.findBalanceById(partnerId)!!
        assertThat(balance.availableBalance.value).isEqualByComparingTo("10.00")
    }

    @Test
    fun `should creditBalance return false when partner balance row does not exist`() {
        val result = adapter.creditBalance(PartnerId.generate(), Money.of(BigDecimal("10.00")))

        assertThat(result).isFalse()
    }

}

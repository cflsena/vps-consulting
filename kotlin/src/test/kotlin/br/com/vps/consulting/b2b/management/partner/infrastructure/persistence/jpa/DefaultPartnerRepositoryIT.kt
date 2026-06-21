package br.com.vps.consulting.b2b.management.partner.infrastructure.persistence.jpa

import br.com.vps.consulting.b2b.management.TestcontainersConfiguration
import br.com.vps.consulting.b2b.management.partner.domain.Partner
import br.com.vps.consulting.b2b.management.partner.domain.PartnerId
import br.com.vps.consulting.b2b.management.partner.infrastructure.persistence.DefaultPartnerRepository
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
import java.util.UUID

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration::class, DefaultPartnerRepository::class)
@ImportAutoConfiguration(FlywayAutoConfiguration::class)
class DefaultPartnerRepositoryIT {

    @Autowired
    lateinit var adapter: DefaultPartnerRepository

    @Autowired
    lateinit var partnerJpaRepository: PartnerJpaRepository

    @Autowired
    lateinit var partnerBalanceJpaRepository: PartnerBalanceJpaRepository

    private fun newPartner() = Partner.create(name = "Acme Corp", document = UUID.randomUUID().toString().replace("-", "").take(14))

    @Test
    fun `should save partner and create a zero-balance row`() {
        val partner = newPartner()

        adapter.save(partner)

        assertThat(partnerJpaRepository.findById(partner.id.value)).isPresent
        val balance = partnerBalanceJpaRepository.findById(partner.id.value).orElseThrow()
        assertThat(balance.totalBalance).isEqualByComparingTo(BigDecimal.ZERO)
        assertThat(balance.availableBalance).isEqualByComparingTo(BigDecimal.ZERO)
    }

    @Test
    fun `should find partner by id`() {
        val partner = newPartner()
        adapter.save(partner)

        val found = adapter.findById(partner.id)

        assertThat(found).isNotNull
        assertThat(found!!.document).isEqualTo(partner.document)
    }

    @Test
    fun `should return null when partner not found by id`() {
        assertThat(adapter.findById(PartnerId.generate())).isNull()
    }

    @Test
    fun `should find balance by id returning PartnerBalance with matching values`() {
        val partner = newPartner()
        adapter.save(partner)
        adapter.creditBalance(partner.id, Money.of(BigDecimal("100.00")))

        val balance = adapter.findBalanceById(partner.id)

        assertThat(balance).isNotNull
        assertThat(balance!!.totalBalance.value).isEqualByComparingTo("100.00")
        assertThat(balance.availableBalance.value).isEqualByComparingTo("100.00")
    }

    @Test
    fun `should return null when balance not found by id`() {
        assertThat(adapter.findBalanceById(PartnerId.generate())).isNull()
    }

    @Test
    fun `should findAll with no document filter returning all partners ordered by createdAt desc`() {
        val first = newPartner()
        adapter.save(first)
        val second = newPartner()
        adapter.save(second)

        val page = adapter.findAll(document = null, pageSize = 20, pageNumber = 0)

        assertThat(page.items.map { it.id.value }).contains(first.id.value, second.id.value)
    }

    @Test
    fun `should findAll filtered by document returning only matching partner`() {
        val partner = newPartner()
        adapter.save(partner)
        adapter.save(newPartner())

        val page = adapter.findAll(document = partner.document, pageSize = 20, pageNumber = 0)

        assertThat(page.items).hasSize(1)
        assertThat(page.items[0].document).isEqualTo(partner.document)
    }

    @Test
    fun `should findAll respecting pageSize and pageNumber`() {
        repeat(3) { adapter.save(newPartner()) }

        val page = adapter.findAll(document = null, pageSize = 2, pageNumber = 0)

        assertThat(page.items).hasSize(2)
        assertThat(page.totalElements).isGreaterThanOrEqualTo(3L)
    }

    @Test
    fun `should creditBalance increase both totalBalance and availableBalance and return true`() {
        val partner = newPartner()
        adapter.save(partner)

        val result = adapter.creditBalance(partner.id, Money.of(BigDecimal("50.00")))

        assertThat(result).isTrue()
        val balance = adapter.findBalanceById(partner.id)!!
        assertThat(balance.totalBalance.value).isEqualByComparingTo("50.00")
        assertThat(balance.availableBalance.value).isEqualByComparingTo("50.00")
    }

    @Test
    fun `should debitBalance decrease availableBalance only and return true when sufficient balance`() {
        val partner = newPartner()
        adapter.save(partner)
        adapter.creditBalance(partner.id, Money.of(BigDecimal("100.00")))

        val result = adapter.debitBalance(partner.id, Money.of(BigDecimal("30.00")))

        assertThat(result).isTrue()
        val balance = adapter.findBalanceById(partner.id)!!
        assertThat(balance.availableBalance.value).isEqualByComparingTo("70.00")
        assertThat(balance.totalBalance.value).isEqualByComparingTo("100.00")
    }

    @Test
    fun `should debitBalance return false and leave balance unchanged when insufficient balance`() {
        val partner = newPartner()
        adapter.save(partner)
        adapter.creditBalance(partner.id, Money.of(BigDecimal("10.00")))

        val result = adapter.debitBalance(partner.id, Money.of(BigDecimal("50.00")))

        assertThat(result).isFalse()
        val balance = adapter.findBalanceById(partner.id)!!
        assertThat(balance.availableBalance.value).isEqualByComparingTo("10.00")
    }

    @Test
    fun `should creditBalance return false when partner balance row does not exist`() {
        val result = adapter.creditBalance(PartnerId.generate(), Money.of(BigDecimal("10.00")))

        assertThat(result).isFalse()
    }

}

package br.com.vps.consulting.b2b.management.partner.infrastructure.persistence

import br.com.vps.consulting.b2b.management.partner.domain.PartnerBalance
import br.com.vps.consulting.b2b.management.partner.domain.PartnerBalanceRepository
import br.com.vps.consulting.b2b.management.partner.domain.PartnerId
import br.com.vps.consulting.b2b.management.partner.infrastructure.mapper.toDomain
import br.com.vps.consulting.b2b.management.partner.infrastructure.mapper.toEntity
import br.com.vps.consulting.b2b.management.partner.infrastructure.persistence.jpa.PartnerBalanceJpaRepository
import br.com.vps.consulting.b2b.management.shared.core.vo.Money
import org.springframework.stereotype.Component

@Component
class DefaultPartnerBalanceRepository(
    private val repository: PartnerBalanceJpaRepository,
) : PartnerBalanceRepository {

    override fun save(partnerBalance: PartnerBalance) {
        repository.save(partnerBalance.toEntity())
    }

    override fun findBalanceById(id: PartnerId): PartnerBalance? =
        repository.findById(id.value).map { it.toDomain() }.orElse(null)

    override fun creditBalance(partnerId: PartnerId, amount: Money): Boolean =
        repository.creditBalance(partnerId.value, amount.value) > 0

    override fun debitBalance(partnerId: PartnerId, amount: Money): Boolean =
        repository.debitBalance(partnerId.value, amount.value) > 0

}

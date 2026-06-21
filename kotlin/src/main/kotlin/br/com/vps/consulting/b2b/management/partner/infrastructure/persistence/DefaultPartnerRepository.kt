package br.com.vps.consulting.b2b.management.partner.infrastructure.persistence

import br.com.vps.consulting.b2b.management.partner.domain.Partner
import br.com.vps.consulting.b2b.management.partner.domain.PartnerBalance
import br.com.vps.consulting.b2b.management.partner.domain.PartnerId
import br.com.vps.consulting.b2b.management.partner.domain.PartnerRepository
import br.com.vps.consulting.b2b.management.partner.infrastructure.mapper.toDomain
import br.com.vps.consulting.b2b.management.partner.infrastructure.mapper.toEntity
import br.com.vps.consulting.b2b.management.partner.infrastructure.mapper.zeroBalanceEntityFor
import br.com.vps.consulting.b2b.management.partner.infrastructure.persistence.jpa.PartnerBalanceJpaRepository
import br.com.vps.consulting.b2b.management.partner.infrastructure.persistence.jpa.PartnerJpaRepository
import br.com.vps.consulting.b2b.management.partner.infrastructure.persistence.jpa.PartnerSpecification
import br.com.vps.consulting.b2b.management.shared.core.page.PageCustom
import br.com.vps.consulting.b2b.management.shared.core.vo.Money
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Component

@Component
class DefaultPartnerRepository(
    private val partnerJpaRepository: PartnerJpaRepository,
    private val partnerBalanceJpaRepository: PartnerBalanceJpaRepository,
) : PartnerRepository {

    override fun save(partner: Partner): Partner {
        partnerJpaRepository.save(partner.toEntity())
        partnerBalanceJpaRepository.save(zeroBalanceEntityFor(partner.id.value))
        return partner
    }

    override fun findById(id: PartnerId): Partner? =
        partnerJpaRepository.findById(id.value).map { it.toDomain() }.orElse(null)

    override fun findBalanceById(id: PartnerId): PartnerBalance? =
        partnerBalanceJpaRepository.findById(id.value).map { it.toDomain() }.orElse(null)

    override fun findAll(document: String?, pageSize: Int, pageNumber: Int): PageCustom<Partner> {
        val spec = Specification.where(PartnerSpecification.hasDocument(document))
        val pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"))
        val result = partnerJpaRepository.findAll(spec, pageable)
        return PageCustom(
            pageNumber = result.number,
            pageSize = result.size,
            totalPages = result.totalPages,
            totalElements = result.totalElements,
            items = result.content.map { it.toDomain() },
        )
    }

    override fun creditBalance(partnerId: PartnerId, amount: Money): Boolean =
        partnerBalanceJpaRepository.creditBalance(partnerId.value, amount.value) > 0

    override fun debitBalance(partnerId: PartnerId, amount: Money): Boolean =
        partnerBalanceJpaRepository.debitBalance(partnerId.value, amount.value) > 0
}

package br.com.vps.consulting.b2b.management.partner.infrastructure.service

import br.com.vps.consulting.b2b.management.partner.domain.PartnerId
import br.com.vps.consulting.b2b.management.partner.domain.PartnerRepository
import br.com.vps.consulting.b2b.management.partner.domain.exception.PartnerNotFoundException
import br.com.vps.consulting.b2b.management.shared.core.vo.Money
import br.com.vps.consulting.b2b.management.transaction.application.service.PartnerService
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class PartnerServiceImpl(
    private val partnerRepository: PartnerRepository,
) : PartnerService {

    override fun existsById(partnerId: UUID): Boolean =
        partnerRepository.findById(PartnerId.from(partnerId)) != null

    override fun debitBalance(partnerId: UUID, amount: Money): Boolean {
        val partnerId = PartnerId.from(partnerId)
        partnerRepository.findById(partnerId) ?: throw PartnerNotFoundException(partnerId)
        return partnerRepository.debitBalance(partnerId, amount)
    }

    override fun creditBalance(partnerId: UUID, amount: Money): Boolean {
        val partnerId = PartnerId.from(partnerId)
        partnerRepository.findById(partnerId) ?: throw PartnerNotFoundException(partnerId)
        return partnerRepository.creditBalance(partnerId, amount)
    }

    override fun findBalanceById(partnerId: UUID): Money =
        partnerRepository.findBalanceById(PartnerId.from(partnerId))?.availableBalance
            ?: throw PartnerNotFoundException(PartnerId.from(partnerId))
}

package br.com.vps.consulting.b2b.management.partner.infrastructure.service

import br.com.vps.consulting.b2b.management.partner.domain.PartnerBalanceRepository
import br.com.vps.consulting.b2b.management.partner.domain.PartnerId
import br.com.vps.consulting.b2b.management.partner.domain.PartnerRepository
import br.com.vps.consulting.b2b.management.partner.domain.exception.PartnerNotFoundException
import br.com.vps.consulting.b2b.management.shared.core.vo.Money
import br.com.vps.consulting.b2b.management.transaction.application.service.PartnerService
import org.springframework.stereotype.Component
import java.util.*

@Component
class PartnerServiceImpl(
    private val partnerRepository: PartnerRepository,
    private val partnerBalanceRepository: PartnerBalanceRepository,
) : PartnerService {

    override fun existsById(partnerId: UUID): Boolean =
        partnerRepository.findById(PartnerId.from(partnerId)) != null

    override fun debitBalance(partnerId: UUID, amount: Money): Boolean {
        val partnerId = PartnerId.from(partnerId)
        partnerRepository.findById(partnerId) ?: throw PartnerNotFoundException(partnerId)
        return partnerBalanceRepository.debitBalance(partnerId, amount)
    }

    override fun creditBalance(partnerId: UUID, amount: Money): Boolean {
        val partnerId = PartnerId.from(partnerId)
        partnerRepository.findById(partnerId) ?: throw PartnerNotFoundException(partnerId)
        return partnerBalanceRepository.creditBalance(partnerId, amount)
    }

    override fun findBalanceById(partnerId: UUID): Money =
        partnerBalanceRepository.findBalanceById(PartnerId.from(partnerId))?.availableBalance
            ?: throw PartnerNotFoundException(PartnerId.from(partnerId))
}

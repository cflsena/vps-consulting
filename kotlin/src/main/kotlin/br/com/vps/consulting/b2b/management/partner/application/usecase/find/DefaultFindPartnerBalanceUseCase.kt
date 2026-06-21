package br.com.vps.consulting.b2b.management.partner.application.usecase.find

import br.com.vps.consulting.b2b.management.partner.domain.PartnerBalanceRepository
import br.com.vps.consulting.b2b.management.partner.domain.PartnerId
import br.com.vps.consulting.b2b.management.partner.domain.exception.PartnerNotFoundException
import jakarta.inject.Named
import org.slf4j.LoggerFactory

@Named
class DefaultFindPartnerBalanceUseCase(
    private val partnerBalanceRepository: PartnerBalanceRepository,
) : FindPartnerBalanceUseCase {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun execute(input: FindPartnerBalanceInput): FindPartnerBalanceOutput {
        val partnerId = PartnerId.from(input.partnerId)
        log.info("Querying balance for partner ${input.partnerId}")
        val partnerBalance = partnerBalanceRepository.findBalanceById(partnerId)
            ?: run {
                log.error("Balance not found for partner ${partnerId.value}")
                throw PartnerNotFoundException(partnerId)
            }
        return FindPartnerBalanceOutput.from(partnerBalance)
    }

}

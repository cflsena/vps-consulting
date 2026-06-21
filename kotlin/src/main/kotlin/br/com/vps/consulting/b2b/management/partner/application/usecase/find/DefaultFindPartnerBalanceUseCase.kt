package br.com.vps.consulting.b2b.management.partner.application.usecase.find

import br.com.vps.consulting.b2b.management.partner.domain.PartnerId
import br.com.vps.consulting.b2b.management.partner.domain.PartnerRepository
import br.com.vps.consulting.b2b.management.partner.domain.exception.PartnerNotFoundException
import jakarta.inject.Named
import org.slf4j.LoggerFactory

@Named
class DefaultFindPartnerBalanceUseCase(
    private val partnerRepository: PartnerRepository,
) : FindPartnerBalanceUseCase {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun execute(input: FindPartnerBalanceInput): FindPartnerBalanceOutput {
        logger.debug("Consultando saldo do parceiro {}", input.partnerId)

        val balance = partnerRepository.findBalanceById(PartnerId.from(input.partnerId))
            ?: run {
                logger.warn("Saldo não encontrado para o parceiro {}", input.partnerId)
                throw PartnerNotFoundException(PartnerId.from(input.partnerId))
            }

        logger.debug("Saldo do parceiro {} consultado com sucesso", input.partnerId)
        return FindPartnerBalanceOutput(
            partnerId = balance.partnerId.value,
            totalBalance = balance.totalBalance.amount,
            availableBalance = balance.availableBalance.amount,
            updatedAt = balance.updatedAt,
        )
    }
}

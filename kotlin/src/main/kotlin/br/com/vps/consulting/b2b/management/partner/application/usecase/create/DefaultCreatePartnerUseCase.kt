package br.com.vps.consulting.b2b.management.partner.application.usecase.create

import br.com.vps.consulting.b2b.management.partner.domain.Partner
import br.com.vps.consulting.b2b.management.partner.domain.PartnerBalance
import br.com.vps.consulting.b2b.management.partner.domain.PartnerBalanceRepository
import br.com.vps.consulting.b2b.management.partner.domain.PartnerRepository
import jakarta.inject.Named
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import java.util.*

@Named
class DefaultCreatePartnerUseCase(
    private val partnerRepository: PartnerRepository,
    private val partnerBalanceRepository: PartnerBalanceRepository
) : CreatePartnerUseCase {

    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    override fun execute(input: CreatePartnerInput): UUID {
        log.info("Criando parceiro [nome=${input.name}, documento=${input.document}]")
        val partnerCreated = partnerRepository.save(Partner.with(name = input.name, document = input.document))
        partnerBalanceRepository.save(PartnerBalance.with(id = partnerCreated.id))
        log.info("Parceiro ${partnerCreated.id.value} criado com sucesso e saldo inicial zerado")
        return partnerCreated.id.value
    }

}

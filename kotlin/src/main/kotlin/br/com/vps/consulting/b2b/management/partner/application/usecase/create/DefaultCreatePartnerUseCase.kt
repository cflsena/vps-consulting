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
        log.info("Creating partner [name=${input.name}, document=${input.document}]")
        val partnerCreated = partnerRepository.save(Partner.with(name = input.name, document = input.document))
        partnerBalanceRepository.save(PartnerBalance.with(id = partnerCreated.id))
        log.info("Partner ${partnerCreated.id.value} created successfully with zeroed initial balance")
        return partnerCreated.id.value
    }

}

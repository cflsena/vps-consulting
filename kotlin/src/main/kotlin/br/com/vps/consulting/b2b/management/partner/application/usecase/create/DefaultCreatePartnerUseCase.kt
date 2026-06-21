package br.com.vps.consulting.b2b.management.partner.application.usecase.create

import br.com.vps.consulting.b2b.management.partner.domain.Partner
import br.com.vps.consulting.b2b.management.partner.domain.PartnerRepository
import jakarta.inject.Named
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import java.util.UUID

@Named
open class DefaultCreatePartnerUseCase(
    private val partnerRepository: PartnerRepository,
) : CreatePartnerUseCase {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    override fun execute(input: CreatePartnerInput): UUID {
        logger.info("Criando parceiro [nome=${input.name}, documento=${input.document}]")

        val partner = Partner.create(name = input.name, document = input.document)
        val saved = partnerRepository.save(partner)

        logger.info("Parceiro ${saved.id.value} criado com sucesso e saldo inicial zerado")
        return saved.id.value
    }
}

package br.com.vps.consulting.b2b.management.partner.application.usecase.list

import br.com.vps.consulting.b2b.management.partner.domain.PartnerRepository
import br.com.vps.consulting.b2b.management.shared.core.page.PageCustom
import jakarta.inject.Named
import org.slf4j.LoggerFactory

@Named
class DefaultListPartnersUseCase(
    private val partnerRepository: PartnerRepository,
) : ListPartnersUseCase {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun execute(input: ListPartnersInput): PageCustom<ListPartnersOutput> {
        log.info("Listando parceiros [documento=${input.document}, pagina=${input.pageNumber}, tamanho=${input.pageSize}]")
        val page = partnerRepository.findAll(input.document, input.pageSize, input.pageNumber)
        return ListPartnersOutput.from(page)
    }

}

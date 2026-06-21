package br.com.vps.consulting.b2b.management.transaction.application.usecase.history

import br.com.vps.consulting.b2b.management.shared.core.exception.DomainException
import br.com.vps.consulting.b2b.management.transaction.application.service.PartnerService
import br.com.vps.consulting.b2b.management.transaction.domain.exception.TransactionPartnerNotFoundException
import jakarta.inject.Named
import org.slf4j.LoggerFactory

@Named
class ListTransactionHistoryValidator(
    private val partnerService: PartnerService,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun validate(input: ListTransactionHistoryInput) {

        partnerService.existsById(input.partnerId).takeIf { it } ?: run {
            log.error("Parceiro ${input.partnerId} não encontrado ao processar transação")
            throw TransactionPartnerNotFoundException(input.partnerId)
        }

        if (input.from != null && input.to != null && input.from.isAfter(input.to)) {
            log.error("Intervalo de datas inválido: de ${input.from} é posterior a até ${input.to}")
            throw DomainException("A data inicial não pode ser posterior à data final")
        }

    }

}

package br.com.vps.consulting.b2b.management.transaction.application.usecase.history

import br.com.vps.consulting.b2b.management.shared.core.page.PageCustom
import br.com.vps.consulting.b2b.management.transaction.domain.TransactionRepository
import jakarta.inject.Named
import org.slf4j.LoggerFactory

@Named
class DefaultListTransactionHistoryUseCase(
    private val validator: ListTransactionHistoryValidator,
    private val transactionRepository: TransactionRepository,
) : ListTransactionHistoryUseCase {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun execute(input: ListTransactionHistoryInput): PageCustom<ListTransactionHistoryOutput> {

        log.info(
            "Consultando histórico de transações [parceiro=${input.partnerId}, de=${input.from}, " +
                "até=${input.to}, tipo=${input.type}, pagina=${input.pageNumber}, tamanho=${input.pageSize}]"
        )

        validator.validate(input)

        val page = transactionRepository.findByPartnerId(
            partnerId = input.partnerId,
            from = input.from,
            to = input.to,
            type = input.type,
            pageSize = input.pageSize,
            pageNumber = input.pageNumber,
        )

        return ListTransactionHistoryOutput.from(page).also {
            log.info(
                "Histórico de transações [parceiro=${input.partnerId}, de=${input.from}, " +
                    "até=${input.to}, tipo=${input.type}, pagina=${input.pageNumber}, " +
                    "tamanho=${input.pageSize}] consultado com sucesso"
            )
        }

    }
}

package br.com.vps.consulting.b2b.management.transaction.application.usecase.create

import br.com.vps.consulting.b2b.management.transaction.application.service.PartnerService
import br.com.vps.consulting.b2b.management.transaction.domain.TransactionRepository
import br.com.vps.consulting.b2b.management.transaction.domain.exception.DuplicateTransactionException
import br.com.vps.consulting.b2b.management.transaction.domain.exception.InvalidIdempotencyKeyException
import br.com.vps.consulting.b2b.management.transaction.domain.exception.InvalidTransactionAmountException
import br.com.vps.consulting.b2b.management.transaction.domain.exception.TransactionPartnerNotFoundException
import jakarta.inject.Named
import org.slf4j.LoggerFactory
import java.math.BigDecimal

@Named
class CreateTransactionValidator(
    private val partnerService: PartnerService,
    private val transactionRepository: TransactionRepository,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun validate(input: CreateTransactionInput) {

        if (input.idempotencyKey.isBlank()) {
            throw InvalidIdempotencyKeyException(input.idempotencyKey)
        }

        transactionRepository.findByIdempotencyKey(input.idempotencyKey)?.let {
            logger.error("Transação já processada para a chave de idempotência ${input.idempotencyKey}")
            throw DuplicateTransactionException(input.idempotencyKey)
        }

        if (input.amount <= BigDecimal.ZERO) {
            logger.error("Valor da transação inválido para o parceiro ${input.partnerId}: ${input.amount}")
            throw InvalidTransactionAmountException("O valor deve ser positivo")
        }

        partnerService.existsById(input.partnerId).takeIf { it } ?: run {
            logger.error("Parceiro ${input.partnerId} não encontrado ao processar transação")
            throw TransactionPartnerNotFoundException(input.partnerId)
        }

    }

}

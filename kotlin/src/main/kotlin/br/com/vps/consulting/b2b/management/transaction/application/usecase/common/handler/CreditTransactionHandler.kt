package br.com.vps.consulting.b2b.management.transaction.application.usecase.common.handler

import br.com.vps.consulting.b2b.management.transaction.application.service.PartnerService
import br.com.vps.consulting.b2b.management.transaction.domain.Transaction
import br.com.vps.consulting.b2b.management.transaction.domain.TransactionType
import br.com.vps.consulting.b2b.management.transaction.domain.exception.InvalidCreditAmountException
import jakarta.inject.Named
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import java.math.BigDecimal

@Named
class CreditTransactionHandler(
    private val partnerService: PartnerService,
    override val type: TransactionType = TransactionType.CREDIT,
) : TransactionHandler {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    override fun successfullyProcessed(transaction: Transaction): Boolean {
        logger.info("Iniciando reconciliação de crédito. Transação: [id=${transaction.id}, amount=${transaction.amount}]")
        validate(transaction)
        return partnerService.creditBalance(transaction.partnerId, transaction.amount)
    }

    private fun validate(transaction: Transaction) {
        if (transaction.amount.value <= BigDecimal.ZERO) {
            logger.error("Valor de crédito inválido para o parceiro ${transaction.partnerId}: ${transaction.amount}")
            throw InvalidCreditAmountException("Valor de crédito inválido. O valor deve ser positivo.")
        }
    }

}
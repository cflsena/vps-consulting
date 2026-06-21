package br.com.vps.consulting.b2b.management.transaction.application.usecase.common.handler

import br.com.vps.consulting.b2b.management.transaction.application.service.PartnerService
import br.com.vps.consulting.b2b.management.transaction.domain.Transaction
import br.com.vps.consulting.b2b.management.transaction.domain.TransactionType
import br.com.vps.consulting.b2b.management.transaction.domain.exception.InsufficientBalanceException
import br.com.vps.consulting.b2b.management.transaction.domain.exception.InvalidDebitAmountException
import jakarta.inject.Named
import org.slf4j.LoggerFactory
import java.math.BigDecimal

@Named
class DebitTransactionHandler(
    private val partnerService: PartnerService,
    override val type: TransactionType = TransactionType.DEBIT,
) : TransactionHandler {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun successfullyProcessed(transaction: Transaction): Boolean {
        logger.info("Iniciando reconciliação de débito. Transação: [id=${transaction.id}, amount=${transaction.amount}]")
        validate(transaction)
        return partnerService.debitBalance(transaction.partnerId, transaction.amount)
    }
    
    private fun validate(transaction: Transaction) {

        val partnerId = transaction.partnerId
        val amount = transaction.amount

        if (amount.value <= BigDecimal.ZERO) {
            logger.error("Valor de débito inválido para o parceiro $partnerId: $amount")
            throw InvalidDebitAmountException("Valor de débito inválido. O valor deve ser positivo")
        }

        val availableBalance = partnerService.findBalanceById(partnerId)

        if (amount.isGreaterThan(availableBalance)) {
            logger.error(
                "Saldo insuficiente para o parceiro $partnerId: solicitado $amount, disponível ${availableBalance.value}"
            )
            throw InsufficientBalanceException(partnerId, amount, availableBalance)
        }

    }

}
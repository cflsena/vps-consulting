package br.com.vps.consulting.b2b.management.transaction.application.usecase.reconciliation

import br.com.vps.consulting.b2b.management.shared.core.event.EventPublisher
import br.com.vps.consulting.b2b.management.shared.core.exception.BaseException
import br.com.vps.consulting.b2b.management.transaction.application.usecase.common.handler.TransactionHandler
import br.com.vps.consulting.b2b.management.transaction.domain.Transaction
import br.com.vps.consulting.b2b.management.transaction.domain.TransactionRepository
import br.com.vps.consulting.b2b.management.transaction.domain.TransactionStatus
import br.com.vps.consulting.b2b.management.transaction.domain.event.TransactionStatusChanged
import jakarta.inject.Named
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory

@Named
open class DefaultReconcileTransactionsUseCase(
    private val transactionRepository: TransactionRepository,
    private val eventPublisher: EventPublisher,
    handlers: List<TransactionHandler>
) : ReconcileTransactionsUseCase {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val handlersByType = handlers.associateBy { it.type }

    @Transactional
    override fun execute() {

        val pendingTransactions = transactionRepository.findPending(limit = 100)
        logger.info("Reconciliação: ${pendingTransactions.size} transações pendentes encontradas")

        pendingTransactions.forEach { transaction ->
            val strategy = handlersByType.getOrDefault(transaction.type, null) ?: return@forEach
            processTransaction(strategy, transaction)
            transactionRepository.save(transaction)
            if (transaction.status != TransactionStatus.PENDING) {
                eventPublisher.publish(
                    TransactionStatusChanged(
                        aggregateId = transaction.id.value,
                        partnerId = transaction.partnerId,
                        type = transaction.type,
                        amount = transaction.amount,
                        status = transaction.status,
                    )
                )
            }
        }

    }

    private fun processTransaction(strategy: TransactionHandler, transaction: Transaction) {
        try {
            if (strategy.successfullyProcessed(transaction)) {
                transaction.complete()
            } else {
                transaction.fail("Operação de saldo não confirmada")
            }
            logger.info("Transação ${transaction.id.value} reconciliada com status ${transaction.status}")
        } catch (e: BaseException) {
            logger.warn("Falha de negócio ao reconciliar transação ${transaction.id.value}: ${e.message}")
            transaction.fail(e.message)
        } catch (e: Exception) {
            logger.warn("Falha inesperada ao reconciliar transação ${transaction.id.value}", e)
        }
    }

}

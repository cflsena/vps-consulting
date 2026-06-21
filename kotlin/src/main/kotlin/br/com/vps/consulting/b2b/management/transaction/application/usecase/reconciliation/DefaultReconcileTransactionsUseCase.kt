package br.com.vps.consulting.b2b.management.transaction.application.usecase.reconciliation

import br.com.vps.consulting.b2b.management.shared.core.event.EventPublisher
import br.com.vps.consulting.b2b.management.shared.core.exception.BaseException
import br.com.vps.consulting.b2b.management.transaction.application.usecase.common.handler.TransactionHandler
import br.com.vps.consulting.b2b.management.transaction.domain.Transaction
import br.com.vps.consulting.b2b.management.transaction.domain.TransactionRepository
import br.com.vps.consulting.b2b.management.transaction.domain.event.TransactionStatusChanged
import jakarta.inject.Named
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory

@Named
class DefaultReconcileTransactionsUseCase(
    private val transactionRepository: TransactionRepository,
    private val eventPublisher: EventPublisher,
    handlers: List<TransactionHandler>
) : ReconcileTransactionsUseCase {

    private val log = LoggerFactory.getLogger(javaClass)
    private val handlersByType = handlers.associateBy { it.type }

    @Transactional
    override fun execute() {

        val pendingTransactions = transactionRepository.findPending(limit = 100)
        log.info("Reconciliation: ${pendingTransactions.size} pending transactions found")

        val reconciledTransactions = pendingTransactions.mapNotNull { transaction ->
            handlersByType[transaction.type]?.let { strategy ->
                processTransaction(strategy, transaction)
                transaction.takeUnless(Transaction::isPending)
            }
        }

        transactionRepository.saveAll(reconciledTransactions)

        reconciledTransactions.forEach {
            eventPublisher.publish(
                TransactionStatusChanged(
                    aggregateId = it.id.value,
                    partnerId = it.partnerId,
                    type = it.type,
                    amount = it.amount,
                    status = it.status,
                )
            )
        }

        log.info(
            "Reconciliation: ${reconciledTransactions.size} transactions reconciled"
        )

    }

    private fun processTransaction(strategy: TransactionHandler, transaction: Transaction) {
        try {
            if (strategy.successfullyProcessed(transaction)) {
                transaction.complete()
            } else {
                transaction.fail("Operação de saldo não confirmada")
            }
            log.info("Transaction ${transaction.id.value} reconciled with status ${transaction.status}")
        } catch (e: BaseException) {
            log.warn("Business failure while reconciling transaction ${transaction.id.value}: ${e.message}")
            transaction.fail(e.message)
        } catch (e: Exception) {
            log.warn("Unexpected failure while reconciling transaction ${transaction.id.value}", e)
        }
    }

}

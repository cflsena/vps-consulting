package br.com.vps.consulting.b2b.management.transaction.application.usecase.create

import br.com.vps.consulting.b2b.management.shared.core.event.EventPublisher
import br.com.vps.consulting.b2b.management.shared.core.exception.BaseException
import br.com.vps.consulting.b2b.management.shared.core.vo.Money
import br.com.vps.consulting.b2b.management.transaction.application.usecase.common.handler.TransactionHandler
import br.com.vps.consulting.b2b.management.transaction.domain.Transaction
import br.com.vps.consulting.b2b.management.transaction.domain.TransactionRepository
import br.com.vps.consulting.b2b.management.transaction.domain.TransactionStatus
import br.com.vps.consulting.b2b.management.transaction.domain.event.TransactionStatusChanged
import jakarta.inject.Named
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory

@Named
class DefaultCreateTransactionUseCase(
    private val transactionRepository: TransactionRepository,
    private val eventPublisher: EventPublisher,
    private val validator: CreateTransactionValidator,
    handlers: List<TransactionHandler>
) : CreateTransactionUseCase {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val handlersByType = handlers.associateBy { it.type }

    @Transactional
    override fun execute(input: CreateTransactionInput): CreateTransactionOutput {

        logger.info("Iniciando criação da transação, detalhes: $input")

        validator.validate(input)

        val transaction = Transaction.createAsPending(
            partnerId = input.partnerId,
            type = input.type,
            amount = Money.of(input.amount),
            description = input.description,
            idempotencyKey = input.idempotencyKey,
        )

        processTransaction(transaction)

        val transactionSaved = transactionRepository.save(transaction)

        if (transactionSaved.status != TransactionStatus.PENDING) {
            eventPublisher.publish(
                TransactionStatusChanged(
                    aggregateId = transactionSaved.id.value,
                    partnerId = transactionSaved.partnerId,
                    type = transactionSaved.type,
                    amount = transactionSaved.amount,
                    status = transactionSaved.status,
                )
            )
        }

        return CreateTransactionOutput(
            transactionId = transactionSaved.id.value,
            status = transactionSaved.status,
            errorDescription = transactionSaved.errorDescription,
        ).also {
        logger.info("Transação ${transactionSaved.id.value} processada com status ${transactionSaved.status} para o parceiro ${input.partnerId}")
        }

    }

    private fun processTransaction(transaction: Transaction) {

        val handler = handlersByType[transaction.type]
            ?: throw IllegalArgumentException("Nenhum handler encontrado para o processamento da transação")

        try {
            if (handler.successfullyProcessed(transaction)) {
                transaction.complete()
            } else {
                transaction.fail("Operação de saldo não confirmada")
            }
        } catch (e: BaseException) {
            logger.warn("Falha de negócio ao processar a transação ${transaction.id.value}: ${e.message}")
            transaction.fail(e.message)
        } catch (e: Exception) {
            logger.warn("Falha inesperada ao processar a transação ${transaction.id.value}", e)
        }

    }

}

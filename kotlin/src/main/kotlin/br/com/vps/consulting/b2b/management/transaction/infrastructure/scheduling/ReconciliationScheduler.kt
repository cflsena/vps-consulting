package br.com.vps.consulting.b2b.management.transaction.infrastructure.scheduling

import br.com.vps.consulting.b2b.management.transaction.application.usecase.reconciliation.ReconcileTransactionsUseCase
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(name = ["scheduling.reconciliation.enabled"], havingValue = "true", matchIfMissing = true)
class ReconciliationScheduler(
    private val reconcileTransactionsUseCase: ReconcileTransactionsUseCase,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelayString = "\${scheduling.reconciliation.fixed-delay-ms:60000}")
    fun reconcile() {
        logger.info("Iniciando job agendado de reconciliação de transações pendentes")
        reconcileTransactionsUseCase.execute()
        logger.info("Job agendado de reconciliação de transações pendentes finalizado")
    }
}

package br.com.vps.consulting.b2b.management.transaction.domain.exception

import br.com.vps.consulting.b2b.management.shared.core.exception.NotFoundException
import br.com.vps.consulting.b2b.management.transaction.domain.TransactionId

class TransactionNotFoundException(transactionId: TransactionId) :
    NotFoundException("Transação não encontrada: ${transactionId.value}")

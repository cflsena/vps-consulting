package br.com.vps.consulting.b2b.management.transaction.domain.exception

import br.com.vps.consulting.b2b.management.shared.core.exception.ConflictException

class DuplicateTransactionException(idempotencyKey: String) :
    ConflictException("Transação já processada para a chave de idempotência: $idempotencyKey")

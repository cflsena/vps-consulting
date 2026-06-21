package br.com.vps.consulting.b2b.management.transaction.domain.exception

import br.com.vps.consulting.b2b.management.shared.core.exception.DomainException

class InvalidIdempotencyKeyException(idempotencyKey: String) :
    DomainException("Chave de idempotência $idempotencyKey inválida")
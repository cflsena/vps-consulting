package br.com.vps.consulting.b2b.management.transaction.domain.exception

import br.com.vps.consulting.b2b.management.shared.core.exception.DomainException

class InvalidDebitAmountException(message: String) : DomainException(message)
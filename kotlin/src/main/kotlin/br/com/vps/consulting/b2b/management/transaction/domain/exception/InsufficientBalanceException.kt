package br.com.vps.consulting.b2b.management.transaction.domain.exception

import br.com.vps.consulting.b2b.management.shared.core.exception.DomainException
import br.com.vps.consulting.b2b.management.shared.core.vo.Money
import java.util.UUID

class InsufficientBalanceException(partnerId: UUID, requested: Money, available: Money) :
    DomainException(
        "Saldo insuficiente para o parceiro $partnerId: solicitado ${requested.value}, disponível ${available.value}"
    )
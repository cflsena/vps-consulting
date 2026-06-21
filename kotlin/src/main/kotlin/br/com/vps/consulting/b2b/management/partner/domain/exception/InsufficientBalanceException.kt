package br.com.vps.consulting.b2b.management.partner.domain.exception

import br.com.vps.consulting.b2b.management.partner.domain.PartnerId
import br.com.vps.consulting.b2b.management.shared.core.exception.DomainException
import br.com.vps.consulting.b2b.management.shared.core.vo.Money

class InsufficientBalanceException(partnerId: PartnerId, requested: Money, available: Money) :
    DomainException(
        "Saldo insuficiente para o parceiro ${partnerId.value}: solicitado ${requested.amount}, disponível ${available.amount}"
    )

package br.com.vps.consulting.b2b.management.transaction.domain.exception

import br.com.vps.consulting.b2b.management.shared.core.exception.NotFoundException
import java.util.UUID

class TransactionPartnerNotFoundException(partnerId: UUID) :
    NotFoundException("Parceiro $partnerId não encontrado para criar transação")

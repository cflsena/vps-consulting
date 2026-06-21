package br.com.vps.consulting.b2b.management.transaction.domain.event

import br.com.vps.consulting.b2b.management.shared.core.event.DomainEvent
import br.com.vps.consulting.b2b.management.shared.core.vo.Money
import br.com.vps.consulting.b2b.management.transaction.domain.TransactionStatus
import br.com.vps.consulting.b2b.management.transaction.domain.TransactionType
import java.util.*

data class TransactionStatusChanged(
    override val aggregateId: UUID,
    val partnerId: UUID,
    val type: TransactionType,
    val amount: Money,
    val status: TransactionStatus
) : DomainEvent

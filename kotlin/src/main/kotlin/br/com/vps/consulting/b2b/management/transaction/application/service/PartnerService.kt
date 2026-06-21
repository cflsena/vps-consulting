package br.com.vps.consulting.b2b.management.transaction.application.service

import br.com.vps.consulting.b2b.management.shared.core.vo.Money
import java.util.*

interface PartnerService {
    fun existsById(partnerId: UUID) : Boolean
    fun debitBalance(partnerId: UUID, amount: Money) : Boolean
    fun creditBalance(partnerId: UUID, amount: Money): Boolean
    fun findBalanceById(partnerId: UUID) : Money
}
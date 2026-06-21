package br.com.vps.consulting.b2b.management.partner.domain

import br.com.vps.consulting.b2b.management.shared.core.vo.Money

interface PartnerBalanceRepository {
    fun save(partnerBalance: PartnerBalance)
    fun findBalanceById(id: PartnerId): PartnerBalance?
    fun creditBalance(partnerId: PartnerId, amount: Money): Boolean
    fun debitBalance(partnerId: PartnerId, amount: Money): Boolean
}

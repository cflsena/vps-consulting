package br.com.vps.consulting.b2b.management.partner.domain

import br.com.vps.consulting.b2b.management.shared.core.page.PageCustom
import br.com.vps.consulting.b2b.management.shared.core.vo.Money

interface PartnerRepository {
    fun save(partner: Partner): Partner
    fun findById(id: PartnerId): Partner?
    fun findBalanceById(id: PartnerId): PartnerBalance?
    fun findAll(pageSize: Int, pageNumber: Int): PageCustom<Partner>
    fun creditBalance(partnerId: PartnerId, amount: Money): Boolean
    fun debitBalance(partnerId: PartnerId, amount: Money): Boolean
}

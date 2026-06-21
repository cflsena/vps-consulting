package br.com.vps.consulting.b2b.management.partner.domain

import br.com.vps.consulting.b2b.management.shared.core.page.PageCustom

interface PartnerRepository {
    fun save(partner: Partner): Partner
    fun findById(id: PartnerId): Partner?
    fun findAll(document: String?, pageSize: Int, pageNumber: Int): PageCustom<Partner>
}

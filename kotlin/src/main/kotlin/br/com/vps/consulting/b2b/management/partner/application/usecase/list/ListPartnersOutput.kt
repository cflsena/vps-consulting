package br.com.vps.consulting.b2b.management.partner.application.usecase.list

import br.com.vps.consulting.b2b.management.partner.domain.Partner
import br.com.vps.consulting.b2b.management.shared.core.page.PageCustom
import java.time.Instant
import java.util.UUID

data class ListPartnersOutput(
    val id: UUID,
    val name: String,
    val document: String,
    val createdAt: Instant,
) {
    companion object {
        fun from(page: PageCustom<Partner>): PageCustom<ListPartnersOutput> = PageCustom(
            pageNumber = page.pageNumber,
            pageSize = page.pageSize,
            totalPages = page.totalPages,
            totalElements = page.totalElements,
            items = page.items.map { from(it) },
        )

        fun from(partner: Partner): ListPartnersOutput = ListPartnersOutput(
            id = partner.id.value,
            name = partner.name,
            document = partner.document,
            createdAt = partner.createdAt,
        )
    }
}

package br.com.vps.consulting.b2b.management.partner.infrastructure.api.response

import br.com.vps.consulting.b2b.management.partner.application.usecase.list.ListPartnersOutput
import br.com.vps.consulting.b2b.management.shared.core.extension.toBrazilianOffsetDateTime
import br.com.vps.consulting.b2b.management.shared.core.page.PageCustom
import br.com.vps.consulting.b2b.management.shared.infrastructure.api.pagination.PageResponseDTO
import io.swagger.v3.oas.annotations.media.Schema
import java.time.OffsetDateTime
import java.util.*

data class ListPartnersResponse(
    @field:Schema(description = "ID único do parceiro")
    val id: UUID,

    @field:Schema(description = "Nome completo do parceiro")
    val name: String,

    @field:Schema(description = "Documento do parceiro (CPF ou CNPJ)")
    val document: String,

    @field:Schema(description = "Data e hora de criação do parceiro")
    val createdAt: OffsetDateTime,
) {
    companion object {
        fun from(page: PageCustom<ListPartnersOutput>): PageResponseDTO<ListPartnersResponse> = PageResponseDTO(
            pageNumber = page.pageNumber,
            pageSize = page.pageSize,
            totalPages = page.totalPages,
            totalElements = page.totalElements,
            items = page.items.map { from(it) },
            numberOfElements =  page.items.size
        )

        fun from(partner: ListPartnersOutput): ListPartnersResponse = ListPartnersResponse(
            id = partner.id,
            name = partner.name,
            document = partner.document,
            createdAt = partner.createdAt.toBrazilianOffsetDateTime(),
        )
    }
}

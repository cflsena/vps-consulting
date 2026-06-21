package br.com.vps.consulting.b2b.management.shared.infrastructure.api.pagination

import br.com.vps.consulting.b2b.management.shared.core.page.PageCustom

data class PageResponseDTO<T>(
    val pageNumber: Int,
    val pageSize: Int,
    val numberOfElements: Int,
    val totalPages: Int,
    val totalElements: Long,
    val items: List<T>,
) {
    companion object {
        fun <T> from(page: PageCustom<T>): PageResponseDTO<T> = PageResponseDTO(
            pageNumber = page.pageNumber,
            pageSize = page.pageSize,
            numberOfElements = page.items.size,
            totalPages = page.totalPages,
            totalElements = page.totalElements,
            items = page.items,
        )
    }
}

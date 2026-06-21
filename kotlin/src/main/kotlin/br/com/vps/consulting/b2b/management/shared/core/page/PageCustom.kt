package br.com.vps.consulting.b2b.management.shared.core.page

data class PageCustom<T>(
    val pageNumber: Int,
    val pageSize: Int,
    val totalPages: Int,
    val totalElements: Long,
    val items: List<T>,
)

package br.com.vps.consulting.b2b.management.shared.infrastructure.api.pagination

import io.swagger.v3.oas.annotations.media.Schema

data class PageResponseDTO<T>(
    @field:Schema(description = "Número da página atual (base 0)", example = "0")
    val pageNumber: Int,

    @field:Schema(description = "Quantidade de registros por página", example = "20")
    val pageSize: Int,

    @field:Schema(description = "Quantidade de elementos nesta página", example = "15")
    val numberOfElements: Int,

    @field:Schema(description = "Número total de páginas", example = "5")
    val totalPages: Int,

    @field:Schema(description = "Número total de elementos", example = "100")
    val totalElements: Long,

    @field:Schema(description = "Lista de itens desta página")
    val items: List<T>,
) {
}

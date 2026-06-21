package br.com.vps.consulting.b2b.management.shared.infrastructure.api.pagination;

import br.com.vps.consulting.b2b.management.shared.core.page.PageCustom;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record PageResponseDTO<T>(
        @Schema(description = "Índice da página (páginas começam na posição 0)", example = "0")
        int pageNumber,
        @Schema(description = "Número de elementos esperados na página", example = "20")
        int pageSize,
        @Schema(description = "Número de elementos na página atual", example = "5")
        int numberOfElements,
        @Schema(description = "Total de páginas", example = "3")
        int totalPages,
        @Schema(description = "Total de elementos", example = "50")
        long totalElements,
        @Schema(description = "Lista de elementos")
        List<T> items
) {
    public static <T> PageResponseDTO<T> from(final PageCustom<T> page) {
        return new PageResponseDTO<>(
                page.pageNumber(),
                page.pageSize(),
                page.numberOfElements(),
                page.totalPages(),
                page.totalElements(),
                page.items()
        );
    }
}

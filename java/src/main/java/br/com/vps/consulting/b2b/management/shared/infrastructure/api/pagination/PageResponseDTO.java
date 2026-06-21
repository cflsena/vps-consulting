package br.com.vps.consulting.b2b.management.shared.infrastructure.api.pagination;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
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
}

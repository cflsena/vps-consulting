package br.com.vps.consulting.b2b.management.shared.infrastructure.api.pagination;

import br.com.vps.consulting.b2b.management.shared.core.page.PageCustom;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record PageResponseDTO<T>(
        @Schema(description = "Page index (pages start at position 0)", example = "0")
        int pageNumber,
        @Schema(description = "Number of elements expected on the page", example = "20")
        int pageSize,
        @Schema(description = "Number of elements on the current page", example = "5")
        int numberOfElements,
        @Schema(description = "Total number of pages", example = "3")
        int totalPages,
        @Schema(description = "Total number of elements", example = "50")
        long totalElements,
        @Schema(description = "List of elements")
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

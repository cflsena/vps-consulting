package br.com.vps.consulting.b2b.management.shared.core.page;

import lombok.Builder;

import java.util.List;

@Builder
public record PageCustom<T>(
        Integer pageNumber,
        Integer pageSize,
        Integer numberOfElements,
        Integer totalPages,
        Long totalElements,
        List<T> items
) {
}

package br.com.vps.consulting.b2b.management.partner.application.usecase.list;

import br.com.vps.consulting.b2b.management.partner.domain.Partner;
import br.com.vps.consulting.b2b.management.shared.core.page.PageCustom;
import lombok.AccessLevel;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder(access = AccessLevel.PRIVATE)
public record PartnerListOutput(
        UUID id,
        String name,
        String document,
        Instant createdAt
) {

    public static PageCustom<PartnerListOutput> from(final PageCustom<Partner> page) {
        return PageCustom.<PartnerListOutput>builder()
                .pageNumber(page.pageNumber())
                .pageSize(page.pageSize())
                .numberOfElements(page.numberOfElements())
                .totalPages(page.totalPages())
                .totalElements(page.totalElements())
                .items(page.items().stream().map(PartnerListOutput::from).toList())
                .build();
    }

    public static PartnerListOutput from(final Partner partner) {
        return PartnerListOutput.builder()
                .id(partner.getId().value())
                .name(partner.getName())
                .document(partner.getDocument())
                .createdAt(partner.getCreatedAt())
                .build();
    }

}

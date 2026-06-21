package br.com.vps.consulting.b2b.management.partner.application.usecase.list;

import br.com.vps.consulting.b2b.management.partner.domain.Partner;
import br.com.vps.consulting.b2b.management.shared.core.page.PageCustom;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.UUID;

import static br.com.vps.consulting.b2b.management.shared.core.utils.ManagementConstants.BRASILIA_TIME_ZONE;

@Builder(access = AccessLevel.PRIVATE)
public record PartnerListOutput(
        @Schema(description = "Identificador único do parceiro")
        UUID id,

        @Schema(description = "Nome do parceiro")
        String name,

        @Schema(description = "Documento do parceiro (CPF ou CNPJ)")
        String document,

        @Schema(description = "Data e hora de cadastro do parceiro (fuso horário Brasília, GMT-3)")
        OffsetDateTime createdAt
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
                .createdAt(partner.getCreatedAt().atOffset(BRASILIA_TIME_ZONE))
                .build();
    }

}

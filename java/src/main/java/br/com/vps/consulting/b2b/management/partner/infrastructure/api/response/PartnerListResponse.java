package br.com.vps.consulting.b2b.management.partner.infrastructure.api.response;

import br.com.vps.consulting.b2b.management.partner.application.usecase.list.PartnerListOutput;
import br.com.vps.consulting.b2b.management.shared.core.page.PageCustom;
import br.com.vps.consulting.b2b.management.shared.infrastructure.api.pagination.PageResponseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.UUID;

import static br.com.vps.consulting.b2b.management.shared.core.utils.ManagementConstants.BRASILIA_TIME_ZONE;

@Builder(access = AccessLevel.PRIVATE)
public record PartnerListResponse(
        @Schema(description = "Identificador único do parceiro")
        UUID id,

        @Schema(description = "Nome do parceiro")
        String name,

        @Schema(description = "Documento do parceiro (CPF ou CNPJ)")
        String document,

        @Schema(description = "Data e hora de cadastro do parceiro (fuso horário Brasília, GMT-3)")
        OffsetDateTime createdAt
) {

    public static PageResponseDTO<PartnerListResponse> from(final PageCustom<PartnerListOutput> page) {
        return PageResponseDTO.<PartnerListResponse>builder()
                .pageNumber(page.pageNumber())
                .pageSize(page.pageSize())
                .numberOfElements(page.numberOfElements())
                .totalPages(page.totalPages())
                .totalElements(page.totalElements())
                .items(page.items().stream().map(PartnerListResponse::from).toList())
                .build();
    }

    public static PartnerListResponse from(final PartnerListOutput partner) {
        return PartnerListResponse.builder()
                .id(partner.id())
                .name(partner.name())
                .document(partner.document())
                .createdAt(partner.createdAt().atOffset(BRASILIA_TIME_ZONE))
                .build();
    }

}

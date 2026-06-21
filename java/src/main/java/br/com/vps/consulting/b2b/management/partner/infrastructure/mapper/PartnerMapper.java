package br.com.vps.consulting.b2b.management.partner.infrastructure.mapper;

import br.com.vps.consulting.b2b.management.partner.domain.Partner;
import br.com.vps.consulting.b2b.management.partner.domain.PartnerId;
import br.com.vps.consulting.b2b.management.partner.infrastructure.persistence.PartnerEntity;

public final class PartnerMapper {

    private PartnerMapper(){}

    public static Partner toDomain(final PartnerEntity partner) {
        return Partner.builder()
                .id(PartnerId.from(partner.getId()))
                .name(partner.getName())
                .document(partner.getDocument())
                .createdAt(partner.getCreatedAt())
                .build();
    }

    public static PartnerEntity toEntity(final Partner partner) {
        return PartnerEntity.builder()
                .id(partner.getId().value())
                .name(partner.getName())
                .document(partner.getDocument())
                .createdAt(partner.getCreatedAt())
                .build();
    }

}

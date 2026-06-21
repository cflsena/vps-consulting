package br.com.vps.consulting.b2b.management.partner.infrastructure.mapper;

import br.com.vps.consulting.b2b.management.partner.domain.PartnerCredit;
import br.com.vps.consulting.b2b.management.partner.infrastructure.persistence.PartnerCreditEntity;

public final class PartnerCreditMapper {

    private PartnerCreditMapper() {
    }

    public static PartnerCreditEntity toEntity(final PartnerCredit partner) {
        return PartnerCreditEntity.builder()
                .id(partner.getId().value())
                .creditLimit(partner.getCreditLimit().value())
                .availableBalance(partner.getAvailableBalance().value())
                .reservedBalance(partner.getReservedBalance().value())
                .updatedAt(partner.getUpdatedAt())
                .build();
    }

    public static PartnerCredit toDomain(final PartnerCreditEntity partner) {
        return PartnerCredit.builder()
                .id(partner.getId())
                .creditLimit(partner.getCreditLimit())
                .availableBalance(partner.getAvailableBalance())
                .reservedBalance(partner.getReservedBalance())
                .updatedAt(partner.getUpdatedAt())
                .build();
    }

}

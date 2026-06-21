package br.com.vps.consulting.b2b.management.partner.infrastructure.mapper;

import br.com.vps.consulting.b2b.management.partner.domain.Partner;
import br.com.vps.consulting.b2b.management.partner.infrastructure.persistence.PartnerCreditEntity;

import java.math.BigDecimal;
import java.time.Instant;

public final class PartnerCreditMapper {

    private PartnerCreditMapper(){}

    public static PartnerCreditEntity toEntity(final Partner partner) {
        return PartnerCreditEntity.builder()
                .partnerId(partner.getId().value())
                .creditLimit(partner.getCreditLimit().value())
                .availableBalance(partner.getCreditLimit().value())
                .reservedBalance(BigDecimal.ZERO)
                .updatedAt(Instant.now())
                .build();
    }

}

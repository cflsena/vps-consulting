package br.com.vps.consulting.b2b.management.partner.application.usecase.find;

import br.com.vps.consulting.b2b.management.partner.domain.PartnerCredit;

import java.util.UUID;

public interface FindPartnerCreditByIdUseCase {
    PartnerCredit execute(UUID id);
}

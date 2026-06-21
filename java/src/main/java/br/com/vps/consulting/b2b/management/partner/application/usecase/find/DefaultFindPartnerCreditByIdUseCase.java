package br.com.vps.consulting.b2b.management.partner.application.usecase.find;

import br.com.vps.consulting.b2b.management.partner.domain.PartnerCredit;
import br.com.vps.consulting.b2b.management.partner.domain.PartnerCreditRepository;
import br.com.vps.consulting.b2b.management.partner.domain.PartnerId;
import br.com.vps.consulting.b2b.management.partner.domain.exception.PartnerNotFoundException;
import jakarta.inject.Named;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
@Named
@RequiredArgsConstructor
public class DefaultFindPartnerCreditByIdUseCase implements FindPartnerCreditByIdUseCase {

    private final PartnerCreditRepository partnerCreditRepository;

    @Override
    public PartnerCredit execute(final UUID id) {
        log.info("Finding credit for partner [partnerId={}]", id);
        final var credit = partnerCreditRepository.findById(PartnerId.from(id))
                .orElseThrow(() -> new PartnerNotFoundException(id));
        log.info("Credit found for partner [partnerId={}, creditLimit={}, availableBalance={}, reservedBalance={}]",
                id, credit.getCreditLimit(), credit.getAvailableBalance(), credit.getReservedBalance());
        return credit;
    }

}

package br.com.vps.consulting.b2b.management.partner.application.usecase.create;

import br.com.vps.consulting.b2b.management.partner.domain.*;
import jakarta.inject.Named;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
@Named
@RequiredArgsConstructor
public class DefaultCreatePartnerUseCase implements CreatePartnerUseCase {

    private final PartnerRepository partnerRepository;
    private final PartnerCreditRepository partnerCreditRepository;

    @Override
    @Transactional
    public UUID execute(final CreatePartnerInput input) {
        log.info("Creating partner [name={}, document={}]", input.name(), input.document());

        final var partnerCreated = createPartner(input);
        saveCredit(partnerCreated.getId(), input);

        log.info("Partner created successfully [partnerId={}, name={}, document={}]",
                partnerCreated.getId().value(), input.name(), input.document());

        return partnerCreated.getId().value();
    }

    private Partner createPartner(CreatePartnerInput input) {
        final var partner = Partner.builder()
                .name(input.name())
                .document(input.document())
                .build();
        return partnerRepository.save(partner);
    }

    private void saveCredit(final PartnerId partnerId, final CreatePartnerInput input) {
        partnerCreditRepository.save(
                PartnerCredit.builder()
                        .id(partnerId.value())
                        .creditLimit(input.creditLimit())
                        .build()
        );
    }

}

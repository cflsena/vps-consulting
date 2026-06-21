package br.com.vps.consulting.b2b.management.partner.application.usecase.create;

import br.com.vps.consulting.b2b.management.partner.domain.Partner;
import br.com.vps.consulting.b2b.management.partner.domain.PartnerRepository;
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

    @Override
    @Transactional
    public UUID execute(final CreatePartnerInput input) {
        log.info("Creating partner [name={}, document={}]", input.name(), input.document());

        final var partner = Partner.builder()
                .name(input.name())
                .document(input.document())
                .creditLimit(input.creditLimit())
                .build();
        final var saved = partnerRepository.save(partner);

        log.info("Partner created successfully [partnerId={}, name={}, document={}]",
                saved.getId().value(), input.name(), input.document());

        return saved.getId().value();
    }

}

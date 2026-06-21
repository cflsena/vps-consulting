package br.com.vps.consulting.b2b.management.partner.application.usecase.adjust;

import br.com.vps.consulting.b2b.management.partner.domain.PartnerCredit;
import br.com.vps.consulting.b2b.management.partner.domain.PartnerId;
import br.com.vps.consulting.b2b.management.partner.domain.PartnerRepository;
import br.com.vps.consulting.b2b.management.partner.domain.exception.CreditLimitBelowReservationException;
import br.com.vps.consulting.b2b.management.partner.domain.exception.PartnerNotFoundException;
import jakarta.inject.Named;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

@Slf4j
@Named
@RequiredArgsConstructor
public class DefaultAdjustCreditLimitUseCase implements AdjustCreditLimitUseCase {

    private final PartnerRepository partnerRepository;

    @Override
    @Transactional
    public void execute(final AdjustCreditLimitInput input) {

        log.info("Adjusting credit limit [partnerId={}, newCreditLimit={}]", input.partnerId(), input.newCreditLimit());

        final var partnerId = PartnerId.from(input.partnerId());
        final var current = partnerRepository.findCreditById(partnerId)
                .orElseThrow(() -> new PartnerNotFoundException(input.partnerId()));

        final var debited = current.creditLimit().subtract(current.availableBalance());
        final var minimumLimit = debited.add(current.reservedBalance());
        if (input.newCreditLimit().compareTo(minimumLimit) < 0) {
            log.warn("Cannot reduce credit limit below committed amount [partnerId={}, newLimit={}, minimumAllowed={}]",
                    input.partnerId(), input.newCreditLimit(), minimumLimit);
            throw new CreditLimitBelowReservationException(input.partnerId(), input.newCreditLimit(), minimumLimit);
        }

        partnerRepository.adjustCreditLimit(partnerId, input.newCreditLimit());

        log.info("Credit limit adjusted successfully [partnerId={}, newCreditLimit={}]",
                input.partnerId(), input.newCreditLimit());

    }

}

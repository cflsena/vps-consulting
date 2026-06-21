package br.com.vps.consulting.b2b.management.partner.application.usecase.adjust;

import br.com.vps.consulting.b2b.management.partner.domain.PartnerCreditRepository;
import br.com.vps.consulting.b2b.management.partner.domain.PartnerId;
import br.com.vps.consulting.b2b.management.partner.domain.exception.CreditLimitBelowReservationException;
import br.com.vps.consulting.b2b.management.partner.domain.exception.PartnerNotFoundException;
import br.com.vps.consulting.b2b.management.shared.core.vo.Money;
import jakarta.inject.Named;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Named
@RequiredArgsConstructor
public class DefaultAdjustCreditLimitUseCase implements AdjustCreditLimitUseCase {

    private final PartnerCreditRepository partnerCreditRepository;

    @Override
    @Transactional
    public void execute(final AdjustCreditLimitInput input) {

        log.info("Adjusting credit limit [partnerId={}, newCreditLimit={}]", input.partnerId(), input.newCreditLimit());

        final var partnerId = PartnerId.from(input.partnerId());
        final var current = partnerCreditRepository.findById(partnerId)
                .orElseThrow(() -> new PartnerNotFoundException(input.partnerId()));

        final var debited = current.getCreditLimit().subtract(current.getAvailableBalance());
        final var minimumLimit = debited.add(current.getReservedBalance());
        if (minimumLimit.isGreaterThan(Money.of(input.newCreditLimit()))) {
            log.error("Cannot reduce credit limit below committed amount [partnerId={}, newLimit={}, minimumAllowed={}]",
                    input.partnerId(), input.newCreditLimit(), minimumLimit);
            throw new CreditLimitBelowReservationException(input.partnerId(), input.newCreditLimit(), minimumLimit.value());
        }

        partnerCreditRepository.adjustCreditLimit(partnerId, input.newCreditLimit());

        log.info("Credit limit adjusted successfully [partnerId={}, newCreditLimit={}]",
                input.partnerId(), input.newCreditLimit());

    }

}

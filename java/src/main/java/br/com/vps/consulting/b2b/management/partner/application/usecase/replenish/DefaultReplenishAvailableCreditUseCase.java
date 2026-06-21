package br.com.vps.consulting.b2b.management.partner.application.usecase.replenish;

import br.com.vps.consulting.b2b.management.partner.domain.PartnerCreditRepository;
import br.com.vps.consulting.b2b.management.partner.domain.PartnerId;
import br.com.vps.consulting.b2b.management.partner.domain.exception.InvalidCreditReplenishmentException;
import br.com.vps.consulting.b2b.management.partner.domain.exception.PartnerNotFoundException;
import br.com.vps.consulting.b2b.management.shared.core.vo.Money;
import jakarta.inject.Named;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Named
@RequiredArgsConstructor
public class DefaultReplenishAvailableCreditUseCase implements ReplenishAvailableCreditUseCase {

    private final PartnerCreditRepository partnerCreditRepository;

    @Override
    @Transactional
    public void execute(final ReplenishAvailableCreditInput input) {

        log.info("Replenishing available credit [partnerId={}, amount={}]", input.partnerId(), input.amount());

        final var partnerId = PartnerId.from(input.partnerId());
        final var current = partnerCreditRepository.findById(partnerId)
                .orElseThrow(() -> new PartnerNotFoundException(input.partnerId()));

        final var maxReplenishment = current.getCreditLimit().subtract(current.getAvailableBalance());
        if (maxReplenishment.isLessThan(Money.of(input.amount()))) {
            log.warn("Replenishment exceeds maximum allowed [partnerId={}, amount={}, maxAllowed={}]",
                    input.partnerId(), input.amount(), maxReplenishment);
            throw new InvalidCreditReplenishmentException(input.partnerId(), input.amount(), maxReplenishment.value());
        }

        partnerCreditRepository.refundCredit(partnerId, input.amount());

        log.info("Credit replenished successfully [partnerId={}, amount={}]", input.partnerId(), input.amount());

    }

}

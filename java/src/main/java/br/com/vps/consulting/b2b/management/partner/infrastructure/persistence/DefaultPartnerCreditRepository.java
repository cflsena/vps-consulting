package br.com.vps.consulting.b2b.management.partner.infrastructure.persistence;

import br.com.vps.consulting.b2b.management.partner.domain.PartnerCredit;
import br.com.vps.consulting.b2b.management.partner.domain.PartnerCreditRepository;
import br.com.vps.consulting.b2b.management.partner.domain.PartnerId;
import br.com.vps.consulting.b2b.management.partner.infrastructure.mapper.PartnerCreditMapper;
import br.com.vps.consulting.b2b.management.partner.infrastructure.persistence.jpa.PartnerCreditJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DefaultPartnerCreditRepository implements PartnerCreditRepository {

    private final PartnerCreditJpaRepository partnerCreditJpaRepository;

    @Override
    public void save(final PartnerCredit partnerCredit) {
        partnerCreditJpaRepository.save(PartnerCreditMapper.toEntity(partnerCredit));
    }

    @Override
    public Optional<PartnerCredit> findById(final PartnerId id) {
        return partnerCreditJpaRepository.findById(id.value()).map(PartnerCreditMapper::toDomain);
    }

    @Override
    public boolean reserveCredit(final PartnerId partnerId, final BigDecimal amount) {
        return partnerCreditJpaRepository.reserveCredit(partnerId.value(), amount) > 0;
    }

    @Override
    public void debitReservation(final PartnerId partnerId, final BigDecimal amount) {
        partnerCreditJpaRepository.debitReservation(partnerId.value(), amount);
    }

    @Override
    public void releaseReservation(final PartnerId partnerId, final BigDecimal amount) {
        partnerCreditJpaRepository.releaseReservation(partnerId.value(), amount);
    }

    @Override
    public void refundCredit(final PartnerId partnerId, final BigDecimal amount) {
        partnerCreditJpaRepository.refundCredit(partnerId.value(), amount);
    }

    @Override
    public void adjustCreditLimit(final PartnerId partnerId, final BigDecimal newLimit) {
        partnerCreditJpaRepository.adjustCreditLimit(partnerId.value(), newLimit);
    }

}

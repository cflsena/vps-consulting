package br.com.vps.consulting.b2b.management.partner.infrastructure.service;

import br.com.vps.consulting.b2b.management.order.application.service.PartnerCreditService;
import br.com.vps.consulting.b2b.management.partner.domain.PartnerId;
import br.com.vps.consulting.b2b.management.partner.domain.PartnerRepository;
import br.com.vps.consulting.b2b.management.partner.domain.exception.InsufficientCreditException;
import br.com.vps.consulting.b2b.management.partner.domain.exception.PartnerNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PartnerCreditServiceImpl implements PartnerCreditService {

    private final PartnerRepository partnerRepository;

    @Override
    @Transactional
    public void reserveCredit(final UUID partnerId, final BigDecimal amount) {
        final var id = PartnerId.from(partnerId);
        partnerRepository.findById(id).orElseThrow(() -> new PartnerNotFoundException(partnerId));
        final boolean reserved = partnerRepository.reserveCredit(id, amount);
        if (!reserved) {
            throw new InsufficientCreditException(partnerId, amount, BigDecimal.ZERO);
        }
    }

    @Override
    @Transactional
    public void debitReservation(final UUID partnerId, final BigDecimal amount) {
        final var id = PartnerId.from(partnerId);
        partnerRepository.findById(id).orElseThrow(() -> new PartnerNotFoundException(partnerId));
        partnerRepository.debitReservation(id, amount);
    }

    @Override
    @Transactional
    public void releaseReservation(final UUID partnerId, final BigDecimal amount) {
        final var id = PartnerId.from(partnerId);
        partnerRepository.findById(id).orElseThrow(() -> new PartnerNotFoundException(partnerId));
        partnerRepository.releaseReservation(id, amount);
    }

    @Override
    @Transactional
    public void refundDebit(final UUID partnerId, final BigDecimal amount) {
        final var id = PartnerId.from(partnerId);
        partnerRepository.findById(id).orElseThrow(() -> new PartnerNotFoundException(partnerId));
        partnerRepository.refundCredit(id, amount);
    }

}

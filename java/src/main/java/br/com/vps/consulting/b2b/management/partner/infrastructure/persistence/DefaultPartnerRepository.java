package br.com.vps.consulting.b2b.management.partner.infrastructure.persistence;

import br.com.vps.consulting.b2b.management.partner.domain.Partner;
import br.com.vps.consulting.b2b.management.partner.domain.PartnerCredit;
import br.com.vps.consulting.b2b.management.partner.domain.PartnerId;
import br.com.vps.consulting.b2b.management.partner.domain.PartnerRepository;
import br.com.vps.consulting.b2b.management.partner.infrastructure.mapper.PartnerCreditMapper;
import br.com.vps.consulting.b2b.management.partner.infrastructure.mapper.PartnerMapper;
import br.com.vps.consulting.b2b.management.partner.infrastructure.persistence.jpa.PartnerCreditJpaRepository;
import br.com.vps.consulting.b2b.management.partner.infrastructure.persistence.jpa.PartnerJpaRepository;
import br.com.vps.consulting.b2b.management.shared.core.page.PageCustom;
import jakarta.inject.Named;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DefaultPartnerRepository implements PartnerRepository {

    private final PartnerJpaRepository partnerJpaRepository;
    private final PartnerCreditJpaRepository partnerCreditJpaRepository;

    @Override
    public Partner save(final Partner partner) {
        final var partnerEntity = PartnerMapper.toEntity(partner);
        partnerJpaRepository.save(partnerEntity);
        partnerCreditJpaRepository.save(PartnerCreditMapper.toEntity(partner));
        return partner;
    }

    @Override
    public Optional<Partner> findById(final PartnerId id) {
        return partnerJpaRepository.findById(id.value())
                .map(partnerEntity -> {
                    final var credit = partnerCreditJpaRepository.findById(id.value()).orElse(null);
                    return PartnerMapper.toDomain(partnerEntity, credit);
                });
    }

    @Override
    public Optional<PartnerCredit> findCreditById(final PartnerId id) {
        return partnerCreditJpaRepository.findById(id.value())
                .map(e -> new PartnerCredit(e.getCreditLimit(), e.getAvailableBalance(), e.getReservedBalance(), e.getUpdatedAt()));
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

    @Override
    public PageCustom<Partner> findAll(final long pageSize, final long pageNumber) {
        final Pageable pageable = PageRequest.of((int) pageNumber, (int) pageSize,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        final var result = partnerJpaRepository.findAll(pageable);
        return PageCustom.<Partner>builder()
                .pageNumber(result.getNumber())
                .pageSize(result.getSize())
                .numberOfElements(result.getNumberOfElements())
                .totalPages(result.getTotalPages())
                .totalElements(result.getTotalElements())
                .items(result.getContent().stream().map(e -> PartnerMapper.toDomain(e, null)).toList())
                .build();
    }

}

package br.com.vps.consulting.b2b.management.partner.infrastructure.persistence;

import br.com.vps.consulting.b2b.management.partner.domain.Partner;
import br.com.vps.consulting.b2b.management.partner.domain.PartnerRepository;
import br.com.vps.consulting.b2b.management.partner.infrastructure.mapper.PartnerMapper;
import br.com.vps.consulting.b2b.management.partner.infrastructure.persistence.jpa.PartnerJpaRepository;
import br.com.vps.consulting.b2b.management.shared.core.page.PageCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DefaultPartnerRepository implements PartnerRepository {

    private final PartnerJpaRepository partnerJpaRepository;

    @Override
    public Partner save(final Partner partner) {
        final var partnerEntity = PartnerMapper.toEntity(partner);
        partnerJpaRepository.save(partnerEntity);
        return partner;
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
                .items(result.getContent().stream().map(PartnerMapper::toDomain).toList())
                .build();
    }

}

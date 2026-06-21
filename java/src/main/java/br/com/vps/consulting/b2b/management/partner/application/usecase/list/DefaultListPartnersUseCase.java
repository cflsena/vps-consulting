package br.com.vps.consulting.b2b.management.partner.application.usecase.list;

import br.com.vps.consulting.b2b.management.partner.domain.PartnerRepository;
import br.com.vps.consulting.b2b.management.shared.core.page.PageCustom;
import jakarta.inject.Named;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Named
@RequiredArgsConstructor
public class DefaultListPartnersUseCase implements ListPartnersUseCase {

    private final PartnerRepository partnerRepository;

    @Override
    public PageCustom<PartnerListOutput> execute(final ListPartnersInput input) {
        log.debug("Listing partners [page={}, size={}]", input.pageNumber(), input.pageSize());
        final var page = partnerRepository.findAll(input.pageSize(), input.pageNumber());
        return PartnerListOutput.from(page);
    }

}

package br.com.vps.consulting.b2b.management.partner.domain;

import br.com.vps.consulting.b2b.management.shared.core.page.PageCustom;

public interface PartnerRepository {
    Partner save(Partner partner);
    PageCustom<Partner> findAll(long pageSize, long pageNumber);
}

package br.com.vps.consulting.b2b.management.partner.domain;

import br.com.vps.consulting.b2b.management.shared.core.page.PageCustom;

import java.math.BigDecimal;
import java.util.Optional;

public interface PartnerRepository {
    Partner save(Partner partner);
    Optional<Partner> findById(PartnerId id);
    Optional<PartnerCredit> findCreditById(PartnerId id);
    PageCustom<Partner> findAll(long pageSize, long pageNumber);
    boolean reserveCredit(PartnerId partnerId, BigDecimal amount);
    void debitReservation(PartnerId partnerId, BigDecimal amount);
    void releaseReservation(PartnerId partnerId, BigDecimal amount);
    void refundCredit(PartnerId partnerId, BigDecimal amount);
    void adjustCreditLimit(PartnerId partnerId, BigDecimal newLimit);
}

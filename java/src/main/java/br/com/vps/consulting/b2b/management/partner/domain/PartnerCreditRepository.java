package br.com.vps.consulting.b2b.management.partner.domain;

import java.math.BigDecimal;
import java.util.Optional;

public interface PartnerCreditRepository {
    void save(PartnerCredit partnerCredit);
    Optional<PartnerCredit> findById(PartnerId id);
    boolean reserveCredit(PartnerId partnerId, BigDecimal amount);
    void debitReservation(PartnerId partnerId, BigDecimal amount);
    void releaseReservation(PartnerId partnerId, BigDecimal amount);
    void refundCredit(PartnerId partnerId, BigDecimal amount);
    void adjustCreditLimit(PartnerId partnerId, BigDecimal newLimit);
}

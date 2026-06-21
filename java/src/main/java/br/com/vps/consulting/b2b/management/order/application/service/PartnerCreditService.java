package br.com.vps.consulting.b2b.management.order.application.service;

import java.math.BigDecimal;
import java.util.UUID;

public interface PartnerCreditService {
    void reserveCredit(UUID partnerId, BigDecimal amount);
    void debitReservation(UUID partnerId, BigDecimal amount);
    void releaseReservation(UUID partnerId, BigDecimal amount);
    void refundDebit(UUID partnerId, BigDecimal amount);
}

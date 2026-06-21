package br.com.vps.consulting.b2b.management.partner.domain.exception;

import br.com.vps.consulting.b2b.management.shared.core.exception.DomainException;

import java.math.BigDecimal;
import java.util.UUID;

public class InsufficientCreditException extends DomainException {
    public InsufficientCreditException(UUID partnerId, BigDecimal requested, BigDecimal available) {
        super("Crédito insuficiente para o parceiro %s: solicitado %s, disponível %s".formatted(partnerId, requested, available));
    }
}

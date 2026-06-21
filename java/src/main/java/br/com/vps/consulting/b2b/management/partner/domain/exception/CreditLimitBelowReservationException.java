package br.com.vps.consulting.b2b.management.partner.domain.exception;

import br.com.vps.consulting.b2b.management.shared.core.exception.DomainException;

import java.math.BigDecimal;
import java.util.UUID;

public class CreditLimitBelowReservationException extends DomainException {
    public CreditLimitBelowReservationException(UUID partnerId, BigDecimal newLimit, BigDecimal minimum) {
        super("Não é possível reduzir o limite de crédito para %s do parceiro %s: o mínimo permitido é %s"
                .formatted(newLimit, partnerId, minimum));
    }
}

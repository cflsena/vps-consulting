package br.com.vps.consulting.b2b.management.partner.domain.exception;

import br.com.vps.consulting.b2b.management.shared.core.exception.DomainException;

import java.math.BigDecimal;
import java.util.UUID;

public class InvalidCreditReplenishmentException extends DomainException {
    public InvalidCreditReplenishmentException(UUID partnerId, BigDecimal amount, BigDecimal max) {
        super("Reposição de crédito inválida de %s para o parceiro %s: o máximo permitido é %s"
                .formatted(amount, partnerId, max));
    }
}

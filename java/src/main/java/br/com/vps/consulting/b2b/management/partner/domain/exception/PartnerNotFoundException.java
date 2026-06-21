package br.com.vps.consulting.b2b.management.partner.domain.exception;

import br.com.vps.consulting.b2b.management.shared.core.exception.NotFoundException;

import java.util.UUID;

public class PartnerNotFoundException extends NotFoundException {
    public PartnerNotFoundException(UUID partnerId) {
        super("Parceiro não encontrado: " + partnerId);
    }
}

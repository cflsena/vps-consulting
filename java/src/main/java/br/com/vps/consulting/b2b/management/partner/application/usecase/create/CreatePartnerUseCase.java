package br.com.vps.consulting.b2b.management.partner.application.usecase.create;

import java.util.UUID;

public interface CreatePartnerUseCase {
    UUID execute(CreatePartnerInput input);
}

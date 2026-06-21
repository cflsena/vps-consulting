package br.com.vps.consulting.b2b.management.partner.application.usecase.create

import java.util.UUID

interface CreatePartnerUseCase {
    fun execute(input: CreatePartnerInput): UUID
}

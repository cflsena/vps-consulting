package br.com.vps.consulting.b2b.management.partner.application.usecase.create

import java.util.*

interface CreatePartnerUseCase {
    fun execute(input: CreatePartnerInput): UUID
}

package br.com.vps.consulting.b2b.management.partner.application.usecase.find

interface FindPartnerBalanceUseCase {
    fun execute(input: FindPartnerBalanceInput): FindPartnerBalanceOutput
}

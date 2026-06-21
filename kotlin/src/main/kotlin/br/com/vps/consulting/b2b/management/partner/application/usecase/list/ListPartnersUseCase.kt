package br.com.vps.consulting.b2b.management.partner.application.usecase.list

import br.com.vps.consulting.b2b.management.shared.core.page.PageCustom

interface ListPartnersUseCase {
    fun execute(input: ListPartnersInput): PageCustom<ListPartnersOutput>
}

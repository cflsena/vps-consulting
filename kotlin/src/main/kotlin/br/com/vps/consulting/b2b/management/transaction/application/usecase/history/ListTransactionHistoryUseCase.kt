package br.com.vps.consulting.b2b.management.transaction.application.usecase.history

import br.com.vps.consulting.b2b.management.shared.core.page.PageCustom

interface ListTransactionHistoryUseCase {
    fun execute(input: ListTransactionHistoryInput): PageCustom<ListTransactionHistoryOutput>
}

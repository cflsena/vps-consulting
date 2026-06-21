package br.com.vps.consulting.b2b.management.transaction.application.usecase.common.handler

import br.com.vps.consulting.b2b.management.transaction.domain.Transaction
import br.com.vps.consulting.b2b.management.transaction.domain.TransactionType

interface TransactionHandler {
    val type: TransactionType
    fun successfullyProcessed(transaction: Transaction) : Boolean
}
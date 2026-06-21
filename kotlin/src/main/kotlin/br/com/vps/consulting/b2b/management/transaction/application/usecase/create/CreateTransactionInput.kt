package br.com.vps.consulting.b2b.management.transaction.application.usecase.create

import br.com.vps.consulting.b2b.management.transaction.domain.TransactionType
import java.math.BigDecimal
import java.util.*

data class CreateTransactionInput(
    val partnerId: UUID,
    val amount: BigDecimal,
    val description: String,
    val idempotencyKey: String,
    val type: TransactionType,
)

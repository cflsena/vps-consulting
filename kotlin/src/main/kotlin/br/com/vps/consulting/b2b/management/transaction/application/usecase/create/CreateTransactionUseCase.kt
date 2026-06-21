package br.com.vps.consulting.b2b.management.transaction.application.usecase.create

interface CreateTransactionUseCase {
    fun execute(input: CreateTransactionInput): CreateTransactionOutput
}

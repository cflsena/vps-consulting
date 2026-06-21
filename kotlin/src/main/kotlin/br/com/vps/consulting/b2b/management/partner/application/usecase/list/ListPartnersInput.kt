package br.com.vps.consulting.b2b.management.partner.application.usecase.list

data class ListPartnersInput(
    val document: String?,
    val pageSize: Int,
    val pageNumber: Int,
)

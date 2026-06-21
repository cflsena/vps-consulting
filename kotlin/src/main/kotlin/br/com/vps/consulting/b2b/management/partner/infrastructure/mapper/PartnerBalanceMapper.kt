package br.com.vps.consulting.b2b.management.partner.infrastructure.mapper

import br.com.vps.consulting.b2b.management.partner.application.usecase.find.FindPartnerBalanceOutput
import br.com.vps.consulting.b2b.management.partner.domain.PartnerBalance
import br.com.vps.consulting.b2b.management.partner.domain.PartnerId
import br.com.vps.consulting.b2b.management.partner.infrastructure.persistence.PartnerBalanceEntity

fun PartnerBalance.toEntity() = PartnerBalanceEntity(
    partnerId = this.id.value,
    totalCredited = this.totalCredited.value,
    totalDebited = this.totalDebited.value,
    availableBalance = this.availableBalance.value,
    updatedAt = this.updatedAt
)

fun PartnerBalance.toOutput() = FindPartnerBalanceOutput(
    partnerId = this.id.value,
    totalCredited = this.totalCredited.value,
    totalDebited = this.totalDebited.value,
    availableBalance = this.availableBalance.value,
    updatedAt = this.updatedAt
)

fun PartnerBalanceEntity.toDomain() = PartnerBalance.with(
    id = PartnerId.from(this.partnerId),
    totalCredited = this.totalCredited,
    totalDebited = this.totalDebited,
    availableBalance = this.availableBalance,
    updatedAt = this.updatedAt
)
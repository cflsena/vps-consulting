package br.com.vps.consulting.b2b.management.partner.infrastructure.mapper

import br.com.vps.consulting.b2b.management.partner.domain.Partner
import br.com.vps.consulting.b2b.management.partner.domain.PartnerBalance
import br.com.vps.consulting.b2b.management.partner.domain.PartnerId
import br.com.vps.consulting.b2b.management.partner.infrastructure.persistence.PartnerBalanceEntity
import br.com.vps.consulting.b2b.management.partner.infrastructure.persistence.PartnerEntity
import br.com.vps.consulting.b2b.management.shared.core.vo.Money
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

fun PartnerEntity.toDomain(): Partner = Partner.with(
    id = PartnerId.from(id),
    name = name,
    document = document,
    createdAt = createdAt,
)

fun Partner.toEntity(): PartnerEntity = PartnerEntity(
    id = id.value,
    name = name,
    document = document,
    createdAt = createdAt,
)

fun PartnerBalanceEntity.toDomain(): PartnerBalance = PartnerBalance(
    partnerId = PartnerId.from(partnerId),
    totalBalance = Money.of(totalBalance),
    availableBalance = Money.of(availableBalance),
    updatedAt = updatedAt,
)

fun zeroBalanceEntityFor(partnerId: UUID): PartnerBalanceEntity = PartnerBalanceEntity(
    partnerId = partnerId,
    totalBalance = BigDecimal.ZERO,
    availableBalance = BigDecimal.ZERO,
    updatedAt = Instant.now(),
)

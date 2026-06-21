package br.com.vps.consulting.b2b.management.partner.infrastructure.mapper

import br.com.vps.consulting.b2b.management.partner.domain.Partner
import br.com.vps.consulting.b2b.management.partner.domain.PartnerId
import br.com.vps.consulting.b2b.management.partner.infrastructure.persistence.PartnerEntity

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

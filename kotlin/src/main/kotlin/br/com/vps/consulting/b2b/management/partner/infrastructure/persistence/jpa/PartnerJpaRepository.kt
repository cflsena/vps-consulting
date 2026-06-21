package br.com.vps.consulting.b2b.management.partner.infrastructure.persistence.jpa

import br.com.vps.consulting.b2b.management.partner.infrastructure.persistence.PartnerEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import java.util.*

interface PartnerJpaRepository : JpaRepository<PartnerEntity, UUID>, JpaSpecificationExecutor<PartnerEntity>

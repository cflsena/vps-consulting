package br.com.vps.consulting.b2b.management.partner.infrastructure.persistence.jpa;

import br.com.vps.consulting.b2b.management.partner.infrastructure.persistence.PartnerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PartnerJpaRepository extends JpaRepository<PartnerEntity, UUID> {
}

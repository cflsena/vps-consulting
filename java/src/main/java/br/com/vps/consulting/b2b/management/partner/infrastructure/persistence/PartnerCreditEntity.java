package br.com.vps.consulting.b2b.management.partner.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "partner_credit")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerCreditEntity {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "credit_limit", nullable = false)
    private BigDecimal creditLimit;

    @Column(name = "available_balance", nullable = false)
    private BigDecimal availableBalance;

    @Column(name = "reserved_balance", nullable = false)
    private BigDecimal reservedBalance;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

}

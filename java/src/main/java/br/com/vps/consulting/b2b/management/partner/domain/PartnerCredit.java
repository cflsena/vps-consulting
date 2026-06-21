package br.com.vps.consulting.b2b.management.partner.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record PartnerCredit(
        BigDecimal creditLimit,
        BigDecimal availableBalance,
        BigDecimal reservedBalance,
        Instant updatedAt
) {}

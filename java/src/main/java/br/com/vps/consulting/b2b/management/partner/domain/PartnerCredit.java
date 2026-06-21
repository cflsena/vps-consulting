package br.com.vps.consulting.b2b.management.partner.domain;

import br.com.vps.consulting.b2b.management.shared.core.entity.Entity;
import br.com.vps.consulting.b2b.management.shared.core.vo.Money;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
public class PartnerCredit extends Entity<PartnerId> {

    private final PartnerId id;
    private Money creditLimit;
    private final Money availableBalance;
    private final Money reservedBalance;
    private final Instant updatedAt;

    @Builder
    public PartnerCredit(final UUID id,
                         final BigDecimal creditLimit,
                         final BigDecimal availableBalance,
                         final BigDecimal reservedBalance,
                         final Instant updatedAt) {
        this.id = PartnerId.from(id);
        this.creditLimit = Money.of(creditLimit);
        this.availableBalance = availableBalance == null ? Money.ZERO : Money.of(availableBalance);
        this.reservedBalance = reservedBalance == null ? Money.ZERO : Money.of(reservedBalance);
        this.updatedAt = updatedAt == null ? Instant.now() : updatedAt;
        this.validate();
    }

    public void adjustCreditLimit(final BigDecimal newLimit) {
        Objects.requireNonNull(newLimit, "newLimit é obrigatório");
        this.creditLimit = Money.of(newLimit);
    }

    @Override
    protected void validate() {
        Objects.requireNonNull(this.id, "PartnerId é obrigatório");
        Objects.requireNonNull(this.creditLimit, "CreditLimit é obrigatório");
    }

}

package br.com.vps.consulting.b2b.management.partner.domain;

import br.com.vps.consulting.b2b.management.shared.core.entity.Entity;
import br.com.vps.consulting.b2b.management.shared.core.vo.Money;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

@Getter
public class Partner extends Entity<PartnerId> {

    private final PartnerId id;
    private final String name;
    private final String document;
    private final Instant createdAt;
    private Money creditLimit;

    @Builder
    public Partner(final PartnerId id,
                   final String name,
                   final String document,
                   final BigDecimal creditLimit,
                   final Instant createdAt) {
        this.id = id == null ? PartnerId.generate() : id;
        this.name = name;
        this.document = document;
        this.creditLimit = Money.of(creditLimit);
        this.createdAt = createdAt == null ? Instant.now() : createdAt;
        this.validate();
    }

    public void adjustCreditLimit(final BigDecimal newLimit) {
        Objects.requireNonNull(newLimit, "newLimit is required");
        this.creditLimit = Money.of(newLimit);
    }

    @Override
    protected void validate() {
        Objects.requireNonNull(this.id, "PartnerId is required");
        Objects.requireNonNull(this.name, "name is required");
        Objects.requireNonNull(this.document, "document is required");
        Objects.requireNonNull(this.creditLimit, "creditLimit is required");
        if (this.name.isBlank()) throw new IllegalArgumentException("name cannot be blank");
        if (this.document.isBlank()) throw new IllegalArgumentException("document cannot be blank");
    }

}

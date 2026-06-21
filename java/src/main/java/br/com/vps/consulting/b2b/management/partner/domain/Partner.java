package br.com.vps.consulting.b2b.management.partner.domain;

import br.com.vps.consulting.b2b.management.shared.core.entity.Entity;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

@Getter
public class Partner extends Entity<PartnerId> {

    private final PartnerId id;
    private final String name;
    private final String document;
    private final Instant createdAt;

    @Builder
    public Partner(final PartnerId id,
                   final String name,
                   final String document,
                   final Instant createdAt) {
        this.id = id == null ? PartnerId.generate() : id;
        this.name = name;
        this.document = document;
        this.createdAt = createdAt == null ? Instant.now() : createdAt;
        this.validate();
    }

    @Override
    protected void validate() {
        Objects.requireNonNull(this.id, "PartnerId é obrigatório");
        Objects.requireNonNull(this.name, "name é obrigatório");
        Objects.requireNonNull(this.document, "document é obrigatório");
        if (this.name.isBlank()) throw new IllegalArgumentException("name cannot be blank");
        if (this.document.isBlank()) throw new IllegalArgumentException("document cannot be blank");
    }

}

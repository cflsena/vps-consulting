package br.com.vps.consulting.b2b.management.partner.infrastructure.persistence;

import br.com.vps.consulting.b2b.management.order.infrastructure.persistence.OrderEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "partner")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerEntity {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "document", nullable = false, unique = true)
    private String document;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "partner", fetch = FetchType.LAZY)
    private List<OrderEntity> orders;

}

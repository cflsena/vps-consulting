package br.com.vps.consulting.b2b.management.order.infrastructure.persistence;

import br.com.vps.consulting.b2b.management.order.domain.OrderStatus;
import br.com.vps.consulting.b2b.management.partner.infrastructure.persistence.PartnerEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "\"order\"")
public class OrderEntity {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "partner_id", nullable = false)
    private UUID partnerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id", insertable = false, updatable = false)
    private PartnerEntity partner;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    private List<OrderItemEntity> items;

}

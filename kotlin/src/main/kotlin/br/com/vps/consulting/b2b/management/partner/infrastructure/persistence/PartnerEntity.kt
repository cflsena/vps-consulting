package br.com.vps.consulting.b2b.management.partner.infrastructure.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "partner")
class PartnerEntity(
    @Id
    val id: UUID,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false, unique = true)
    val document: String,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant,
)

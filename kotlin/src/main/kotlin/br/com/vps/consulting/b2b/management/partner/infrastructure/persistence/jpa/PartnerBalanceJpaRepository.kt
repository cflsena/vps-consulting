package br.com.vps.consulting.b2b.management.partner.infrastructure.persistence.jpa

import br.com.vps.consulting.b2b.management.partner.infrastructure.persistence.PartnerBalanceEntity
import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.math.BigDecimal
import java.util.*

interface PartnerBalanceJpaRepository : JpaRepository<PartnerBalanceEntity, UUID> {

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(
        """
        UPDATE partner_balance
        SET total_credited = total_credited + :amount,
            available_balance = available_balance + :amount,
            updated_at = now()
        WHERE partner_id = :partnerId
        """,
        nativeQuery = true,
    )
    fun creditBalance(@Param("partnerId") partnerId: UUID, @Param("amount") amount: BigDecimal): Int

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(
        """
        UPDATE partner_balance
        SET available_balance = available_balance - :amount,
            total_debited = total_debited + :amount,
            updated_at = now()
        WHERE partner_id = :partnerId AND available_balance >= :amount
        """,
        nativeQuery = true,
    )
    fun debitBalance(@Param("partnerId") partnerId: UUID, @Param("amount") amount: BigDecimal): Int
}

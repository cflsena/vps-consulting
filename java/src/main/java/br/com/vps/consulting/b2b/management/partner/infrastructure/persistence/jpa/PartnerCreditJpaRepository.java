package br.com.vps.consulting.b2b.management.partner.infrastructure.persistence.jpa;

import br.com.vps.consulting.b2b.management.partner.infrastructure.persistence.PartnerCreditEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

public interface PartnerCreditJpaRepository extends JpaRepository<PartnerCreditEntity, UUID> {

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
            UPDATE partner_credit
            SET reserved_balance = reserved_balance + :amount, updated_at = now()
            WHERE partner_id = :partnerId AND available_balance - reserved_balance >= :amount
            """, nativeQuery = true)
    int reserveCredit(@Param("partnerId") UUID partnerId, @Param("amount") BigDecimal amount);

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
            UPDATE partner_credit
            SET available_balance = available_balance - :amount,
                reserved_balance  = reserved_balance  - :amount,
                updated_at = now()
            WHERE partner_id = :partnerId
            """, nativeQuery = true)
    void debitReservation(@Param("partnerId") UUID partnerId, @Param("amount") BigDecimal amount);

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
            UPDATE partner_credit
            SET reserved_balance = reserved_balance - :amount, updated_at = now()
            WHERE partner_id = :partnerId
            """, nativeQuery = true)
    void releaseReservation(@Param("partnerId") UUID partnerId, @Param("amount") BigDecimal amount);

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
            UPDATE partner_credit
            SET available_balance = available_balance + :amount, updated_at = now()
            WHERE partner_id = :partnerId
            """, nativeQuery = true)
    void refundCredit(@Param("partnerId") UUID partnerId, @Param("amount") BigDecimal amount);

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
            UPDATE partner_credit
            SET credit_limit      = :newLimit,
                available_balance = available_balance + (:newLimit - credit_limit),
                updated_at        = now()
            WHERE partner_id = :partnerId
            """, nativeQuery = true)
    int adjustCreditLimit(@Param("partnerId") UUID partnerId, @Param("newLimit") BigDecimal newLimit);

}

package br.com.vps.consulting.b2b.management.partner.application.usecase.adjust;

import br.com.vps.consulting.b2b.management.partner.domain.PartnerCredit;
import br.com.vps.consulting.b2b.management.partner.domain.PartnerCreditRepository;
import br.com.vps.consulting.b2b.management.partner.domain.PartnerId;
import br.com.vps.consulting.b2b.management.partner.domain.exception.CreditLimitBelowReservationException;
import br.com.vps.consulting.b2b.management.partner.domain.exception.PartnerNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultAdjustCreditLimitUseCaseTest {

    @Mock private PartnerCreditRepository partnerCreditRepository;
    @InjectMocks private DefaultAdjustCreditLimitUseCase useCase;

    @Test
    @DisplayName("Given a valid new limit, when execute is called, should adjust the credit limit")
    void shouldAdjustCreditLimit() {
        final var partnerId = UUID.randomUUID();
        when(partnerCreditRepository.findById(PartnerId.from(partnerId))).thenReturn(Optional.of(creditWith(partnerId, "10000", "10000", "0")));

        useCase.execute(new AdjustCreditLimitInput(partnerId, new BigDecimal("25000.00")));

        verify(partnerCreditRepository).adjustCreditLimit(PartnerId.from(partnerId), new BigDecimal("25000.00"));
    }

    // creditLimit=1000, availableBalance=600, reservedBalance=200 → debited=400 → minimumLimit=600
    @Test
    @DisplayName("Given existing debits and reservations, when execute is called, should allow adjustment above the recalculated minimum limit")
    void shouldAllowAdjustmentAboveMinimumLimit() {
        final var partnerId = UUID.randomUUID();

        when(partnerCreditRepository.findById(PartnerId.from(partnerId))).thenReturn(Optional.of(creditWith(partnerId, "1000", "600", "200")));

        useCase.execute(new AdjustCreditLimitInput(partnerId, new BigDecimal("600.00")));

        verify(partnerCreditRepository).adjustCreditLimit(PartnerId.from(partnerId), new BigDecimal("600.00"));
    }

    // creditLimit=1000, availableBalance=600, reservedBalance=200 → debited=400 → minimumLimit=600
    @Test
    @DisplayName("Given a new limit below the minimum, when execute is called, should throw CreditLimitBelowReservationException")
    void shouldThrowWhenNewLimitBelowMinimum() {
        final var partnerId = UUID.randomUUID();

        when(partnerCreditRepository.findById(PartnerId.from(partnerId))).thenReturn(Optional.of(creditWith(partnerId, "1000", "600", "200")));

        assertThatThrownBy(() -> useCase.execute(new AdjustCreditLimitInput(partnerId, new BigDecimal("599.99"))))
                .isInstanceOf(CreditLimitBelowReservationException.class);

        verify(partnerCreditRepository, never()).adjustCreditLimit(any(), any());
    }

    @Test
    @DisplayName("Given a non-existing partner, when execute is called, should throw PartnerNotFoundException")
    void shouldThrowWhenPartnerNotFound() {
        final var partnerId = UUID.randomUUID();
        when(partnerCreditRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new AdjustCreditLimitInput(partnerId, BigDecimal.TEN)))
                .isInstanceOf(PartnerNotFoundException.class)
                .hasMessageContaining("Parceiro não encontrado");
    }

    @Test
    @DisplayName("Given a non-existing partner, when execute is called, should not adjust the credit limit")
    void shouldNotAdjustWhenPartnerNotFound() {
        when(partnerCreditRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new AdjustCreditLimitInput(UUID.randomUUID(), BigDecimal.TEN)))
                .isInstanceOf(PartnerNotFoundException.class);

        verify(partnerCreditRepository, never()).adjustCreditLimit(any(), any());
    }

    private static PartnerCredit creditWith(UUID id, String limit, String available, String reserved) {
        return new PartnerCredit(id, new BigDecimal(limit), new BigDecimal(available), new BigDecimal(reserved), Instant.now());
    }

}

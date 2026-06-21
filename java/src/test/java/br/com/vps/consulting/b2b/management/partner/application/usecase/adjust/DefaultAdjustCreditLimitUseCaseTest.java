package br.com.vps.consulting.b2b.management.partner.application.usecase.adjust;

import br.com.vps.consulting.b2b.management.partner.domain.PartnerCredit;
import br.com.vps.consulting.b2b.management.partner.domain.PartnerId;
import br.com.vps.consulting.b2b.management.partner.domain.PartnerRepository;
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

    @Mock private PartnerRepository partnerRepository;
    @InjectMocks private DefaultAdjustCreditLimitUseCase useCase;

    @Test
    @DisplayName("Should adjust credit limit")
    void shouldAdjustCreditLimit() {
        final var partnerId = UUID.randomUUID();
        when(partnerRepository.findCreditById(PartnerId.from(partnerId))).thenReturn(Optional.of(creditWith("10000", "10000", "0")));

        useCase.execute(new AdjustCreditLimitInput(partnerId, new BigDecimal("25000.00")));

        verify(partnerRepository).adjustCreditLimit(PartnerId.from(partnerId), new BigDecimal("25000.00"));
    }

    // creditLimit=1000, availableBalance=600, reservedBalance=200 → debited=400 → minimumLimit=600
    @Test
    @DisplayName("Should recalculate correctly when there are existing debits and reservations")
    void shouldAllowAdjustmentAboveMinimumLimit() {
        final var partnerId = UUID.randomUUID();

        when(partnerRepository.findCreditById(PartnerId.from(partnerId))).thenReturn(Optional.of(creditWith("1000", "600", "200")));

        useCase.execute(new AdjustCreditLimitInput(partnerId, new BigDecimal("600.00")));

        verify(partnerRepository).adjustCreditLimit(PartnerId.from(partnerId), new BigDecimal("600.00"));
    }

    // creditLimit=1000, availableBalance=600, reservedBalance=200 → debited=400 → minimumLimit=600
    @Test
    @DisplayName("Should throw CreditLimitBelowReservationException when new limit is below minimum")
    void shouldThrowWhenNewLimitBelowMinimum() {
        final var partnerId = UUID.randomUUID();

        when(partnerRepository.findCreditById(PartnerId.from(partnerId))).thenReturn(Optional.of(creditWith("1000", "600", "200")));

        assertThatThrownBy(() -> useCase.execute(new AdjustCreditLimitInput(partnerId, new BigDecimal("599.99"))))
                .isInstanceOf(CreditLimitBelowReservationException.class);

        verify(partnerRepository, never()).adjustCreditLimit(any(), any());
    }

    @Test
    @DisplayName("Should throw PartnerNotFoundException when partner does not exist")
    void shouldThrowWhenPartnerNotFound() {
        final var partnerId = UUID.randomUUID();
        when(partnerRepository.findCreditById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new AdjustCreditLimitInput(partnerId, BigDecimal.TEN)))
                .isInstanceOf(PartnerNotFoundException.class)
                .hasMessageContaining("Parceiro não encontrado");
    }

    @Test
    @DisplayName("Should not adjust credit when partner is not found")
    void shouldNotAdjustWhenPartnerNotFound() {
        when(partnerRepository.findCreditById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new AdjustCreditLimitInput(UUID.randomUUID(), BigDecimal.TEN)))
                .isInstanceOf(PartnerNotFoundException.class);

        verify(partnerRepository, never()).adjustCreditLimit(any(), any());
    }

    private static PartnerCredit creditWith(String limit, String available, String reserved) {
        return new PartnerCredit(new BigDecimal(limit), new BigDecimal(available), new BigDecimal(reserved), Instant.now());
    }

}

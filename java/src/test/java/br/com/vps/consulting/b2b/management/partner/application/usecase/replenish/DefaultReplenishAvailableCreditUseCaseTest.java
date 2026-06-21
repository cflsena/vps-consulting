package br.com.vps.consulting.b2b.management.partner.application.usecase.replenish;

import br.com.vps.consulting.b2b.management.partner.domain.PartnerCredit;
import br.com.vps.consulting.b2b.management.partner.domain.PartnerId;
import br.com.vps.consulting.b2b.management.partner.domain.PartnerRepository;
import br.com.vps.consulting.b2b.management.partner.domain.exception.InvalidCreditReplenishmentException;
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
class DefaultReplenishAvailableCreditUseCaseTest {

    @Mock private PartnerRepository partnerRepository;
    @InjectMocks private DefaultReplenishAvailableCreditUseCase useCase;

    // creditLimit=15000, availableBalance=7500 → maxReplenishment=7500
    @Test
    @DisplayName("Given a valid amount, when execute is called, should replenish the available credit")
    void shouldReplenishAvailableCredit() {
        final var partnerId = UUID.randomUUID();

        when(partnerRepository.findCreditById(PartnerId.from(partnerId)))
                .thenReturn(Optional.of(creditWith("15000", "7500", "0")));

        useCase.execute(new ReplenishAvailableCreditInput(partnerId, new BigDecimal("5000.00")));

        verify(partnerRepository).refundCredit(PartnerId.from(partnerId), new BigDecimal("5000.00"));
    }

    // creditLimit=15000, availableBalance=0 → maxReplenishment=15000
    @Test
    @DisplayName("Given an amount exactly at the maximum limit, when execute is called, should allow the replenishment")
    void shouldAllowReplenishmentAtExactMaximum() {
        final var partnerId = UUID.randomUUID();
        when(partnerRepository.findCreditById(PartnerId.from(partnerId)))
                .thenReturn(Optional.of(creditWith("15000", "0", "0")));

        useCase.execute(new ReplenishAvailableCreditInput(partnerId, new BigDecimal("15000.00")));

        verify(partnerRepository).refundCredit(PartnerId.from(partnerId), new BigDecimal("15000.00"));
    }

    // creditLimit=15000, availableBalance=7500 → maxReplenishment=7500
    @Test
    @DisplayName("Given an amount that exceeds the maximum, when execute is called, should throw InvalidCreditReplenishmentException")
    void shouldThrowWhenAmountExceedsMaximum() {
        final var partnerId = UUID.randomUUID();

        when(partnerRepository.findCreditById(PartnerId.from(partnerId)))
                .thenReturn(Optional.of(creditWith("15000", "7500", "0")));

        assertThatThrownBy(() -> useCase.execute(new ReplenishAvailableCreditInput(partnerId, new BigDecimal("7500.01"))))
                .isInstanceOf(InvalidCreditReplenishmentException.class);

        verify(partnerRepository, never()).refundCredit(any(), any());
    }

    @Test
    @DisplayName("Given a non-existing partner, when execute is called, should throw PartnerNotFoundException")
    void shouldThrowWhenPartnerNotFound() {
        final var partnerId = UUID.randomUUID();
        when(partnerRepository.findCreditById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new ReplenishAvailableCreditInput(partnerId, BigDecimal.TEN)))
                .isInstanceOf(PartnerNotFoundException.class)
                .hasMessageContaining("Parceiro não encontrado");

        verify(partnerRepository, never()).refundCredit(any(), any());
    }

    private static PartnerCredit creditWith(String limit, String available, String reserved) {
        return new PartnerCredit(new BigDecimal(limit), new BigDecimal(available), new BigDecimal(reserved), Instant.now());
    }

}

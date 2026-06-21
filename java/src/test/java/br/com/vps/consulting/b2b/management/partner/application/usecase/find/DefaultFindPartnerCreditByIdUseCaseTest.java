package br.com.vps.consulting.b2b.management.partner.application.usecase.find;

import br.com.vps.consulting.b2b.management.partner.domain.PartnerCredit;
import br.com.vps.consulting.b2b.management.partner.domain.PartnerId;
import br.com.vps.consulting.b2b.management.partner.domain.PartnerRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultFindPartnerCreditByIdUseCaseTest {

    @Mock private PartnerRepository partnerRepository;
    @InjectMocks private DefaultFindPartnerCreditByIdUseCase useCase;

    @Test
    @DisplayName("Given an existing partner, when execute is called, should return the credit from the repository")
    void shouldReturnCreditFromRepository() {
        final var partnerId = UUID.randomUUID();
        final var credit = aPartnerCredit();
        when(partnerRepository.findCreditById(PartnerId.from(partnerId))).thenReturn(Optional.of(credit));

        final var result = useCase.execute(partnerId);

        assertThat(result).isEqualTo(credit);
    }

    @Test
    @DisplayName("Given a non-existing credit record, when execute is called, should throw PartnerNotFoundException")
    void shouldThrowWhenCreditNotFound() {
        final var partnerId = UUID.randomUUID();
        when(partnerRepository.findCreditById(PartnerId.from(partnerId))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(partnerId))
                .isInstanceOf(PartnerNotFoundException.class)
                .hasMessageContaining("Parceiro não encontrado");
    }

    private static PartnerCredit aPartnerCredit() {
        return new PartnerCredit(
                new BigDecimal("10000.00"),
                new BigDecimal("7500.00"),
                new BigDecimal("2500.00"),
                Instant.now()
        );
    }
}

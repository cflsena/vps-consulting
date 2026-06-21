package br.com.vps.consulting.b2b.management.partner.application.usecase.create;

import br.com.vps.consulting.b2b.management.partner.domain.Partner;
import br.com.vps.consulting.b2b.management.partner.domain.PartnerId;
import br.com.vps.consulting.b2b.management.partner.domain.PartnerCreditRepository;
import br.com.vps.consulting.b2b.management.partner.domain.PartnerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultCreatePartnerUseCaseTest {

    @Mock private PartnerRepository partnerRepository;
    @Mock private PartnerCreditRepository partnerCreditRepository;
    @InjectMocks private DefaultCreatePartnerUseCase useCase;

    @Test
    @DisplayName("Given a valid input, when execute is called, should create the partner and return its UUID")
    void shouldCreatePartnerAndReturnUUID() {
        final var partner = newPartner();
        when(partnerRepository.save(any())).thenReturn(partner);

        final var result = useCase.execute(new CreatePartnerInput("Acme Corp", "12345678000100", new BigDecimal("10000.00")));

        assertThat(result).isEqualTo(partner.getId().value());
        verify(partnerRepository).save(any(Partner.class));
        verify(partnerCreditRepository).save(any());
    }

    private static Partner newPartner() {
        return Partner.builder()
                .id(PartnerId.generate())
                .name("Acme Corp")
                .document("12345678000100")
                .build();
    }
}

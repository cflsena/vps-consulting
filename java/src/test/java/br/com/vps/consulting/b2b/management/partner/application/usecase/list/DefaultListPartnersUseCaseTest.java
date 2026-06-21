package br.com.vps.consulting.b2b.management.partner.application.usecase.list;

import br.com.vps.consulting.b2b.management.partner.domain.Partner;
import br.com.vps.consulting.b2b.management.partner.domain.PartnerId;
import br.com.vps.consulting.b2b.management.partner.domain.PartnerRepository;
import br.com.vps.consulting.b2b.management.shared.core.page.PageCustom;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultListPartnersUseCaseTest {

    @Mock private PartnerRepository partnerRepository;
    @InjectMocks private DefaultListPartnersUseCase useCase;

    @Test
    @DisplayName("Should return mapped PageCustom<PartnerListOutput> from repository page")
    void shouldReturnMappedPage() {
        final var partner = aPartner();
        final var page = PageCustom.<Partner>builder()
                .pageNumber(0).pageSize(10).numberOfElements(1)
                .totalPages(1).totalElements(1L).items(List.of(partner))
                .build();
        when(partnerRepository.findAll(10, 0)).thenReturn(page);

        final var result = useCase.execute(new ListPartnersInput(10, 0));

        assertThat(result.items()).hasSize(1);
        assertThat(result.totalElements()).isEqualTo(1L);
        assertThat(result.pageNumber()).isEqualTo(0);
        assertThat(result.items().get(0).id()).isEqualTo(partner.getId().value());
        assertThat(result.items().get(0).name()).isEqualTo(partner.getName());
        assertThat(result.items().get(0).document()).isEqualTo(partner.getDocument());
    }

    @Test
    @DisplayName("Should forward all input parameters to repository")
    void shouldPassInputParamsToRepository() {
        when(partnerRepository.findAll(5, 2)).thenReturn(emptyPage());

        useCase.execute(new ListPartnersInput(5, 2));

        verify(partnerRepository).findAll(5, 2);
    }

    @Test
    @DisplayName("Should return empty page when no partners exist")
    void shouldReturnEmptyPageWhenNoPartners() {
        when(partnerRepository.findAll(20, 0)).thenReturn(emptyPage());

        final var result = useCase.execute(new ListPartnersInput(20, 0));

        assertThat(result.items()).isEmpty();
        assertThat(result.totalElements()).isZero();
    }

    private static Partner aPartner() {
        return Partner.builder()
                .id(PartnerId.generate())
                .name("Acme Corp")
                .document("12345678000100")
                .creditLimit(new BigDecimal("5000.00"))
                .createdAt(Instant.now())
                .build();
    }

    private static PageCustom<Partner> emptyPage() {
        return PageCustom.<Partner>builder()
                .pageNumber(0).pageSize(20).numberOfElements(0)
                .totalPages(0).totalElements(0L).items(List.of())
                .build();
    }
}

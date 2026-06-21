package br.com.vps.consulting.b2b.management.partner.infrastructure.api;

import br.com.vps.consulting.b2b.management.partner.application.usecase.adjust.AdjustCreditLimitUseCase;
import br.com.vps.consulting.b2b.management.partner.application.usecase.create.CreatePartnerUseCase;
import br.com.vps.consulting.b2b.management.partner.application.usecase.find.FindPartnerCreditByIdUseCase;
import br.com.vps.consulting.b2b.management.partner.application.usecase.list.ListPartnersUseCase;
import br.com.vps.consulting.b2b.management.partner.application.usecase.list.PartnerListOutput;
import br.com.vps.consulting.b2b.management.partner.application.usecase.replenish.ReplenishAvailableCreditUseCase;
import br.com.vps.consulting.b2b.management.partner.domain.Partner;
import br.com.vps.consulting.b2b.management.partner.domain.PartnerCredit;
import br.com.vps.consulting.b2b.management.partner.domain.PartnerId;
import br.com.vps.consulting.b2b.management.partner.domain.exception.PartnerNotFoundException;
import br.com.vps.consulting.b2b.management.shared.core.page.PageCustom;
import br.com.vps.consulting.b2b.management.shared.infrastructure.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PartnerController.class)
@Import(GlobalExceptionHandler.class)
class PartnerControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    CreatePartnerUseCase createPartnerUseCase;
    @MockitoBean
    AdjustCreditLimitUseCase adjustCreditLimitUseCase;
    @MockitoBean
    ReplenishAvailableCreditUseCase replenishAvailableCreditUseCase;
    @MockitoBean
    FindPartnerCreditByIdUseCase findPartnerCreditByIdUseCase;
    @MockitoBean
    ListPartnersUseCase listPartnersUseCase;

    @Test
    @DisplayName("Given a request, when GET /api/v1/b2b/partners is called, should return 200 with a paginated list")
    void shouldListPartnersAndReturn200() throws Exception {
        given(listPartnersUseCase.execute(any())).willReturn(emptyPage());

        mockMvc.perform(get("/api/v1/b2b/partners"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    @DisplayName("Given an existing partner, when GET /api/v1/b2b/partners is called, should return 200 with the partner fields in the items")
    void shouldReturnPartnerFieldsInListItems() throws Exception {
        final var id = UUID.randomUUID();
        final var partner = Partner.builder()
                .id(PartnerId.from(id))
                .name("Acme Corp")
                .document("12345678000100")
                .createdAt(Instant.now())
                .build();
        final var page = PartnerListOutput.from(PageCustom.<Partner>builder()
                .pageNumber(0).pageSize(20).numberOfElements(1)
                .totalPages(1).totalElements(1L).items(List.of(partner))
                .build());
        given(listPartnersUseCase.execute(any())).willReturn(page);

        mockMvc.perform(get("/api/v1/b2b/partners"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.items[0].id").value(id.toString()))
                .andExpect(jsonPath("$.items[0].name").value("Acme Corp"))
                .andExpect(jsonPath("$.items[0].document").value("12345678000100"))
                .andExpect(jsonPath("$.items[0].createdAt").exists());
    }

    @Test
    @DisplayName("Given a valid partner request, when POST /api/v1/b2b/partners is called, should return 201 with the partner id")
    void shouldCreatePartnerAndReturn201() throws Exception {
        final var id = UUID.randomUUID();
        given(createPartnerUseCase.execute(any())).willReturn(id);

        mockMvc.perform(post("/api/v1/b2b/partners")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Acme Corp","document":"12345678901234","creditLimit":5000.00}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @ParameterizedTest
    @DisplayName("Given an invalid name, document or creditLimit, when POST /api/v1/b2b/partners is called, should return 400")
    @CsvSource({
            "'', 12345678901234, 5000.00",
            "Acme, '', 5000.00",
            "Acme, 12345678901234, -1.00",
    })
    void shouldReturn400WhenRequestIsInvalid(final String name, final String document, final String creditLimit)
            throws Exception {
        mockMvc.perform(post("/api/v1/b2b/partners")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"%s","document":"%s","creditLimit":%s}
                                """.formatted(name, document, creditLimit)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Given a valid request, when PATCH /api/v1/b2b/partners/{id}/credit-limit is called, should return 204")
    void shouldAdjustCreditLimitAndReturn204() throws Exception {
        final var id = UUID.randomUUID();
        doNothing().when(adjustCreditLimitUseCase).execute(any());

        mockMvc.perform(patch("/api/v1/b2b/partners/{id}/credit-limit", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"newCreditLimit":10000.00}
                                """))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Given an invalid body, when PATCH /api/v1/b2b/partners/{id}/credit-limit is called, should return 400")
    void shouldReturn400WhenCreditLimitBodyIsInvalid() throws Exception {
        final var id = UUID.randomUUID();

        mockMvc.perform(patch("/api/v1/b2b/partners/{id}/credit-limit", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"newCreditLimit":null}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Given a valid request, when PATCH /api/v1/b2b/partners/{id}/available-credit is called, should return 204")
    void shouldReplenishAvailableCreditAndReturn204() throws Exception {
        final var id = UUID.randomUUID();
        doNothing().when(replenishAvailableCreditUseCase).execute(any());

        mockMvc.perform(patch("/api/v1/b2b/partners/{id}/available-credit", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount":5000.00}
                                """))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Given a null amount, when PATCH /api/v1/b2b/partners/{id}/available-credit is called, should return 400")
    void shouldReturn400WhenReplenishAmountIsNull() throws Exception {
        final var id = UUID.randomUUID();

        mockMvc.perform(patch("/api/v1/b2b/partners/{id}/available-credit", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount":null}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Given an existing partner, when GET /api/v1/b2b/partners/{id}/credit is called, should return 200 with the full credit data")
    void shouldReturnPartnerCreditData() throws Exception {
        final var id = UUID.randomUUID();
        final var credit = new PartnerCredit(
                id,
                new BigDecimal("10000.00"),
                new BigDecimal("7500.00"),
                new BigDecimal("2500.00"),
                Instant.now()
        );
        given(findPartnerCreditByIdUseCase.execute(id)).willReturn(credit);

        mockMvc.perform(get("/api/v1/b2b/partners/{id}/credit", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.partnerId").value(id.toString()))
                .andExpect(jsonPath("$.creditLimit").value(10000.00))
                .andExpect(jsonPath("$.availableBalance").value(7500.00))
                .andExpect(jsonPath("$.reservedBalance").value(2500.00))
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    @DisplayName("Given a non-existing partner, when GET /api/v1/b2b/partners/{id}/credit is called, should return 404")
    void shouldReturn404WhenPartnerNotFound() throws Exception {
        final var id = UUID.randomUUID();
        given(findPartnerCreditByIdUseCase.execute(id))
                .willThrow(new PartnerNotFoundException(id));

        mockMvc.perform(get("/api/v1/b2b/partners/{id}/credit", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    private static PageCustom<PartnerListOutput> emptyPage() {
        return PageCustom.<PartnerListOutput>builder()
                .pageNumber(0).pageSize(20).numberOfElements(0)
                .totalPages(0).totalElements(0L).items(List.of())
                .build();
    }
}

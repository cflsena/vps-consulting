package br.com.vps.consulting.b2b.management.partner.infrastructure.api;

import br.com.vps.consulting.b2b.management.partner.application.usecase.adjust.AdjustCreditLimitUseCase;
import br.com.vps.consulting.b2b.management.partner.application.usecase.create.CreatePartnerUseCase;
import br.com.vps.consulting.b2b.management.partner.application.usecase.find.FindPartnerCreditByIdUseCase;
import br.com.vps.consulting.b2b.management.partner.application.usecase.list.ListPartnersUseCase;
import br.com.vps.consulting.b2b.management.partner.application.usecase.replenish.ReplenishAvailableCreditUseCase;
import br.com.vps.consulting.b2b.management.partner.application.usecase.list.PartnerListOutput;
import br.com.vps.consulting.b2b.management.partner.domain.Partner;
import br.com.vps.consulting.b2b.management.partner.domain.PartnerCredit;
import br.com.vps.consulting.b2b.management.partner.domain.PartnerId;
import br.com.vps.consulting.b2b.management.partner.domain.exception.PartnerNotFoundException;
import br.com.vps.consulting.b2b.management.shared.core.page.PageCustom;
import br.com.vps.consulting.b2b.management.shared.infrastructure.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    @DisplayName("GET /api/v1/b2b/partners → 200 with paginated list")
    void shouldListPartnersAndReturn200() throws Exception {
        given(listPartnersUseCase.execute(any())).willReturn(emptyPage());

        mockMvc.perform(get("/api/v1/b2b/partners"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/b2b/partners → 200 returns partner fields in items")
    void shouldReturnPartnerFieldsInListItems() throws Exception {
        final var id = UUID.randomUUID();
        final var partner = Partner.builder()
                .id(PartnerId.from(id))
                .name("Acme Corp")
                .document("12345678000100")
                .creditLimit(new BigDecimal("5000.00"))
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
    @DisplayName("POST /api/v1/b2b/partners → 201 with partner id")
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

    @Test
    @DisplayName("POST /api/v1/b2b/partners → 400 when name is blank")
    void shouldReturn400WhenNameIsBlank() throws Exception {
        mockMvc.perform(post("/api/v1/b2b/partners")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"","document":"12345678901234","creditLimit":5000.00}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("POST /api/v1/b2b/partners → 400 when creditLimit is negative")
    void shouldReturn400WhenCreditLimitIsNegative() throws Exception {
        mockMvc.perform(post("/api/v1/b2b/partners")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Acme","document":"12345678901234","creditLimit":-1.00}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("PATCH /api/v1/b2b/partners/{id}/credit-limit → 204 on success")
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
    @DisplayName("PATCH /api/v1/b2b/partners/{id}/credit-limit → 400 when body is invalid")
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
    @DisplayName("PATCH /api/v1/b2b/partners/{id}/available-credit → 204 on success")
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
    @DisplayName("PATCH /api/v1/b2b/partners/{id}/available-credit → 400 when amount is null")
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
    @DisplayName("GET /api/v1/b2b/partners/{id}/credit → 200 with full credit data")
    void shouldReturnPartnerCreditData() throws Exception {
        final var id = UUID.randomUUID();
        final var credit = new PartnerCredit(
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
    @DisplayName("GET /api/v1/b2b/partners/{id}/credit → 404 when partner not found")
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

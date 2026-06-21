package br.com.vps.consulting.b2b.management.order.infrastructure.api;

import br.com.vps.consulting.b2b.management.order.application.usecase.create.CreateOrderUseCase;
import br.com.vps.consulting.b2b.management.order.application.usecase.find.FindOrderByIdUseCase;
import br.com.vps.consulting.b2b.management.order.application.usecase.find.OrderOutput;
import br.com.vps.consulting.b2b.management.order.application.usecase.list.item.ListOrderItemsUseCase;
import br.com.vps.consulting.b2b.management.order.application.usecase.list.item.OrderItemListOutput;
import br.com.vps.consulting.b2b.management.order.application.usecase.list.order.ListOrdersUseCase;
import br.com.vps.consulting.b2b.management.order.application.usecase.list.order.OrderListOutput;
import br.com.vps.consulting.b2b.management.order.application.usecase.update.UpdateOrderStatusUseCase;
import br.com.vps.consulting.b2b.management.order.domain.exception.InvalidOrderTransitionException;
import br.com.vps.consulting.b2b.management.order.domain.exception.OrderNotFoundException;
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
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@Import(GlobalExceptionHandler.class)
class OrderControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean CreateOrderUseCase createOrderUseCase;
    @MockitoBean FindOrderByIdUseCase findOrderByIdUseCase;
    @MockitoBean ListOrdersUseCase listOrdersUseCase;
    @MockitoBean ListOrderItemsUseCase listOrderItemsUseCase;
    @MockitoBean UpdateOrderStatusUseCase updateOrderStatusUseCase;

    @Test
    @DisplayName("Given a valid order request, when POST /api/v1/b2b/orders is called, should return 201 with the order id")
    void shouldCreateOrderAndReturn201() throws Exception {
        final var id = UUID.randomUUID();
        given(createOrderUseCase.execute(any())).willReturn(id);

        mockMvc.perform(post("/api/v1/b2b/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "partnerId":"%s",
                                  "items":[{"productId":"PROD-1","quantity":2,"unitPrice":50.00}]
                                }
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    @DisplayName("Given an empty items list, when POST /api/v1/b2b/orders is called, should return 400")
    void shouldReturn400WhenItemsIsEmpty() throws Exception {
        mockMvc.perform(post("/api/v1/b2b/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"partnerId":"%s","items":[]}
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @DisplayName("Given an item with invalid productId, quantity or unitPrice, when POST /api/v1/b2b/orders is called, should return 400")
    @CsvSource({
            "'', 2, 50.00",
            "PROD-1, 0, 50.00",
            "PROD-1, 2, -10.00",
    })
    void shouldReturn400WhenItemIsInvalid(final String productId, final int quantity, final String unitPrice)
            throws Exception {
        mockMvc.perform(post("/api/v1/b2b/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "partnerId":"%s",
                                  "items":[{"productId":"%s","quantity":%d,"unitPrice":%s}]
                                }
                                """.formatted(UUID.randomUUID(), productId, quantity, unitPrice)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Given a request, when GET /api/v1/b2b/orders is called, should return 200 with a paginated list")
    void shouldListOrdersAndReturn200() throws Exception {
        final var page = emptyPage();
        given(listOrdersUseCase.execute(any())).willReturn(page);

        mockMvc.perform(get("/api/v1/b2b/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    @DisplayName("Given an existing order, when GET /api/v1/b2b/orders/{id} is called, should return 200 with the order details")
    void shouldFindOrderByIdAndReturn200() throws Exception {
        final var id = UUID.randomUUID();
        final var now = OffsetDateTime.now(ZoneOffset.ofHours(-3));
        given(findOrderByIdUseCase.execute(id))
                .willReturn(new OrderOutput(id, UUID.randomUUID(), new BigDecimal("100.00"), "PENDING", now, now));

        mockMvc.perform(get("/api/v1/b2b/orders/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("Given a non-existing order, when GET /api/v1/b2b/orders/{id} is called, should return 404")
    void shouldReturn404WhenOrderNotFound() throws Exception {
        final var id = UUID.randomUUID();
        given(findOrderByIdUseCase.execute(id)).willThrow(new OrderNotFoundException(id));

        mockMvc.perform(get("/api/v1/b2b/orders/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Given an existing order, when GET /api/v1/b2b/orders/{id}/items is called, should return 200 with the paginated items")
    void shouldListOrderItemsAndReturn200() throws Exception {
        final var id = UUID.randomUUID();
        final var page = PageCustom.<OrderItemListOutput>builder()
                .pageNumber(0).pageSize(20).numberOfElements(0)
                .totalPages(0).totalElements(0L).items(List.of())
                .build();
        given(listOrderItemsUseCase.execute(any())).willReturn(page);

        mockMvc.perform(get("/api/v1/b2b/orders/{id}/items", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    @DisplayName("Given a valid status transition, when PATCH /api/v1/b2b/orders/{id}/status is called, should return 204")
    void shouldUpdateOrderStatusAndReturn204() throws Exception {
        final var id = UUID.randomUUID();
        doNothing().when(updateOrderStatusUseCase).execute(any());

        mockMvc.perform(patch("/api/v1/b2b/orders/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"targetStatus":"APPROVED"}
                                """))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Given an invalid status transition, when PATCH /api/v1/b2b/orders/{id}/status is called, should return 422")
    void shouldReturn422OnInvalidTransition() throws Exception {
        final var id = UUID.randomUUID();
        doThrow(new InvalidOrderTransitionException(id, null, null))
                .when(updateOrderStatusUseCase).execute(any());

        mockMvc.perform(patch("/api/v1/b2b/orders/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"targetStatus":"DELIVERED"}
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").exists());
    }

    private static PageCustom<OrderListOutput> emptyPage() {
        return PageCustom.<OrderListOutput>builder()
                .pageNumber(0).pageSize(20).numberOfElements(0)
                .totalPages(0).totalElements(0L).items(List.of())
                .build();
    }

}

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
    @DisplayName("POST /api/v1/b2b/orders → 201 with order id")
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
    @DisplayName("POST /api/v1/b2b/orders → 400 when items list is empty")
    void shouldReturn400WhenItemsIsEmpty() throws Exception {
        mockMvc.perform(post("/api/v1/b2b/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"partnerId":"%s","items":[]}
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/b2b/orders → 400 when item quantity is zero")
    void shouldReturn400WhenItemQuantityIsZero() throws Exception {
        mockMvc.perform(post("/api/v1/b2b/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "partnerId":"%s",
                                  "items":[{"productId":"PROD-1","quantity":0,"unitPrice":10.00}]
                                }
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("GET /api/v1/b2b/orders → 200 with paginated list")
    void shouldListOrdersAndReturn200() throws Exception {
        final var page = emptyPage();
        given(listOrdersUseCase.execute(any())).willReturn(page);

        mockMvc.perform(get("/api/v1/b2b/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/b2b/orders/{id} → 200 with order details")
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
    @DisplayName("GET /api/v1/b2b/orders/{id} → 404 when order not found")
    void shouldReturn404WhenOrderNotFound() throws Exception {
        final var id = UUID.randomUUID();
        given(findOrderByIdUseCase.execute(id)).willThrow(new OrderNotFoundException(id));

        mockMvc.perform(get("/api/v1/b2b/orders/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("GET /api/v1/b2b/orders/{id}/items → 200 with paginated items")
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
    @DisplayName("PATCH /api/v1/b2b/orders/{id}/status → 204 on valid transition")
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
    @DisplayName("PATCH /api/v1/b2b/orders/{id}/status → 422 on invalid transition")
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

package br.com.vps.consulting.b2b.management.order.application.usecase.list.order;

import br.com.vps.consulting.b2b.management.order.domain.Order;
import br.com.vps.consulting.b2b.management.order.domain.OrderId;
import br.com.vps.consulting.b2b.management.order.domain.OrderItem;
import br.com.vps.consulting.b2b.management.order.domain.OrderRepository;
import br.com.vps.consulting.b2b.management.order.domain.OrderStatus;
import br.com.vps.consulting.b2b.management.partner.domain.PartnerId;
import br.com.vps.consulting.b2b.management.shared.core.page.PageCustom;
import br.com.vps.consulting.b2b.management.shared.core.vo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.vps.consulting.b2b.management.shared.core.exception.DomainException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultListOrdersUseCaseTest {

    @Mock
    private OrderRepository orderRepository;
    @InjectMocks
    private DefaultListOrdersUseCase useCase;

    @Test
    @DisplayName("Should return mapped PageCustom<OrderListOutput> from repository page")
    void shouldReturnMappedPage() {
        final var from = Instant.now().minusSeconds(3600);
        final var to = Instant.now();
        final var page = PageCustom.<Order>builder()
                .pageNumber(0).pageSize(10).numberOfElements(1)
                .totalPages(1).totalElements(1L).items(List.of(pendingOrder()))
                .build();
        when(orderRepository.findByFilter(from, to, null, null, 10, 0)).thenReturn(page);

        final var result = useCase.execute(new ListOrdersInput(from, to, null, null, 10, 0));

        assertThat(result.items()).hasSize(1);
        assertThat(result.totalElements()).isEqualTo(1L);
        assertThat(result.pageNumber()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should forward all input parameters to repository")
    void shouldPassInputParamsToRepository() {
        final var from = Instant.now().minusSeconds(3600);
        final var to = Instant.now();
        when(orderRepository.findByFilter(from, to, OrderStatus.PENDING, null, 5, 2))
                .thenReturn(emptyPage());

        useCase.execute(new ListOrdersInput(from, to, OrderStatus.PENDING, null, 5, 2));

        verify(orderRepository).findByFilter(from, to, OrderStatus.PENDING, null, 5, 2);
    }

    @Test
    @DisplayName("Should forward partnerId filter to repository")
    void shouldPassPartnerIdToRepository() {
        final var partnerId = java.util.UUID.randomUUID();
        when(orderRepository.findByFilter(null, null, null, partnerId, 10, 0))
                .thenReturn(emptyPage());

        useCase.execute(new ListOrdersInput(null, null, null, partnerId, 10, 0));

        verify(orderRepository).findByFilter(null, null, null, partnerId, 10, 0);
    }

    @Test
    @DisplayName("Should throw DomainException when from is after to")
    void shouldThrowWhenFromIsAfterTo() {
        final var from = Instant.now();
        final var to = from.minusSeconds(1);

        assertThatThrownBy(() -> useCase.execute(new ListOrdersInput(from, to, null, null, 10, 0)))
                .isInstanceOf(DomainException.class)
                .hasMessage("A data inicial não pode ser posterior à data final");

        verifyNoInteractions(orderRepository);
    }

    private static Order pendingOrder() {
        return Order.builder()
                .id(OrderId.generate())
                .partnerId(PartnerId.generate())
                .items(List.of(OrderItem.builder()
                        .productId("PROD-001").quantity(1)
                        .unitPrice(Money.of(new BigDecimal("100.00"))).build()))
                .totalAmount(Money.of(new BigDecimal("100.00")))
                .status(OrderStatus.PENDING)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    private static PageCustom<Order> emptyPage() {
        return PageCustom.<Order>builder()
                .pageNumber(0).pageSize(5).numberOfElements(0)
                .totalPages(0).totalElements(0L).items(List.of())
                .build();
    }

}

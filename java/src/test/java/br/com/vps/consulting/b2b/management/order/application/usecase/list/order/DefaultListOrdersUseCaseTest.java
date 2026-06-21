package br.com.vps.consulting.b2b.management.order.application.usecase.list.order;

import br.com.vps.consulting.b2b.management.order.domain.OrderRepository;
import br.com.vps.consulting.b2b.management.order.domain.OrderStatus;
import br.com.vps.consulting.b2b.management.order.domain.projection.OrderProjection;
import br.com.vps.consulting.b2b.management.shared.core.page.PageCustom;
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
    @DisplayName("Given a page of orders from the repository, when execute is called, should return a mapped PageCustom<OrderListOutput>")
    void shouldReturnMappedPage() {
        final var from = Instant.now().minusSeconds(3600);
        final var to = Instant.now();
        final var page = PageCustom.<OrderProjection>builder()
                .pageNumber(0).pageSize(10).numberOfElements(1)
                .totalPages(1).totalElements(1L).items(List.of(pendingProjection()))
                .build();
        when(orderRepository.findByFilter(from, to, null, null, 10, 0)).thenReturn(page);

        final var result = useCase.execute(new ListOrdersInput(from, to, null, null, 10, 0));

        assertThat(result.items()).hasSize(1);
        assertThat(result.totalElements()).isEqualTo(1L);
        assertThat(result.pageNumber()).isEqualTo(0);
    }

    @Test
    @DisplayName("Given a filter input, when execute is called, should forward all parameters to the repository")
    void shouldPassInputParamsToRepository() {
        final var from = Instant.now().minusSeconds(3600);
        final var to = Instant.now();
        when(orderRepository.findByFilter(from, to, OrderStatus.PENDING, null, 5, 2))
                .thenReturn(emptyPage());

        useCase.execute(new ListOrdersInput(from, to, OrderStatus.PENDING, null, 5, 2));

        verify(orderRepository).findByFilter(from, to, OrderStatus.PENDING, null, 5, 2);
    }

    @Test
    @DisplayName("Given a partnerId filter, when execute is called, should forward it to the repository")
    void shouldPassPartnerIdToRepository() {
        final var partnerId = java.util.UUID.randomUUID();
        when(orderRepository.findByFilter(null, null, null, partnerId, 10, 0))
                .thenReturn(emptyPage());

        useCase.execute(new ListOrdersInput(null, null, null, partnerId, 10, 0));

        verify(orderRepository).findByFilter(null, null, null, partnerId, 10, 0);
    }

    @Test
    @DisplayName("Given a from date after the to date, when execute is called, should throw DomainException")
    void shouldThrowWhenFromIsAfterTo() {
        final var from = Instant.now();
        final var to = from.minusSeconds(1);

        assertThatThrownBy(() -> useCase.execute(new ListOrdersInput(from, to, null, null, 10, 0)))
                .isInstanceOf(DomainException.class)
                .hasMessage("A data inicial não pode ser posterior à data final");

        verifyNoInteractions(orderRepository);
    }

    private static OrderProjection pendingProjection() {
        final var now = Instant.now();
        return new OrderProjection() {
            @Override
            public java.util.UUID getId() {
                return java.util.UUID.randomUUID();
            }

            @Override
            public java.util.UUID getPartnerId() {
                return java.util.UUID.randomUUID();
            }

            @Override
            public BigDecimal getTotalAmount() {
                return new BigDecimal("100.00");
            }

            @Override
            public OrderStatus getStatus() {
                return OrderStatus.PENDING;
            }

            @Override
            public Instant getCreatedAt() {
                return now;
            }

            @Override
            public Instant getUpdatedAt() {
                return now;
            }
        };
    }

    private static PageCustom<OrderProjection> emptyPage() {
        return PageCustom.<OrderProjection>builder()
                .pageNumber(0).pageSize(5).numberOfElements(0)
                .totalPages(0).totalElements(0L).items(List.of())
                .build();
    }

}

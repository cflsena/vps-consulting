package br.com.vps.consulting.b2b.management.order.application.usecase.find;

import br.com.vps.consulting.b2b.management.order.domain.OrderId;
import br.com.vps.consulting.b2b.management.order.domain.OrderRepository;
import br.com.vps.consulting.b2b.management.order.domain.OrderStatus;
import br.com.vps.consulting.b2b.management.order.domain.exception.OrderNotFoundException;
import br.com.vps.consulting.b2b.management.order.domain.projection.OrderProjection;
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
class DefaultFindOrderByIdUseCaseTest {

    @Mock private OrderRepository orderRepository;
    @InjectMocks private DefaultFindOrderByIdUseCase useCase;

    @Test
    @DisplayName("Given an existing order, when execute is called, should return an OrderOutput with the fields correctly mapped")
    void shouldReturnMappedOrderOutput() {
        final var orderId = UUID.randomUUID();
        final var partnerId = UUID.randomUUID();
        final var projection = projectionOf(orderId, partnerId);
        when(orderRepository.findOrderDetailsById(OrderId.from(orderId))).thenReturn(Optional.of(projection));

        final var result = useCase.execute(orderId);

        assertThat(result.id()).isEqualTo(orderId);
        assertThat(result.partnerId()).isEqualTo(partnerId);
        assertThat(result.status()).isEqualTo(OrderStatus.PENDING.name());
        assertThat(result.totalAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("Given a non-existing order, when execute is called, should throw OrderNotFoundException")
    void shouldThrowWhenOrderNotFound() {
        final var orderId = UUID.randomUUID();
        when(orderRepository.findOrderDetailsById(OrderId.from(orderId))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(orderId))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("Pedido não encontrado");
    }

    private static OrderProjection projectionOf(final UUID orderId, final UUID partnerId) {
        return new OrderProjection() {
            @Override
            public UUID getId() {
                return orderId;
            }

            @Override
            public UUID getPartnerId() {
                return partnerId;
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
                return Instant.now();
            }

            @Override
            public Instant getUpdatedAt() {
                return Instant.now();
            }
        };
    }
}

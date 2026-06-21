package br.com.vps.consulting.b2b.management.order.application.usecase.find;

import br.com.vps.consulting.b2b.management.order.domain.Order;
import br.com.vps.consulting.b2b.management.order.domain.OrderId;
import br.com.vps.consulting.b2b.management.order.domain.OrderItem;
import br.com.vps.consulting.b2b.management.order.domain.OrderRepository;
import br.com.vps.consulting.b2b.management.order.domain.OrderStatus;
import br.com.vps.consulting.b2b.management.order.domain.exception.OrderNotFoundException;
import br.com.vps.consulting.b2b.management.partner.domain.PartnerId;
import br.com.vps.consulting.b2b.management.shared.core.vo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
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
    @DisplayName("Should return OrderOutput with correct fields mapped from found order")
    void shouldReturnMappedOrderOutput() {
        final var orderId = UUID.randomUUID();
        final var partnerId = UUID.randomUUID();
        final var order = orderOf(orderId, partnerId);
        when(orderRepository.findById(OrderId.from(orderId))).thenReturn(Optional.of(order));

        final var result = useCase.execute(orderId);

        assertThat(result.id()).isEqualTo(orderId);
        assertThat(result.partnerId()).isEqualTo(partnerId);
        assertThat(result.status()).isEqualTo(OrderStatus.PENDING.name());
        assertThat(result.totalAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("Should throw OrderNotFoundException when order does not exist")
    void shouldThrowWhenOrderNotFound() {
        final var orderId = UUID.randomUUID();
        when(orderRepository.findById(OrderId.from(orderId))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(orderId))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("Pedido não encontrado");
    }

    private static Order orderOf(final UUID orderId, final UUID partnerId) {
        return Order.builder()
                .id(OrderId.from(orderId))
                .partnerId(PartnerId.from(partnerId))
                .items(List.of(OrderItem.builder()
                        .productId("PROD-001").quantity(2)
                        .unitPrice(Money.of(new BigDecimal("50.00"))).build()))
                .totalAmount(Money.of(new BigDecimal("100.00")))
                .status(OrderStatus.PENDING)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}

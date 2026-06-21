package br.com.vps.consulting.b2b.management.order.application.usecase.create;

import br.com.vps.consulting.b2b.management.order.application.service.PartnerCreditService;
import br.com.vps.consulting.b2b.management.order.domain.Order;
import br.com.vps.consulting.b2b.management.order.domain.OrderItem;
import br.com.vps.consulting.b2b.management.order.domain.OrderRepository;
import br.com.vps.consulting.b2b.management.order.domain.event.OrderCreated;
import br.com.vps.consulting.b2b.management.partner.domain.PartnerId;
import br.com.vps.consulting.b2b.management.partner.domain.exception.InsufficientCreditException;
import br.com.vps.consulting.b2b.management.partner.domain.exception.PartnerNotFoundException;
import br.com.vps.consulting.b2b.management.shared.core.event.EventPublisher;
import br.com.vps.consulting.b2b.management.shared.core.vo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultCreateOrderUseCaseTest {

    @Mock private OrderRepository orderRepository;
    @Mock private PartnerCreditService partnerCreditService;
    @Mock private EventPublisher eventPublisher;
    @InjectMocks private DefaultCreateOrderUseCase useCase;

    @Test
    @DisplayName("Given a valid input, when execute is called, should create the order and return its UUID")
    void shouldCreateOrderAndReturnUUID() {
        final var partnerId = UUID.randomUUID();
        final var saved = newPendingOrder(partnerId);
        when(orderRepository.save(any())).thenReturn(saved);

        final var result = useCase.execute(newInput(partnerId));

        assertThat(result).isEqualTo(saved.getId().value());
        verify(orderRepository).save(any(Order.class));
        verify(eventPublisher).publish(any(OrderCreated.class));
    }

    @Test
    @DisplayName("Given a valid input, when execute is called, should reserve credit with the partnerId and order total amount")
    void shouldReserveCreditWithCorrectArgs() {
        final var partnerId = UUID.randomUUID();
        when(orderRepository.save(any())).thenReturn(newPendingOrder(partnerId));

        useCase.execute(newInput(partnerId));

        verify(partnerCreditService).reserveCredit(eq(partnerId), any(BigDecimal.class));
    }

    @Test
    @DisplayName("Given a partner that does not exist, when execute is called, should propagate PartnerNotFoundException from the credit service")
    void shouldThrowWhenPartnerNotFound() {
        final var partnerId = UUID.randomUUID();
        doThrow(new PartnerNotFoundException(partnerId))
                .when(partnerCreditService).reserveCredit(any(), any());

        assertThatThrownBy(() -> useCase.execute(newInput(partnerId)))
                .isInstanceOf(PartnerNotFoundException.class)
                .hasMessageContaining("Parceiro não encontrado");
    }

    @Test
    @DisplayName("Given insufficient credit, when execute is called, should propagate InsufficientCreditException from the credit service")
    void shouldThrowWhenInsufficientCredit() {
        final var partnerId = UUID.randomUUID();
        doThrow(new InsufficientCreditException(partnerId, BigDecimal.TEN, BigDecimal.ZERO))
                .when(partnerCreditService).reserveCredit(any(), any());

        assertThatThrownBy(() -> useCase.execute(newInput(partnerId)))
                .isInstanceOf(InsufficientCreditException.class)
                .hasMessageContaining("Crédito insuficiente");
    }

    @Test
    @DisplayName("Given a failed credit reservation, when execute is called, should not save the order")
    void shouldNotSaveWhenCreditReservationFails() {
        final var partnerId = UUID.randomUUID();
        doThrow(new InsufficientCreditException(partnerId, BigDecimal.TEN, BigDecimal.ZERO))
                .when(partnerCreditService).reserveCredit(any(), any());

        assertThatThrownBy(() -> useCase.execute(newInput(partnerId)))
                .isInstanceOf(InsufficientCreditException.class);

        verify(orderRepository, never()).save(any());
    }

    private static CreateOrderInput newInput(final UUID partnerId) {
        return new CreateOrderInput(partnerId, List.of(
                new CreateOrderInput.Item("PROD-001", 2, new BigDecimal("50.00"))
        ));
    }

    private static Order newPendingOrder(final UUID partnerId) {
        return Order.createPending()
                .partnerId(PartnerId.from(partnerId))
                .items(List.of(OrderItem.builder()
                        .productId("PROD-001")
                        .quantity(2)
                        .unitPrice(Money.of(new BigDecimal("50.00")))
                        .build()))
                .build();
    }

}

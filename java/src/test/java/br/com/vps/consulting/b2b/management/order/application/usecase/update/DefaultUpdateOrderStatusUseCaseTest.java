package br.com.vps.consulting.b2b.management.order.application.usecase.update;

import br.com.vps.consulting.b2b.management.order.application.service.PartnerCreditService;
import br.com.vps.consulting.b2b.management.order.domain.Order;
import br.com.vps.consulting.b2b.management.order.domain.OrderId;
import br.com.vps.consulting.b2b.management.order.domain.OrderItem;
import br.com.vps.consulting.b2b.management.order.domain.OrderRepository;
import br.com.vps.consulting.b2b.management.order.domain.OrderStatus;
import br.com.vps.consulting.b2b.management.order.domain.event.OrderStatusChanged;
import br.com.vps.consulting.b2b.management.order.domain.exception.InvalidOrderTransitionException;
import br.com.vps.consulting.b2b.management.order.domain.exception.OrderNotFoundException;
import br.com.vps.consulting.b2b.management.partner.domain.PartnerId;
import br.com.vps.consulting.b2b.management.shared.core.event.EventPublisher;
import br.com.vps.consulting.b2b.management.shared.core.vo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultUpdateOrderStatusUseCaseTest {

    @Mock private OrderRepository orderRepository;
    @Mock private PartnerCreditService partnerCreditService;
    @Mock private EventPublisher eventPublisher;
    @InjectMocks private DefaultUpdateOrderStatusUseCase useCase;

    @Test
    @DisplayName("Given a valid target status, when execute is called, should transition the order and save it")
    void shouldUpdateOrderStatusAndSave() {
        final var orderId = UUID.randomUUID();
        final var order = orderInState(orderId, OrderStatus.PENDING);
        when(orderRepository.findById(OrderId.from(orderId))).thenReturn(Optional.of(order));

        useCase.execute(new UpdateOrderStatusInput(orderId, OrderStatus.APPROVED));

        assertThat(order.getStatus()).isEqualTo(OrderStatus.APPROVED);
        verify(orderRepository).save(order);
    }

    @Test
    @DisplayName("Given a status transition, when execute is called, should publish an OrderStatusChanged event with the correct previous and new status")
    void shouldPublishOrderStatusChangedEvent() {
        final var orderId = UUID.randomUUID();
        final var order = orderInState(orderId, OrderStatus.PENDING);
        when(orderRepository.findById(OrderId.from(orderId))).thenReturn(Optional.of(order));

        useCase.execute(new UpdateOrderStatusInput(orderId, OrderStatus.APPROVED));

        final var captor = ArgumentCaptor.forClass(OrderStatusChanged.class);
        verify(eventPublisher).publish(captor.capture());
        assertThat(captor.getValue().previousStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(captor.getValue().newStatus()).isEqualTo(OrderStatus.APPROVED);
    }

    @Test
    @DisplayName("Given an order transitioning from PENDING to APPROVED, when execute is called, should debit the reservation")
    void shouldDebitReservationWhenApprovingPendingOrder() {
        final var partnerId = UUID.randomUUID();
        final var order = orderInStateWithPartner(UUID.randomUUID(), partnerId, OrderStatus.PENDING);
        when(orderRepository.findById(any())).thenReturn(Optional.of(order));

        useCase.execute(new UpdateOrderStatusInput(order.getId().value(), OrderStatus.APPROVED));

        verify(partnerCreditService).debitReservation(eq(partnerId), any(BigDecimal.class));
    }

    @Test
    @DisplayName("Given a non-approval transition, when execute is called, should not interact with the credit service")
    void shouldNotInteractWithCreditServiceOnOtherTransitions() {
        final var order = orderInState(UUID.randomUUID(), OrderStatus.APPROVED);
        when(orderRepository.findById(any())).thenReturn(Optional.of(order));

        useCase.execute(new UpdateOrderStatusInput(order.getId().value(), OrderStatus.IN_PROCESS));

        verify(partnerCreditService, never()).debitReservation(any(), any());
        verify(partnerCreditService, never()).releaseReservation(any(), any());
        verify(partnerCreditService, never()).refundDebit(any(), any());
    }

    @Test
    @DisplayName("Given a PENDING order being canceled, when execute is called, should release the reservation")
    void shouldReleaseReservationWhenCancelingPendingOrder() {
        final var partnerId = UUID.randomUUID();
        final var order = orderInStateWithPartner(UUID.randomUUID(), partnerId, OrderStatus.PENDING);
        when(orderRepository.findById(any())).thenReturn(Optional.of(order));

        useCase.execute(new UpdateOrderStatusInput(order.getId().value(), OrderStatus.CANCELED));

        verify(partnerCreditService).releaseReservation(eq(partnerId), any(BigDecimal.class));
        verify(partnerCreditService, never()).refundDebit(any(), any());
    }

    @Test
    @DisplayName("Given an APPROVED order being canceled, when execute is called, should refund the debit")
    void shouldRefundDebitWhenCancelingApprovedOrder() {
        final var partnerId = UUID.randomUUID();
        final var order = orderInStateWithPartner(UUID.randomUUID(), partnerId, OrderStatus.APPROVED);
        when(orderRepository.findById(any())).thenReturn(Optional.of(order));

        useCase.execute(new UpdateOrderStatusInput(order.getId().value(), OrderStatus.CANCELED));

        verify(partnerCreditService).refundDebit(eq(partnerId), any(BigDecimal.class));
        verify(partnerCreditService, never()).releaseReservation(any(), any());
    }

    @Test
    @DisplayName("Given an order being canceled, when execute is called, should publish a cancellation event with refundedAmount and currency")
    void shouldPublishCancellationEventWithRefundFields() {
        final var order = orderInState(UUID.randomUUID(), OrderStatus.APPROVED);
        when(orderRepository.findById(any())).thenReturn(Optional.of(order));

        useCase.execute(new UpdateOrderStatusInput(order.getId().value(), OrderStatus.CANCELED));

        final var captor = ArgumentCaptor.forClass(OrderStatusChanged.class);
        verify(eventPublisher).publish(captor.capture());
        final var event = captor.getValue();

        assertThat(event.newStatus()).isEqualTo(OrderStatus.CANCELED);
        assertThat(event.refundedAmount()).isNotNull();
        assertThat(event.currency()).isNotNull();
    }

    @Test
    @DisplayName("Given a non-cancellation transition, when execute is called, should publish an event without refundedAmount")
    void shouldPublishEventWithoutRefundFieldsOnNonCancellation() {
        var order = orderInState(UUID.randomUUID(), OrderStatus.PENDING);
        when(orderRepository.findById(any())).thenReturn(Optional.of(order));

        useCase.execute(new UpdateOrderStatusInput(order.getId().value(), OrderStatus.APPROVED));

        var captor = ArgumentCaptor.forClass(OrderStatusChanged.class);
        verify(eventPublisher).publish(captor.capture());
        assertThat(captor.getValue().refundedAmount()).isNull();
        assertThat(captor.getValue().currency()).isNull();
    }

    @Test
    @DisplayName("Given a non-existing order, when execute is called, should throw OrderNotFoundException")
    void shouldThrowWhenOrderNotFound() {
        var orderId = UUID.randomUUID();
        when(orderRepository.findById(OrderId.from(orderId))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new UpdateOrderStatusInput(orderId, OrderStatus.APPROVED)))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("Pedido não encontrado");
    }

    @Test
    @DisplayName("Given an invalid status transition, when execute is called, should throw InvalidOrderTransitionException and not save")
    void shouldThrowOnInvalidTransition() {
        var orderId = UUID.randomUUID();
        var order = orderInState(orderId, OrderStatus.DELIVERED);
        when(orderRepository.findById(OrderId.from(orderId))).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> useCase.execute(new UpdateOrderStatusInput(orderId, OrderStatus.CANCELED)))
                .isInstanceOf(InvalidOrderTransitionException.class)
                .hasMessageContaining("Transição de status inválida");

        verify(orderRepository, never()).save(any());
    }

    private static Order orderInState(final UUID orderId, final OrderStatus status) {
        return orderInStateWithPartner(orderId, UUID.randomUUID(), status);
    }

    private static Order orderInStateWithPartner(final UUID orderId, final UUID partnerId,
                                                  final OrderStatus status) {
        return Order.builder()
                .id(OrderId.from(orderId))
                .partnerId(PartnerId.from(partnerId))
                .items(List.of(OrderItem.builder()
                        .productId("PROD-001").quantity(1)
                        .unitPrice(Money.of(new BigDecimal("100.00"))).build()))
                .totalAmount(Money.of(new BigDecimal("100.00")))
                .status(status)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}

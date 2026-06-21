package br.com.vps.consulting.b2b.management.order.application.usecase.list.item;

import br.com.vps.consulting.b2b.management.order.domain.OrderId;
import br.com.vps.consulting.b2b.management.order.domain.OrderItem;
import br.com.vps.consulting.b2b.management.order.domain.OrderItemRepository;
import br.com.vps.consulting.b2b.management.shared.core.page.PageCustom;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultListOrderItemsUseCaseTest {

    @Mock private OrderItemRepository orderItemRepository;
    @InjectMocks private DefaultListOrderItemsUseCase useCase;

    @Test
    @DisplayName("Given a page of items from the repository, when execute is called, should return a mapped PageCustom<OrderItemListOutput>")
    void shouldReturnMappedItemsPage() {
        final var orderId = UUID.randomUUID();
        final var page = PageCustom.<OrderItem>builder()
                .pageNumber(0).pageSize(10).numberOfElements(1)
                .totalPages(1).totalElements(1L).items(List.of(newItem()))
                .build();
        when(orderItemRepository.findByOrderId(OrderId.from(orderId), 10, 0)).thenReturn(page);

        final var result = useCase.execute(new ListOrderItemsInput(orderId, 10, 0));

        assertThat(result.items()).hasSize(1);
        assertThat(result.totalElements()).isEqualTo(1L);
        assertThat(result.items().get(0).productId()).isEqualTo("PROD-001");
    }

    @Test
    @DisplayName("Given an orderId and pagination parameters, when execute is called, should forward them to the repository")
    void shouldPassOrderIdAndPaginationToRepository() {
        var orderId = UUID.randomUUID();
        when(orderItemRepository.findByOrderId(OrderId.from(orderId), 5, 2))
                .thenReturn(emptyPage());

        useCase.execute(new ListOrderItemsInput(orderId, 5, 2));

        verify(orderItemRepository).findByOrderId(OrderId.from(orderId), 5, 2);
    }

    private static OrderItem newItem() {
        return OrderItem.builder()
                .productId("PROD-001")
                .quantity(3)
                .unitPrice(Money.of(new BigDecimal("25.00")))
                .build();
    }

    private static PageCustom<OrderItem> emptyPage() {
        return PageCustom.<OrderItem>builder()
                .pageNumber(0).pageSize(5).numberOfElements(0)
                .totalPages(0).totalElements(0L).items(List.of())
                .build();
    }
}

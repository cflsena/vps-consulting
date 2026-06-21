package br.com.vps.consulting.b2b.management.order.infrastructure.persistence.jpa;

import br.com.vps.consulting.b2b.management.TestcontainersConfiguration;
import br.com.vps.consulting.b2b.management.order.domain.OrderId;
import br.com.vps.consulting.b2b.management.order.domain.OrderItem;
import br.com.vps.consulting.b2b.management.order.domain.OrderStatus;
import br.com.vps.consulting.b2b.management.order.infrastructure.persistence.DefaultOrderItemRepository;
import br.com.vps.consulting.b2b.management.order.infrastructure.persistence.OrderEntity;
import br.com.vps.consulting.b2b.management.order.infrastructure.persistence.OrderItemEntity;
import br.com.vps.consulting.b2b.management.partner.infrastructure.persistence.PartnerEntity;
import br.com.vps.consulting.b2b.management.partner.infrastructure.persistence.jpa.PartnerJpaRepository;
import br.com.vps.consulting.b2b.management.shared.core.vo.Money;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, DefaultOrderItemRepository.class})
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
class DefaultOrderItemRepositoryIT {

    @Autowired private DefaultOrderItemRepository adapter;
    @Autowired private OrderItemJpaRepository orderItemJpaRepository;
    @Autowired private OrderJpaRepository orderJpaRepository;
    @Autowired private PartnerJpaRepository partnerJpaRepository;

    private final List<UUID> createdOrderIds = new ArrayList<>();
    private final List<UUID> createdPartnerIds = new ArrayList<>();

    @AfterEach
    void cleanup() {
        createdOrderIds.forEach(id -> {
            orderItemJpaRepository.deleteAll(orderItemJpaRepository.findAllByOrderId(id, Pageable.unpaged()).getContent());
            orderJpaRepository.deleteById(id);
        });
        createdOrderIds.clear();
        createdPartnerIds.forEach(partnerJpaRepository::deleteById);
        createdPartnerIds.clear();
    }

    @Test
    @DisplayName("Given an order with items, when findByOrderId is called, should return the paginated items belonging to it")
    void shouldReturnItems_whenOrderHasItems() {
        final var orderId = createOrderWithItems(3);

        final var page = adapter.findByOrderId(OrderId.from(orderId), 20, 0);

        assertThat(page.items()).hasSize(3);
        assertThat(page.totalElements()).isEqualTo(3L);
    }

    @Test
    @DisplayName("Given an order with multiple items, when findByOrderId is called with pageSize and pageNumber, should paginate them accordingly")
    void shouldPaginateItems() {
        final var orderId = createOrderWithItems(5);

        final var firstPage = adapter.findByOrderId(OrderId.from(orderId), 2, 0);
        final var secondPage = adapter.findByOrderId(OrderId.from(orderId), 2, 1);

        assertThat(firstPage.items()).hasSize(2);
        assertThat(firstPage.totalPages()).isEqualTo(3);
        assertThat(firstPage.totalElements()).isEqualTo(5L);
        assertThat(secondPage.pageNumber()).isEqualTo(1);
        assertThat(secondPage.items()).hasSize(2);
    }

    @Test
    @DisplayName("Given an order with no items, when findByOrderId is called, should return an empty page")
    void shouldReturnEmptyPage_whenOrderHasNoItems() {
        final var orderId = createOrderWithItems(0);

        final var page = adapter.findByOrderId(OrderId.from(orderId), 20, 0);

        assertThat(page.items()).isEmpty();
        assertThat(page.totalElements()).isZero();
    }

    @Test
    @DisplayName("Given a non-existing order, when findByOrderId is called, should return an empty page")
    void shouldReturnEmptyPage_whenOrderDoesNotExist() {
        final var page = adapter.findByOrderId(OrderId.generate(), 20, 0);

        assertThat(page.items()).isEmpty();
        assertThat(page.totalElements()).isZero();
    }

    @Test
    @DisplayName("Given an order and a list of items, when saveAll is called, should persist all items linked to that order")
    void shouldPersistItems_whenSaveAllIsCalled() {
        final var orderId = createOrderWithItems(0);
        final var items = List.of(
                OrderItem.builder().productId("PROD-A").quantity(2).unitPrice(Money.of("10.00")).build(),
                OrderItem.builder().productId("PROD-B").quantity(1).unitPrice(Money.of("20.00")).build()
        );

        adapter.saveAll(orderId, items);

        final var persisted = orderItemJpaRepository.findAllByOrderId(orderId, Pageable.unpaged()).getContent();
        assertThat(persisted).hasSize(2);
        assertThat(persisted).extracting(OrderItemEntity::getOrderId).containsOnly(orderId);
        assertThat(persisted).extracting(OrderItemEntity::getProductId).containsExactlyInAnyOrder("PROD-A", "PROD-B");
        assertThat(persisted).extracting(e -> e.getUnitPrice().stripTrailingZeros())
                .containsExactlyInAnyOrder(new BigDecimal("10.00").stripTrailingZeros(), new BigDecimal("20.00").stripTrailingZeros());
    }

    @Test
    @DisplayName("Given an empty items list, when saveAll is called, should persist nothing")
    void shouldPersistNothing_whenSaveAllIsCalledWithEmptyList() {
        final var orderId = createOrderWithItems(0);

        adapter.saveAll(orderId, List.of());

        final var page = adapter.findByOrderId(OrderId.from(orderId), 20, 0);
        assertThat(page.items()).isEmpty();
    }

    private UUID createOrderWithItems(final int itemCount) {
        final var partnerId = UUID.randomUUID();
        partnerJpaRepository.save(PartnerEntity.builder()
                .id(partnerId)
                .name("Order Item IT Partner")
                .document(UUID.randomUUID().toString().replace("-", "").substring(0, 14))
                .createdAt(Instant.now())
                .build());
        createdPartnerIds.add(partnerId);

        final var orderId = UUID.randomUUID();
        orderJpaRepository.save(OrderEntity.builder()
                .id(orderId)
                .partnerId(partnerId)
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("10.00"))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build());
        createdOrderIds.add(orderId);

        for (int i = 0; i < itemCount; i++) {
            orderItemJpaRepository.save(OrderItemEntity.builder()
                    .id(UUID.randomUUID())
                    .orderId(orderId)
                    .productId("PROD-" + i)
                    .quantity(1)
                    .unitPrice(new BigDecimal("10.00"))
                    .build());
        }

        return orderId;
    }

}

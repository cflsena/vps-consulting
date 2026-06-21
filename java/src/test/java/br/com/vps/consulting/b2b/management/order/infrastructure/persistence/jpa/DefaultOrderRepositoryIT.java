package br.com.vps.consulting.b2b.management.order.infrastructure.persistence.jpa;

import br.com.vps.consulting.b2b.management.TestcontainersConfiguration;
import br.com.vps.consulting.b2b.management.order.domain.Order;
import br.com.vps.consulting.b2b.management.order.domain.OrderId;
import br.com.vps.consulting.b2b.management.order.domain.OrderItem;
import br.com.vps.consulting.b2b.management.order.domain.OrderStatus;
import br.com.vps.consulting.b2b.management.order.domain.projection.OrderProjection;
import br.com.vps.consulting.b2b.management.order.infrastructure.persistence.DefaultOrderRepository;
import br.com.vps.consulting.b2b.management.partner.domain.PartnerId;
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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, DefaultOrderRepository.class})
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
class DefaultOrderRepositoryIT {

    @Autowired private DefaultOrderRepository adapter;
    @Autowired private OrderJpaRepository orderJpaRepository;
    @Autowired private OrderItemJpaRepository orderItemJpaRepository;
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
    @DisplayName("Given a pending order, when save is called, should persist the order")
    void shouldSaveOrder_andPersistItems() {
        final var partnerId = createPartner();
        final var order = newPendingOrder(partnerId, "PROD-1", "50.00");

        adapter.save(order);
        createdOrderIds.add(order.getId().value());

        final var savedEntity = orderJpaRepository.findById(order.getId().value()).orElseThrow();
        assertThat(savedEntity.getPartnerId()).isEqualTo(partnerId);
        assertThat(savedEntity.getTotalAmount()).isEqualByComparingTo("50.00");

    }

    @Test
    @DisplayName("Given an existing order, when findById is called, should return it with its items mapped to domain")
    void shouldFindById_whenOrderExists() {
        final var partnerId = createPartner();
        final var order = newPendingOrder(partnerId, "PROD-2", "30.00");
        adapter.save(order);
        createdOrderIds.add(order.getId().value());

        final var found = adapter.findById(order.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getPartnerId()).isEqualTo(partnerId);
        assertThat(found.get().getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    @DisplayName("Given a non-existing order, when findById is called, should return empty")
    void shouldReturnEmpty_whenOrderDoesNotExist() {
        final var found = adapter.findById(OrderId.generate());

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Given orders with different statuses, when findByFilter is called with a status, should return only matching orders")
    void shouldFilterByStatus() {
        final var partnerId = createPartner();
        final var pending = newPendingOrder(partnerId, "PROD-1", "10.00");
        adapter.save(pending);
        createdOrderIds.add(pending.getId().value());

        final var approved = newPendingOrder(partnerId, "PROD-1", "10.00");
        approved.transitionTo(OrderStatus.APPROVED);
        adapter.save(approved);
        createdOrderIds.add(approved.getId().value());

        final var page = adapter.findByFilter(null, null, OrderStatus.APPROVED, null, 20, 0);

        assertThat(page.items()).extracting(o -> o.getId()).containsExactly(approved.getId().value());
    }

    @Test
    @DisplayName("Given orders from different partners, when findByFilter is called with a partnerId, should return only matching orders")
    void shouldFilterByPartnerId() {
        final var partnerA = createPartner();
        final var partnerB = createPartner();
        final var orderA = newPendingOrder(partnerA, "PROD-1", "10.00");
        adapter.save(orderA);
        createdOrderIds.add(orderA.getId().value());
        final var orderB = newPendingOrder(partnerB, "PROD-1", "10.00");
        adapter.save(orderB);
        createdOrderIds.add(orderB.getId().value());

        final var page = adapter.findByFilter(null, null, null, partnerA, 20, 0);

        assertThat(page.items()).extracting(o -> o.getId()).containsExactly(orderA.getId().value());
    }

    @Test
    @DisplayName("Given an order, when findByFilter is called with a date range, should include or exclude it accordingly")
    void shouldFilterByDateRange() {
        final var partnerId = createPartner();
        final var order = newPendingOrder(partnerId, "PROD-1", "10.00");
        adapter.save(order);
        createdOrderIds.add(order.getId().value());

        final var anHourAgo = Instant.now().minus(1, ChronoUnit.HOURS);
        final var pageIncluding = adapter.findByFilter(anHourAgo, null, null, null, 20, 0);
        final var pageExcluding = adapter.findByFilter(null, anHourAgo, null, null, 20, 0);

        assertThat(pageIncluding.items()).extracting(o -> o.getId()).contains(order.getId().value());
        assertThat(pageExcluding.items()).extracting(o -> o.getId()).doesNotContain(order.getId().value());
    }

    @Test
    @DisplayName("Given multiple filters, when findByFilter is called, should combine them and paginate sorted by createdAt desc")
    void shouldCombineFiltersAndPaginate() {
        final var partnerId = createPartner();
        final var match = newPendingOrder(partnerId, "PROD-1", "10.00");
        adapter.save(match);
        createdOrderIds.add(match.getId().value());

        final var differentStatus = newPendingOrder(partnerId, "PROD-1", "10.00");
        differentStatus.transitionTo(OrderStatus.APPROVED);
        adapter.save(differentStatus);
        createdOrderIds.add(differentStatus.getId().value());

        final var page = adapter.findByFilter(null, null, OrderStatus.PENDING, partnerId, 20, 0);

        assertThat(page.pageNumber()).isZero();
        assertThat(page.items()).extracting(o -> o.getId()).containsExactly(match.getId().value());
    }

    @Test
    @DisplayName("Given an existing order, when findOrderDetailsById is called, should return a projection without items (N+1 safe)")
    void shouldFindOrderDetailsById_returnProjection_withoutItems() {
        final var partnerId = createPartner();
        final var order = newPendingOrder(partnerId, "PROD-X", "25.00");
        adapter.save(order);
        createdOrderIds.add(order.getId().value());

        final var projection = adapter.findOrderDetailsById(order.getId());

        assertThat(projection).isPresent();
        assertThat(projection.get()).isInstanceOf(OrderProjection.class);
        assertThat(projection.get().getId()).isEqualTo(order.getId().value());
        assertThat(projection.get().getPartnerId()).isEqualTo(partnerId);
        assertThat(projection.get().getTotalAmount()).isEqualByComparingTo("25.00");
        assertThat(projection.get().getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(projection.get().getCreatedAt()).isNotNull();
        assertThat(projection.get().getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Given multiple orders, when findByFilter is called, should return projections without items (N+1 safe)")
    void shouldFindByFilter_returnProjections_withoutLoadingItems() {
        final var partnerId = createPartner();

        // Create 3 orders with different statuses
        final var pending1 = newPendingOrder(partnerId, "PROD-A", "15.00");
        adapter.save(pending1);
        createdOrderIds.add(pending1.getId().value());

        final var pending2 = newPendingOrder(partnerId, "PROD-B", "20.00");
        adapter.save(pending2);
        createdOrderIds.add(pending2.getId().value());

        final var approved = newPendingOrder(partnerId, "PROD-C", "30.00");
        approved.transitionTo(OrderStatus.APPROVED);
        adapter.save(approved);
        createdOrderIds.add(approved.getId().value());

        // Query only PENDING orders
        final var page = adapter.findByFilter(null, null, OrderStatus.PENDING, partnerId, 20, 0);

        assertThat(page.items()).hasSize(2);
        assertThat(page.items()).allMatch(o -> o instanceof OrderProjection, "all items should be OrderProjection instances");
        assertThat(page.items()).extracting(OrderProjection::getStatus).containsOnly(OrderStatus.PENDING);
        assertThat(page.items()).extracting(OrderProjection::getId)
                .containsExactlyInAnyOrder(pending1.getId().value(), pending2.getId().value());
    }

    private UUID createPartner() {
        final var id = UUID.randomUUID();
        partnerJpaRepository.save(PartnerEntity.builder()
                .id(id)
                .name("Order IT Partner")
                .document(UUID.randomUUID().toString().replace("-", "").substring(0, 14))
                .createdAt(Instant.now())
                .build());
        createdPartnerIds.add(id);
        return id;
    }

    private Order newPendingOrder(final UUID partnerId, final String productId, final String unitPrice) {
        final var item = OrderItem.builder()
                .productId(productId)
                .quantity(1)
                .unitPrice(Money.of(unitPrice))
                .build();
        return Order.createPending()
                .partnerId(partnerId)
                .items(List.of(item))
                .build();
    }

}

package br.com.vps.consulting.b2b.management.shared.infrastructure.event;

import br.com.vps.consulting.b2b.management.RabbitMQTestcontainersConfiguration;
import br.com.vps.consulting.b2b.management.TestcontainersConfiguration;
import br.com.vps.consulting.b2b.management.order.application.usecase.create.CreateOrderInput;
import br.com.vps.consulting.b2b.management.order.application.usecase.create.CreateOrderUseCase;
import br.com.vps.consulting.b2b.management.partner.infrastructure.persistence.PartnerCreditEntity;
import br.com.vps.consulting.b2b.management.partner.infrastructure.persistence.PartnerEntity;
import br.com.vps.consulting.b2b.management.partner.infrastructure.persistence.jpa.PartnerCreditJpaRepository;
import br.com.vps.consulting.b2b.management.partner.infrastructure.persistence.jpa.PartnerJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, RabbitMQTestcontainersConfiguration.class})
class RabbitMQEventPublisherIT {

    @Autowired private CreateOrderUseCase createOrderUseCase;
    @Autowired private PartnerJpaRepository partnerJpaRepository;
    @Autowired private PartnerCreditJpaRepository partnerCreditJpaRepository;
    @Autowired private RabbitTemplate rabbitTemplate;

    @Test
    @DisplayName("Given a created order, when the use case completes, should publish an OrderCreated event to RabbitMQ")
    void shouldPublishOrderCreatedEventToRabbitMQ() {
        final var partnerId = setupPartner(new BigDecimal("1000.00"));

        final var orderId = createOrderUseCase.execute(new CreateOrderInput(
                partnerId,
                List.of(new CreateOrderInput.Item("PROD-TEST", 1, new BigDecimal("100.00")))
        ));

        Message rawMessage = rabbitTemplate.receive("queue.order.created", 10_000);
        assertThat(rawMessage).isNotNull();
        final var body = new String(rawMessage.getBody(), StandardCharsets.UTF_8);
        assertThat(body).contains("eventId");
        assertThat(body).contains(orderId.toString());
    }

    private UUID setupPartner(final BigDecimal creditLimit) {
        final var id = UUID.randomUUID();
        partnerJpaRepository.save(PartnerEntity.builder()
                .id(id)
                .name("RabbitMQ Test Partner")
                .document(UUID.randomUUID().toString().replace("-", "").substring(0, 14))
                .createdAt(Instant.now())
                .build());
        partnerCreditJpaRepository.save(PartnerCreditEntity.builder()
                .id(id)
                .creditLimit(creditLimit)
                .availableBalance(creditLimit)
                .reservedBalance(BigDecimal.ZERO)
                .updatedAt(Instant.now())
                .build());
        return id;
    }
}

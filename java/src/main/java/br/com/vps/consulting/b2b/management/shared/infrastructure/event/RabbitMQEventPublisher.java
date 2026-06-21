package br.com.vps.consulting.b2b.management.shared.infrastructure.event;

import br.com.vps.consulting.b2b.management.order.domain.event.OrderCreated;
import br.com.vps.consulting.b2b.management.order.domain.event.OrderStatusChanged;
import br.com.vps.consulting.b2b.management.shared.core.event.DomainEvent;
import br.com.vps.consulting.b2b.management.shared.core.event.EventMessage;
import br.com.vps.consulting.b2b.management.shared.core.event.EventPublisher;
import br.com.vps.consulting.b2b.management.shared.infrastructure.config.RabbitMQConfig;
import jakarta.inject.Named;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Map;

@Named
@RequiredArgsConstructor
public class RabbitMQEventPublisher implements EventPublisher {

    private static final Map<Class<? extends DomainEvent>, String> ROUTING_KEYS = Map.of(
            OrderCreated.class, "order.created",
            OrderStatusChanged.class, "order.status.changed"
    );

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publish(final DomainEvent event) {
        final String routingKey = ROUTING_KEYS.get(event.getClass());
        if (routingKey == null) {
            throw new IllegalArgumentException("No routing key configured for event: " + event.getClass().getSimpleName());
        }
        final EventMessage<DomainEvent> message = EventMessage.of(event);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, routingKey, message);
    }
}

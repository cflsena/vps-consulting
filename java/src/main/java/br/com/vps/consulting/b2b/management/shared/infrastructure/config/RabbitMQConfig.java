package br.com.vps.consulting.b2b.management.shared.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "b2b.events";
    public static final String QUEUE_ORDER_CREATED = "queue.order.created";
    public static final String QUEUE_ORDER_STATUS_CHANGED = "queue.order.status.changed";

    @Bean
    public TopicExchange b2bEventsExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue orderCreatedQueue() {
        return new Queue(QUEUE_ORDER_CREATED, true);
    }

    @Bean
    public Queue orderStatusChangedQueue() {
        return new Queue(QUEUE_ORDER_STATUS_CHANGED, true);
    }

    @Bean
    public Binding orderCreatedBinding(final Queue orderCreatedQueue, final TopicExchange b2bEventsExchange) {
        return BindingBuilder.bind(orderCreatedQueue).to(b2bEventsExchange).with("order.created");
    }

    @Bean
    public Binding orderStatusChangedBinding(final Queue orderStatusChangedQueue, final TopicExchange b2bEventsExchange) {
        return BindingBuilder.bind(orderStatusChangedQueue).to(b2bEventsExchange).with("order.status.changed");
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(final ConnectionFactory connectionFactory,
                                         final Jackson2JsonMessageConverter messageConverter) {
        final RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}

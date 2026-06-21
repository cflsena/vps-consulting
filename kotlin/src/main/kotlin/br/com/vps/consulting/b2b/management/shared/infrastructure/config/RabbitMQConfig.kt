package br.com.vps.consulting.b2b.management.shared.infrastructure.config

import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import tools.jackson.databind.json.JsonMapper

@Configuration
class RabbitMQConfig {

    companion object {
        const val EXCHANGE = "b2b.credits.events"
        const val QUEUE_TRANSACTION_STATUS_CHANGED = "queue.transaction.status.changed"
        const val ROUTING_KEY_TRANSACTION_STATUS_CHANGED = "transaction.status.changed"
    }

    @Bean
    fun exchange(): TopicExchange = TopicExchange(EXCHANGE)

    @Bean
    fun transactionStatusChangedQueue(): Queue = Queue(QUEUE_TRANSACTION_STATUS_CHANGED, true)

    @Bean
    fun transactionStatusChangedBinding(
        transactionStatusChangedQueue: Queue,
        exchange: TopicExchange,
    ): Binding = BindingBuilder.bind(transactionStatusChangedQueue).to(exchange).with(ROUTING_KEY_TRANSACTION_STATUS_CHANGED)

    @Bean
    fun messageConverter(jsonMapper: JsonMapper): MessageConverter = JacksonJsonMessageConverter(jsonMapper)
}

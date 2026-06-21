package br.com.vps.consulting.b2b.management.shared.infrastructure.event

import br.com.vps.consulting.b2b.management.shared.core.event.DomainEvent
import br.com.vps.consulting.b2b.management.shared.core.event.EventMessage
import br.com.vps.consulting.b2b.management.shared.core.event.EventPublisher
import br.com.vps.consulting.b2b.management.shared.infrastructure.config.RabbitMQConfig
import br.com.vps.consulting.b2b.management.transaction.domain.event.TransactionStatusChanged
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component
import kotlin.reflect.KClass

@Component
class RabbitMQEventPublisher(
    private val rabbitTemplate: RabbitTemplate,
) : EventPublisher {

    companion object {
        private val ROUTING_KEYS: Map<KClass<out DomainEvent>, String> = mapOf(
            TransactionStatusChanged::class to RabbitMQConfig.ROUTING_KEY_TRANSACTION_STATUS_CHANGED,
        )
    }

    override fun publish(event: DomainEvent) {
        val routingKey = ROUTING_KEYS[event::class]
            ?: throw IllegalArgumentException("Routing key não configurada para o evento: ${event::class.simpleName}")
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, routingKey, EventMessage(payload = event))
    }
}

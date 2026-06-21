package br.com.vps.consulting.b2b.management

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.rabbitmq.RabbitMQContainer
import org.testcontainers.utility.DockerImageName

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

	@Bean
	@ServiceConnection
	fun postgresContainer(): PostgreSQLContainer {
		return PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"))
	}

	@Bean
	@ServiceConnection
	fun rabbitMQContainer(): RabbitMQContainer {
		return RabbitMQContainer(DockerImageName.parse("rabbitmq:4-management"))
	}

}

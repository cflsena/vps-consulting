package br.com.vps.consulting.b2b.management.shared.infrastructure.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {

    @Bean
    fun openAPI(): OpenAPI = OpenAPI()
        .info(
            Info()
                .title("Gestão de Créditos para Parceiros B2B")
                .description("API para gestão de créditos de parceiros: consulta de saldo, crédito, débito e histórico de transações")
                .version("1.0.0")
        )
}

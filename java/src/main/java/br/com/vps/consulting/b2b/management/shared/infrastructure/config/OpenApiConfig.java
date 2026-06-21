package br.com.vps.consulting.b2b.management.shared.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Gestão B2B")
                        .description("API para gerenciamento de crédito e pedidos de parceiros B2B")
                        .version("1.0.0"))
                .addServersItem(new Server().url("/").description("Default server"));
    }
}

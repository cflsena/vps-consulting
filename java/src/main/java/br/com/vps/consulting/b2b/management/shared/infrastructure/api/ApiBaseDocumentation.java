package br.com.vps.consulting.b2b.management.shared.infrastructure.api;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@ApiResponses(value = {
        @ApiResponse(responseCode = "422", description = "Erro de validação de negócio"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
})
public interface ApiBaseDocumentation {
}

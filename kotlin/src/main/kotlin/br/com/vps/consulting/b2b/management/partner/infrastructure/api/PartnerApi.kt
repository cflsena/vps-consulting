package br.com.vps.consulting.b2b.management.partner.infrastructure.api

import br.com.vps.consulting.b2b.management.partner.application.usecase.find.FindPartnerBalanceOutput
import br.com.vps.consulting.b2b.management.partner.application.usecase.list.ListPartnersOutput
import br.com.vps.consulting.b2b.management.partner.infrastructure.api.request.CreatePartnerRequest
import br.com.vps.consulting.b2b.management.partner.infrastructure.api.response.PartnerCreatedResponse
import br.com.vps.consulting.b2b.management.shared.infrastructure.api.pagination.PageResponseDTO
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import java.util.UUID

@RequestMapping("/api/v1/b2b/partners")
@Tag(name = "Parceiros", description = "Operações de cadastro, consulta de saldo e listagem de parceiros B2B")
interface PartnerApi {

    @Operation(
        summary = "Cadastrar parceiro",
        description = "Cadastra um novo parceiro B2B no sistema, com saldo inicial zerado",
    )
    @ApiResponse(responseCode = "201", description = "Parceiro cadastrado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos")
    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun create(@RequestBody @Valid request: CreatePartnerRequest): ResponseEntity<PartnerCreatedResponse>

    @Operation(
        summary = "Consultar saldo do parceiro",
        description = "Retorna o saldo total (histórico de créditos) e o saldo disponível do parceiro",
    )
    @ApiResponse(responseCode = "200", description = "Saldo consultado com sucesso")
    @ApiResponse(responseCode = "404", description = "Parceiro não encontrado")
    @GetMapping("/{partnerId}/balance", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun findBalance(
        @Parameter(description = "ID único do parceiro") @PathVariable partnerId: UUID,
    ): ResponseEntity<FindPartnerBalanceOutput>

    @Operation(
        summary = "Listar parceiros",
        description = "Retorna uma lista paginada de parceiros, com filtro opcional por documento",
    )
    @ApiResponse(responseCode = "200", description = "Parceiros listados com sucesso")
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun list(
        @Parameter(description = "Filtro opcional por documento") @RequestParam(required = false) document: String?,
        @Parameter(description = "Quantidade de registros por página") @RequestParam(defaultValue = "20") pageSize: Int,
        @Parameter(description = "Número da página (base 0)") @RequestParam(defaultValue = "0") pageNumber: Int,
    ): ResponseEntity<PageResponseDTO<ListPartnersOutput>>
}

package br.com.vps.consulting.b2b.management.partner.infrastructure.api;

import br.com.vps.consulting.b2b.management.partner.application.usecase.list.PartnerListOutput;
import br.com.vps.consulting.b2b.management.shared.infrastructure.api.ApiBaseDocumentation;
import br.com.vps.consulting.b2b.management.shared.infrastructure.api.pagination.PageResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@RequestMapping("api/v1/b2b/partners")
@Tag(name = "Parceiro", description = "API para gerenciamento de parceiros B2B e seus créditos")
public interface PartnerApi extends ApiBaseDocumentation {

    record CreatePartnerRequest(
            @Schema(description = "Nome do parceiro")
            @NotBlank String name,

            @Schema(description = "Documento do parceiro (CPF ou CNPJ)")
            @NotBlank String document,

            @Schema(description = "Limite de crédito inicial do parceiro", example = "10000.00")
            @NotNull @Positive BigDecimal creditLimit
    ) {}

    record AdjustCreditLimitRequest(
            @Schema(description = "Novo limite de crédito a ser configurado", example = "15000.00")
            @NotNull @Positive BigDecimal newCreditLimit
    ) {}

    record ReplenishAvailableCreditRequest(
            @Schema(description = "Valor a ser reposto no saldo disponível do parceiro", example = "2000.00")
            @NotNull @Positive BigDecimal amount
    ) {}

    record PartnerCreatedResponse(
            @Schema(description = "ID único do parceiro criado")
            UUID id
    ) {}

    record PartnerCreditResponse(
            @Schema(description = "ID do parceiro")
            UUID partnerId,

            @Schema(description = "Limite de crédito total configurado para o parceiro")
            BigDecimal creditLimit,

            @Schema(description = "Saldo disponível para utilização em novos pedidos")
            BigDecimal availableBalance,

            @Schema(description = "Saldo reservado por pedidos com status PENDING")
            BigDecimal reservedBalance,

            @Schema(description = "Data e hora da última atualização do crédito (fuso horário Brasília, GMT-3)")
            OffsetDateTime updatedAt
    ) {}

    @Operation(
        summary = "Listar parceiros",
        description = "Retorna uma lista paginada de todos os parceiros B2B cadastrados no sistema."
    )
    @ApiResponse(responseCode = "200", description = "Parceiros listados com sucesso")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<PageResponseDTO<PartnerListOutput>> listPartners(
            @Parameter(description = "Número da página (base 0)", example = "0")
            @RequestParam(defaultValue = "0") int pageNumber,
            @Parameter(description = "Quantidade de registros por página", example = "20")
            @RequestParam(defaultValue = "20") int pageSize);

    @Operation(
        summary = "Criar parceiro",
        description = "Cadastra um novo parceiro B2B no sistema com seu limite de crédito inicial. O saldo disponível é iniciado com o mesmo valor do limite."
    )
    @ApiResponse(responseCode = "201", description = "Parceiro criado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<PartnerCreatedResponse> createPartner(@RequestBody @Valid CreatePartnerRequest request);

    @Operation(
        summary = "Ajustar limite de crédito",
        description = "Atualiza o limite de crédito de um parceiro. O novo limite não pode ser inferior ao valor já comprometido, que é a soma do valor debitado e do saldo reservado por pedidos pendentes."
    )
    @ApiResponse(responseCode = "204", description = "Limite de crédito ajustado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos")
    @ApiResponse(responseCode = "404", description = "Parceiro não encontrado")
    @ApiResponse(responseCode = "422", description = "Novo limite inferior ao valor comprometido (debitado + reservado)")
    @PatchMapping(value = "/{id}/credit-limit", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> adjustCreditLimit(
            @Parameter(description = "ID único do parceiro") @PathVariable UUID id,
            @RequestBody @Valid AdjustCreditLimitRequest request);

    @Operation(
        summary = "Repor saldo disponível",
        description = "Incrementa o saldo disponível do parceiro. O valor informado não pode exceder a diferença entre o limite de crédito e o saldo já disponível (crédito livre ainda não utilizado)."
    )
    @ApiResponse(responseCode = "204", description = "Saldo disponível reposto com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos")
    @ApiResponse(responseCode = "404", description = "Parceiro não encontrado")
    @ApiResponse(responseCode = "422", description = "Valor de reposição excede o máximo permitido")
    @PatchMapping(value = "/{id}/available-credit", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> replenishAvailableCredit(
            @Parameter(description = "ID único do parceiro") @PathVariable UUID id,
            @RequestBody @Valid ReplenishAvailableCreditRequest request);

    @Operation(
        summary = "Consultar crédito do parceiro",
        description = "Retorna os dados de crédito do parceiro: limite total configurado, saldo disponível para novos pedidos e saldo reservado por pedidos pendentes."
    )
    @ApiResponse(responseCode = "200", description = "Dados de crédito retornados com sucesso")
    @ApiResponse(responseCode = "404", description = "Parceiro não encontrado")
    @GetMapping(value = "/{id}/credit", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<PartnerCreditResponse> findPartnerCredit(
            @Parameter(description = "ID único do parceiro") @PathVariable UUID id);

}

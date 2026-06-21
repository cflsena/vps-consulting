package br.com.vps.consulting.b2b.management.partner.infrastructure.api

import br.com.vps.consulting.b2b.management.partner.application.usecase.create.CreatePartnerInput
import br.com.vps.consulting.b2b.management.partner.application.usecase.create.CreatePartnerUseCase
import br.com.vps.consulting.b2b.management.partner.application.usecase.find.FindPartnerBalanceInput
import br.com.vps.consulting.b2b.management.partner.application.usecase.find.FindPartnerBalanceOutput
import br.com.vps.consulting.b2b.management.partner.application.usecase.find.FindPartnerBalanceUseCase
import br.com.vps.consulting.b2b.management.partner.application.usecase.list.ListPartnersInput
import br.com.vps.consulting.b2b.management.partner.application.usecase.list.ListPartnersOutput
import br.com.vps.consulting.b2b.management.partner.application.usecase.list.ListPartnersUseCase
import br.com.vps.consulting.b2b.management.partner.infrastructure.api.request.CreatePartnerRequest
import br.com.vps.consulting.b2b.management.partner.infrastructure.api.response.PartnerCreatedResponse
import br.com.vps.consulting.b2b.management.shared.infrastructure.api.pagination.PageResponseDTO
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class PartnerController(
    private val createPartnerUseCase: CreatePartnerUseCase,
    private val findPartnerBalanceUseCase: FindPartnerBalanceUseCase,
    private val listPartnersUseCase: ListPartnersUseCase,
) : PartnerApi {

    override fun create(request: CreatePartnerRequest): ResponseEntity<PartnerCreatedResponse> {
        val id = createPartnerUseCase.execute(CreatePartnerInput(name = request.name, document = request.document))
        return ResponseEntity.status(HttpStatus.CREATED).body(PartnerCreatedResponse(id))
    }

    override fun findBalance(partnerId: UUID): ResponseEntity<FindPartnerBalanceOutput> {
        val output = findPartnerBalanceUseCase.execute(FindPartnerBalanceInput(partnerId))
        return ResponseEntity.ok(output)
    }

    override fun list(document: String?, pageSize: Int, pageNumber: Int): ResponseEntity<PageResponseDTO<ListPartnersOutput>> {
        val page = listPartnersUseCase.execute(ListPartnersInput(document, pageSize, pageNumber))
        return ResponseEntity.ok(PageResponseDTO.from(page))
    }
}

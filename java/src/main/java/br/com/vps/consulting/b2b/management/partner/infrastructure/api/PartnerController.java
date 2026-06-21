package br.com.vps.consulting.b2b.management.partner.infrastructure.api;

import br.com.vps.consulting.b2b.management.partner.application.usecase.adjust.AdjustCreditLimitInput;
import br.com.vps.consulting.b2b.management.partner.application.usecase.adjust.AdjustCreditLimitUseCase;
import br.com.vps.consulting.b2b.management.partner.application.usecase.create.CreatePartnerInput;
import br.com.vps.consulting.b2b.management.partner.application.usecase.create.CreatePartnerUseCase;
import br.com.vps.consulting.b2b.management.partner.application.usecase.find.FindPartnerCreditByIdUseCase;
import br.com.vps.consulting.b2b.management.partner.application.usecase.list.ListPartnersInput;
import br.com.vps.consulting.b2b.management.partner.application.usecase.list.ListPartnersUseCase;
import br.com.vps.consulting.b2b.management.partner.application.usecase.replenish.ReplenishAvailableCreditInput;
import br.com.vps.consulting.b2b.management.partner.application.usecase.replenish.ReplenishAvailableCreditUseCase;
import br.com.vps.consulting.b2b.management.partner.infrastructure.api.request.AdjustCreditLimitRequest;
import br.com.vps.consulting.b2b.management.partner.infrastructure.api.request.CreatePartnerRequest;
import br.com.vps.consulting.b2b.management.partner.infrastructure.api.request.ReplenishAvailableCreditRequest;
import br.com.vps.consulting.b2b.management.partner.infrastructure.api.response.PartnerCreatedResponse;
import br.com.vps.consulting.b2b.management.partner.infrastructure.api.response.PartnerCreditResponse;
import br.com.vps.consulting.b2b.management.partner.infrastructure.api.response.PartnerListResponse;
import br.com.vps.consulting.b2b.management.shared.core.utils.ManagementConstants;
import br.com.vps.consulting.b2b.management.shared.infrastructure.api.pagination.PageResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PartnerController implements PartnerApi {

    private final CreatePartnerUseCase createPartnerUseCase;
    private final AdjustCreditLimitUseCase adjustCreditLimitUseCase;
    private final ReplenishAvailableCreditUseCase replenishAvailableCreditUseCase;
    private final FindPartnerCreditByIdUseCase findPartnerCreditByIdUseCase;
    private final ListPartnersUseCase listPartnersUseCase;

    @Override
    public ResponseEntity<PartnerCreatedResponse> createPartner(final CreatePartnerRequest request) {
        final var id = createPartnerUseCase.execute(
                new CreatePartnerInput(request.name(), request.document(), request.creditLimit()));
        return ResponseEntity.status(HttpStatus.CREATED).body(new PartnerCreatedResponse(id));
    }

    @Override
    public ResponseEntity<Void> adjustCreditLimit(final UUID id, final AdjustCreditLimitRequest request) {
        adjustCreditLimitUseCase.execute(new AdjustCreditLimitInput(id, request.newCreditLimit()));
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> replenishAvailableCredit(final UUID id, final ReplenishAvailableCreditRequest request) {
        replenishAvailableCreditUseCase.execute(new ReplenishAvailableCreditInput(id, request.amount()));
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<PartnerCreditResponse> findPartnerCredit(final UUID id) {
        final var credit = findPartnerCreditByIdUseCase.execute(id);
        return ResponseEntity.ok(new PartnerCreditResponse(
                id,
                credit.getCreditLimit().value(),
                credit.getAvailableBalance().value(),
                credit.getReservedBalance().value(),
                credit.getUpdatedAt().atOffset(ManagementConstants.BRASILIA_TIME_ZONE)
        ));
    }

    @Override
    public ResponseEntity<PageResponseDTO<PartnerListResponse>> listPartners(final int pageNumber, final int pageSize) {
        final var page = listPartnersUseCase.execute(new ListPartnersInput(pageSize, pageNumber));
        return ResponseEntity.ok(PartnerListResponse.from(page));
    }

}

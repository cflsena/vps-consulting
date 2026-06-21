package br.com.vps.consulting.b2b.management.partner.domain.exception

import br.com.vps.consulting.b2b.management.partner.domain.PartnerId
import br.com.vps.consulting.b2b.management.shared.core.exception.NotFoundException

class PartnerNotFoundException(partnerId: PartnerId) :
    NotFoundException("Parceiro não encontrado: ${partnerId.value}")

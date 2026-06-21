package br.com.vps.consulting.b2b.management.partner.application.usecase.adjust;

import java.math.BigDecimal;
import java.util.UUID;

public record AdjustCreditLimitInput(UUID partnerId, BigDecimal newCreditLimit) {}

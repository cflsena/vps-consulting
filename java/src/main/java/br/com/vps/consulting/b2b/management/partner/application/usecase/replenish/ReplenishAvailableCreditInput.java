package br.com.vps.consulting.b2b.management.partner.application.usecase.replenish;

import java.math.BigDecimal;
import java.util.UUID;

public record ReplenishAvailableCreditInput(UUID partnerId, BigDecimal amount) {}

package br.com.vps.consulting.b2b.management.partner.application.usecase.create;

import java.math.BigDecimal;

public record CreatePartnerInput(String name, String document, BigDecimal creditLimit) {}

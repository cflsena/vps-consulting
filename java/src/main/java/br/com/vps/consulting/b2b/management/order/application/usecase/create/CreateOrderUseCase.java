package br.com.vps.consulting.b2b.management.order.application.usecase.create;

import java.util.UUID;

public interface CreateOrderUseCase {
    UUID execute(CreateOrderInput input);
}

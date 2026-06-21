package br.com.vps.consulting.b2b.management.order.application.usecase.find;

import java.util.UUID;

public interface FindOrderByIdUseCase {
    OrderOutput execute(UUID orderId);
}

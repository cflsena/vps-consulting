package br.com.vps.consulting.b2b.management.order.application.usecase.list.item;

import java.util.UUID;

public record ListOrderItemsInput(UUID orderId, long pageSize, long pageNumber) {}

package br.com.vps.consulting.b2b.management.order.infrastructure.mapper;

import br.com.vps.consulting.b2b.management.order.application.usecase.list.order.ListOrdersInput;
import br.com.vps.consulting.b2b.management.order.domain.OrderStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static br.com.vps.consulting.b2b.management.shared.core.utils.ManagementConstants.BRASILIA_TIME_ZONE;

public final class OrderRequestMapper {

    private OrderRequestMapper() {}

    public static Instant toStartOfDayInstant(final LocalDate date) {
        return date == null ? null : date.atStartOfDay().toInstant(BRASILIA_TIME_ZONE);
    }

    public static Instant toEndOfDayInstant(final LocalDate date) {
        return date == null ? null : date.atTime(23, 59, 59).toInstant(BRASILIA_TIME_ZONE);
    }

    public static ListOrdersInput toListInput(
            final LocalDate from, final LocalDate to,
            final OrderStatus status, final UUID partnerId,
            final int pageSize, final int pageNumber) {
        return new ListOrdersInput(
                toStartOfDayInstant(from),
                toEndOfDayInstant(to),
                status,
                partnerId,
                pageSize,
                pageNumber
        );
    }
}

package br.com.vps.consulting.b2b.management.order.application.usecase.list.order;

import br.com.vps.consulting.b2b.management.shared.core.page.PageCustom;

public interface ListOrdersUseCase {
    PageCustom<OrderListOutput> execute(ListOrdersInput input);
}

package br.com.vps.consulting.b2b.management.order.application.usecase.list.item;

import br.com.vps.consulting.b2b.management.shared.core.page.PageCustom;

public interface ListOrderItemsUseCase {
    PageCustom<OrderItemListOutput> execute(ListOrderItemsInput input);
}

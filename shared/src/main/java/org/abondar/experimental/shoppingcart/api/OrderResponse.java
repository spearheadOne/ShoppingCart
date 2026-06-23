package org.abondar.experimental.shoppingcart.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        String orderId,
        String cartId,
        String status,
        List<OrderItemResponse> items,
        int itemsTotal,
        BigDecimal totalPrice,
        Instant createdAt
) {
}
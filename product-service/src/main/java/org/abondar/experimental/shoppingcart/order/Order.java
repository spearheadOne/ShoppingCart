package org.abondar.experimental.shoppingcart.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record Order(
        UUID id,
        UUID cartId,
        OrderStatus status,
        BigDecimal totalPrice,
        Instant createdAt,
        List<OrderItem> items
) {
}

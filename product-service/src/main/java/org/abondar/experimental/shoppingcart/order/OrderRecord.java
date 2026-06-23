package org.abondar.experimental.shoppingcart.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderRecord(
        UUID id,
        UUID cartId,
        OrderStatus status,
        BigDecimal totalPrice,
        Instant createdAt
) {
}

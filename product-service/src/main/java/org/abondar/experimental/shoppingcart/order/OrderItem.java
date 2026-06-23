package org.abondar.experimental.shoppingcart.order;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItem(
        UUID orderId,
        UUID productId,
        String productName,
        String productImgUrl,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal lineTotal
) {
}

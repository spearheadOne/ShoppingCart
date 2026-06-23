package org.abondar.experimental.shoppingcart.api;

import java.math.BigDecimal;

public record OrderItemResponse(
        String productId,
        String name,
        String imgUrl,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal lineTotal
) {
}

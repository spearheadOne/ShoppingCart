package org.abondar.experimental.shoppingcart.cart;

import java.math.BigDecimal;

public record CartItemResponse(
        String productId,
        String name,
        String imgUrl,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal lineTotal
) {
}
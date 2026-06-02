package org.abondar.experimental.shoppingcart.cart;

import java.math.BigDecimal;

public record CartItem(
        String id,
        String name,
        BigDecimal price,
        int quantity,
        BigDecimal totalPrice
) {
}

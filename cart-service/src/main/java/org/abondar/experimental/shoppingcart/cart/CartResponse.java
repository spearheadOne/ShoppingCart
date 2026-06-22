package org.abondar.experimental.shoppingcart.cart;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(
        String cartId,
        List<CartItemResponse> items,
        long itemsTotal,
        BigDecimal totalPrice
) {
}

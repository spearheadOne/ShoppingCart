package org.abondar.experimental.shoppingcart.cart;

import java.math.BigDecimal;
import java.util.UUID;

public record CartItem(
        UUID productId,
        String name,
        String imgUrl,
        BigDecimal unitPrice,
        int quantity
) {
    public BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}

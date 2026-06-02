package org.abondar.experimental.shoppingcart.product;

import java.math.BigDecimal;

public record ProductResponse(
        String id,
        String name,
        String imgUrl,
        BigDecimal price
) {
}

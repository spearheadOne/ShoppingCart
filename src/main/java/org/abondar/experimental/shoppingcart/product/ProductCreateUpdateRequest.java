package org.abondar.experimental.shoppingcart.product;

import java.math.BigDecimal;

public record ProductCreateUpdateRequest(
        String name,
        String imgUrl,
        BigDecimal price
) {
}

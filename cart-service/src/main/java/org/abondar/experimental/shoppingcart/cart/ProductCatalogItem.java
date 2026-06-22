package org.abondar.experimental.shoppingcart.cart;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductCatalogItem(
        UUID id,
        String name,
        String imgUrl,
        BigDecimal price
) {
}

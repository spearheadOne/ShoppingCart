package org.abondar.experimental.shoppingcart.api;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String name,
        String imgUrl,
        BigDecimal price
) {
}

package org.abondar.experimental.cameldemo.shoppingcart.model;

import java.math.BigDecimal;

public record CartProductGetResponse(
        String name,
        String imgUrl,
        BigDecimal price
) {
}

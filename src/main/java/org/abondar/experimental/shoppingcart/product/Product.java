package org.abondar.experimental.shoppingcart.product;

import java.math.BigDecimal;
import java.util.UUID;


public record Product(
       UUID id,
       String name,
       String imgUrl,
       BigDecimal price
) {
}



package org.abondar.experimental.shoppingcart.model;

import java.util.List;

public record CartProductRequest(
        List<CartProduct> products
) {
}

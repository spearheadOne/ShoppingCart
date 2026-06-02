package org.abondar.experimental.cameldemo.shoppingcart.model;

import java.util.List;

public record CartProductRequest(
        List<CartProduct> products
) {
}

package org.abondar.experimental.shoppingcart.cart;

import java.util.UUID;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(UUID productId) {
        super("Product with id %s not found".formatted(productId));
    }
}

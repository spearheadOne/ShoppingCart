package org.abondar.experimental.shoppingcart.product;

import java.util.UUID;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(UUID id) {
        super(String.format("Product with id %s not found", id.toString()));
    }
}

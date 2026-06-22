package org.abondar.experimental.shoppingcart.cart;

import java.util.UUID;

public class CartNotFoundException extends RuntimeException {

    public CartNotFoundException() {
        super("Cart not found");
    }

    public CartNotFoundException(UUID id) {
        super(String.format("Cart found with id %s" , id));
    }
}

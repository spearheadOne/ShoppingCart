package org.abondar.experimental.shoppingcart.cart;

import java.util.UUID;

public class CartItemNotFoundException extends RuntimeException {
    public CartItemNotFoundException(UUID productId) {
        super(String.format("Cart item with productId %s not found", productId));
    }
}

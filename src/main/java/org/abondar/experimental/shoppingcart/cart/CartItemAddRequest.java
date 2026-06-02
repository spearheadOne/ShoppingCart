package org.abondar.experimental.shoppingcart.cart;

public record CartItemAddRequest(
        String productId,
        int quantity
) {
}

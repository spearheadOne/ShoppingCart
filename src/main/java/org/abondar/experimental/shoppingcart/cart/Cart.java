package org.abondar.experimental.shoppingcart.cart;

import java.util.List;
import java.util.UUID;

public record Cart(
        UUID id,
        List<CartItem> items
) {
}

package org.abondar.experimental.shoppingcart.model;

import java.util.List;

public record CartItems(
        boolean changed,
        long itemsTotal,
        boolean showCart,
        List<CartItem> items
) {
}

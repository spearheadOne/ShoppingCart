package org.abondar.experimental.cameldemo.shoppingcart.model;

import java.util.List;

public record CartItems(
        boolean changed,
        long itemsTotal,
        boolean showCart,
        List<CartItem> items
) {
}

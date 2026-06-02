package org.abondar.experimental.shoppingcart.cart;

import jakarta.validation.constraints.Min;

public record CartItemUpdateQuantityRequest(
        @Min(1) int quantity
) {
}

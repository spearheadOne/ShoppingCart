package org.abondar.experimental.shoppingcart.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CartItemAddRequest(
        @NotBlank String productId,
        @Min(1) int quantity
) {
}
package org.abondar.experimental.shoppingcart.api;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateOrderItemRequest(
        @NotBlank
        String productId,

        @Min(1)
        int quantity
) {
}

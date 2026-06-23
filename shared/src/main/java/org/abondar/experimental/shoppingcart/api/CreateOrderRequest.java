package org.abondar.experimental.shoppingcart.api;

import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record CreateOrderRequest(
        @NotBlank
        String cartId,

        @NotEmpty
        List<CreateOrderItemRequest> items
) {

}
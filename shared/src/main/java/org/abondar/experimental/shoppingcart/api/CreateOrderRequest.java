package org.abondar.experimental.shoppingcart.api;

import jakarta.validation.Valid;

import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record CreateOrderRequest(
        @NotBlank
        String cartId,

        @Valid
        @NotEmpty
        List<CreateOrderItemRequest> items
) {

}

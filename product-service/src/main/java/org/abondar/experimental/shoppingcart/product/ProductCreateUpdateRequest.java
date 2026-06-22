package org.abondar.experimental.shoppingcart.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ProductCreateUpdateRequest(
        @NotBlank String name,
        @NotBlank String imgUrl,
        @NotNull @DecimalMin("0.0")BigDecimal price
) {
}

package org.abondar.experimental.shoppingcart.product;

import java.util.List;

public record ProductListResponse(
        List<ProductResponse> products,
        int limit,
        int offset,
        int total
) {
}

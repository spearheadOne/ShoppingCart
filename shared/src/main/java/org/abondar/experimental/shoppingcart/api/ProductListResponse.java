package org.abondar.experimental.shoppingcart.api;

import java.util.List;

public record ProductListResponse(
        List<ProductResponse> products,
        int limit,
        int offset,
        long total
) {
}

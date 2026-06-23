package org.abondar.experimental.shoppingcart.product;

import org.abondar.experimental.shoppingcart.api.ProductListResponse;
import org.abondar.experimental.shoppingcart.api.ProductResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductResponseMapper {

    public ProductListResponse toListResponse(List<Product> products, int limit, int offset, long total) {
        var responses = products.stream()
                .map(this::toResponse)
                .toList();

        return new ProductListResponse(responses, limit, offset, total);
    }

    public ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.id(),
                product.name(),
                product.imgUrl(),
                product.price()
        );
    }
}

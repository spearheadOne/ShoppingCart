package org.abondar.experimental.shoppingcart.product;

import lombok.RequiredArgsConstructor;
import org.abondar.experimental.shoppingcart.util.UuidUtil;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductMapper productMapper;

    public ProductResponse getProductById(String id) {
        var productId = UuidUtil.parseUuid(id);

        var product = productMapper.findProductById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        return toResponse(product);
    }


    public ProductListResponse getProductList(int limit, int offset) {
        var products = productMapper.findAll(limit, offset)
                .stream()
                .map(this::toResponse)
                .toList();

        var count = productMapper.count();

        return new ProductListResponse(products, limit, offset, count);

    }




    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.id().toString(),
                product.name(),
                product.imgUrl(),
                product.price()
        );
    }

}

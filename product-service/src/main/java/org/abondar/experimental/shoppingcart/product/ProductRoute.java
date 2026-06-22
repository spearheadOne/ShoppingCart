package org.abondar.experimental.shoppingcart.product;

import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductRoute extends RouteBuilder {


    private final ProductService productService;


    private static final int DEFAULT_LIMIT = 10;
    private static final int DEFAULT_OFFSET = 0;
    private static final int MAX_LIMIT = 100;

    @Override
    public void configure() throws Exception {
        from("direct:getProductById")
                .routeId("getProductById")
                .bean(productService, "getProductById(${header.id})");


        from("direct:getProducts")
                .routeId("getProducts")
                .process(exchange -> {
                    String limitHeader = exchange.getIn().getHeader("limit", String.class);
                    String offsetHeader = exchange.getIn().getHeader("offset", String.class);

                    try {
                        int limit = limitHeader != null ? Integer.parseInt(limitHeader.trim()) : DEFAULT_LIMIT;
                        int offset = offsetHeader != null ? Integer.parseInt(offsetHeader.trim()) : DEFAULT_OFFSET;

                        if (limit < 1 || limit > MAX_LIMIT || offset < 0) {
                            throw new ProductPaginationException();
                        }

                        exchange.getIn().setHeader("limit", limit);
                        exchange.getIn().setHeader("offset", offset);
                    } catch (NumberFormatException e) {
                        throw new ProductPaginationException();
                    }
                })
                .bean(productService, "getProductList(${header.limit}, ${header.offset})");
    }
}

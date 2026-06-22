package org.abondar.experimental.shoppingcart.product;

import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProductRoute extends RouteBuilder {

    private static final int DEFAULT_LIMIT = 10;
    private static final int DEFAULT_OFFSET = 0;
    private static final int MAX_LIMIT = 100;

    static final String PRODUCT_MAPPER =
            "org.abondar.experimental.shoppingcart.product.ProductMapper";

    private static final String FIND_PRODUCT_BY_ID = "mybatis:" + PRODUCT_MAPPER
            + ".findProductById"
            + "?statementType=SelectOne";

    private static final String FIND_PRODUCTS = "mybatis:" + PRODUCT_MAPPER
            + ".findAll"
            + "?statementType=SelectList";

    private static final String COUNT_PRODUCTS = "mybatis:" + PRODUCT_MAPPER
            + ".count"
            + "?statementType=SelectOne";

    private final ProductResponseMapper productResponseMapper;

    @Override
    public void configure() throws Exception {
        from("direct:getProductById")
                .routeId("getProductById")
                .process(exchange -> {
                    var id = exchange.getMessage().getHeader("id", String.class);
                    var productId = UUID.fromString(id);

                    exchange.setProperty("productId", productId);

                    //for mybatis
                    exchange.getMessage().setBody(Map.of("id", productId));
                })
                .to(FIND_PRODUCT_BY_ID)
                .id("findProductByIdQuery")
                .choice()
                .when(body().isNull())
                .process(exchange -> {
                    var productId = exchange.getProperty("productId", UUID.class);
                    throw new ProductNotFoundException(productId);
                })
                .end()
                .bean(productResponseMapper, "toResponse");


        from("direct:getProducts")
                .routeId("getProducts")
                .process(exchange -> {
                    var message = exchange.getMessage();
                    String limitHeader = message.getHeader("limit", String.class);
                    String offsetHeader = message.getHeader("offset", String.class);

                    try {
                        int limit = limitHeader != null ? Integer.parseInt(limitHeader.trim()) : DEFAULT_LIMIT;
                        int offset = offsetHeader != null ? Integer.parseInt(offsetHeader.trim()) : DEFAULT_OFFSET;

                        if (limit < 1 || limit > MAX_LIMIT || offset < 0) {
                            throw new ProductPaginationException();
                        }

                        message.setHeader("limit", limit);
                        message.setHeader("offset", offset);

                        //for mybatis
                        message.setBody(Map.of(
                                "limit", limit,
                                "offset", offset
                        ));
                    } catch (NumberFormatException e) {
                        throw new ProductPaginationException();
                    }
                })
                .to(FIND_PRODUCTS)
                .id("findProductsQuery")
                .setProperty("products", body())
                .setBody(constant((Object) null))
                .to(COUNT_PRODUCTS)
                .id("countProductsQuery")
                .bean(productResponseMapper, "toListResponse("
                        + "${exchangeProperty.products}, "
                        + "${header.limit}, "
                        + "${header.offset}, "
                        + "${body})");
    }


}

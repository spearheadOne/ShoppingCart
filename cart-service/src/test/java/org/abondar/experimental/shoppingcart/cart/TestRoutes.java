package org.abondar.experimental.shoppingcart.cart;

import lombok.RequiredArgsConstructor;
import org.abondar.experimental.shoppingcart.api.ProductResponse;
import org.abondar.experimental.shoppingcart.product.ProductClientException;
import org.apache.camel.builder.RouteBuilder;

import java.math.BigDecimal;
import java.util.UUID;

@RequiredArgsConstructor
public class TestRoutes extends RouteBuilder {

    private static final UUID PRODUCT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private final TestCartStore cartStore;

    @Override
    public void configure() throws Exception {
        from("direct:loadCartFromRedis")
                .routeId("testLoadCartFromRedis")
                .process(exchange -> {
                    var cart = cartStore.getCart();

                    if (cart == null) {
                        throw new CartNotFoundException();
                    }

                    exchange.getMessage().setBody(cart);
                });

        from("direct:saveCartToRedis")
                .routeId("testSaveCartToRedis")
                .process(exchange -> {
                    var cart = exchange.getMessage().getBody(Cart.class);
                    cartStore.setCart(cart);

                    exchange.getMessage().setBody(cart);
                });

        from("direct:deleteCartFromRedis")
                .routeId("testDeleteCartFromRedis")
                .process(exchange -> {

                    if (cartStore.getCart() == null) {
                        throw new CartNotFoundException();
                    }

                    cartStore.clear();
                });

        from("direct:getProduct")
                .routeId("testGetProduct")
                .process(exchange -> {
                    var requestedProductId = exchange.getProperty("productId", String.class);

                    if (!PRODUCT_ID.toString().equals(requestedProductId)) {
                        throw new ProductClientException(
                                404,
                                "test",
                                "Product with id %s not found".formatted(requestedProductId));
                    }

                    exchange.getMessage().setBody(
                            new ProductResponse(
                                    PRODUCT_ID,
                                    "Test product",
                                    "https://example.com/product.jpg",
                                    new BigDecimal("10.00")
                            ));
                });

        from("direct:createOrder")
                .routeId("testCreateOrder")
                .to("mock:createOrder");
    }
}

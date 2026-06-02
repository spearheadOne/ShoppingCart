package org.abondar.experimental.shoppingcart.cart;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class CartRestRoute extends RouteBuilder {
    @Override
    public void configure() throws Exception {

        rest()
                .path("/v1/carts")
                .consumes("application/json")
                .produces("application/json")

                .post()
                .apiDocs(true)
                .outType(CartResponse.class)
                .to("direct:createCart")

                .get("/{id}")
                .apiDocs(true)
                .outType(CartResponse.class)
                .to("direct:getCart")

                .post("/{id}/items")
                .apiDocs(true)
                .type(CartItemAddRequest.class)
                .outType(CartResponse.class)
                .to("direct:addCartItem")

                .patch("/{id}/items/{productId}")
                .apiDocs(true)
                .type(CartItemUpdateQuantityRequest.class)
                .outType(CartResponse.class)
                .to("direct:updateCartItem")

                .delete("/{id}/items/{productId}")
                .apiDocs(true)
                .outType(CartResponse.class)
                .to("direct:deleteCartItem")

                .delete("/{id}")
                .apiDocs(true)
                .to("direct:deleteCart");

    }
}

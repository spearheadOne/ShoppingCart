package org.abondar.experimental.shoppingcart.cart;


import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CartRoute extends RouteBuilder {

    private final CartService cartService;

    @Override
    public void configure() {

        from("direct:createCart")
                .routeId("createCart")
                .bean(cartService, "createCart()")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(201));

        from("direct:getCart")
                .routeId("getCart")
                .bean(cartService, "getCart(${header.id})");

        from("direct:addCartItem")
                .routeId("addCartItem")
                .to("bean-validator:addCartItemRequest")
                .bean(
                        cartService,
                        "addCartItem(${header.id}, ${body})"
                );

        from("direct:updateCartQuantity")
                .routeId("updateCartQuantity")
                .to("bean-validator:updateCartItemQuantityRequest")
                .bean(
                        cartService,
                        "updateCartQuantity(${header.id}, ${header.productId}, ${body})"
                );

        from("direct:deleteCartItem")
                .routeId("deleteCartItem")
                .bean(
                        cartService,
                        "deleteCartItem(${header.id}, ${header.productId})"
                );

        from("direct:deleteCart")
                .routeId("deleteCart")
                .bean(cartService, "deleteCart(${header.id})")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(204))
                .setBody(constant((Object) null));
    }
}
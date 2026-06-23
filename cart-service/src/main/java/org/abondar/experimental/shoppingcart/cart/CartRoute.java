package org.abondar.experimental.shoppingcart.cart;


import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CartRoute extends RouteBuilder {

    private final CartService cartService;
    private final CartDtoMapper cartDtoMapper;

    @Override
    public void configure() {

        from("direct:createCart")
                .routeId("createCart")
                .bean(cartService, "createCart()")
                .to("direct:saveCartToRedis")
                .bean(cartDtoMapper, "toResponse")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(201));

        from("direct:getCart")
                .routeId("getCart")
                .to("direct:loadCartFromRedis")
                .bean(cartDtoMapper, "toResponse");

        from("direct:addCartItem")
                .routeId("addCartItem")
                .to("bean-validator:addCartItemRequest")
                .setProperty("addRequest", body())
                .setProperty(
                        "productId",
                        simple("${body.productId}")
                )
                .to("direct:loadCartFromRedis")
                .setProperty("cart", body())
                .to("direct:getProduct")
                .bean(
                        cartService,
                        "addCartItem("
                                + "${exchangeProperty.cart}, "
                                + "${exchangeProperty.addRequest}, "
                                + "${body})"
                )
                .to("direct:saveCartToRedis")
                .bean(cartDtoMapper, "toResponse");

        from("direct:updateCartQuantity")
                .routeId("updateCartQuantity")
                .to("bean-validator:updateCartItemQuantityRequest")
                .setProperty("updateRequest", body())
                .to("direct:loadCartFromRedis")
                .bean(
                        cartService,
                        "updateCartQuantity("
                                + "${body}, "
                                + "${header.productId}, "
                                + "${exchangeProperty.updateRequest})"
                )
                .to("direct:saveCartToRedis")
                .bean(cartDtoMapper, "toResponse");

        from("direct:deleteCartItem")
                .routeId("deleteCartItem")
                .to("direct:loadCartFromRedis")
                .bean(
                        cartService,
                        "deleteCartItem(${body}, ${header.productId})"
                )
                .to("direct:saveCartToRedis")
                .bean(cartDtoMapper, "toResponse");

        from("direct:deleteCart")
                .routeId("deleteCart")
                .to("direct:deleteCartFromRedis")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(204))
                .setBody(constant((Object) null));

        from("direct:submitCart")
                .routeId("submitCart")
                .to("direct:loadCartFromRedis")
                .setProperty("submittedCart", body())
                .bean(cartDtoMapper,"toRequest")
                .to("bean-validator:createOrderRequest")
                .to("direct:createOrder")
                .setProperty("createdOrderResponse", body())
                .setHeader("id", simple("${exchangeProperty.submittedCart.id}"))
                .to("direct:deleteCartFromRedis")
                .setBody(exchangeProperty("createdOrderResponse"))
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(201));

    }
}
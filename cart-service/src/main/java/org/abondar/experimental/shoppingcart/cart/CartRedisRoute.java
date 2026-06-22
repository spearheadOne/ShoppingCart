package org.abondar.experimental.shoppingcart.cart;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.redis.RedisConstants;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;

@Component
public class CartRedisRoute extends RouteBuilder {

    private static final String REDIS =
            "spring-redis://{{redis.host}}:{{redis.port}}";

    private static final long CART_TTL_SECONDS = 86_400L;

    @Override
    public void configure() throws Exception {
        from("direct:loadCartFromRedis")
                .routeId("loadCartFromRedis")
                .setHeader(RedisConstants.KEY, simple("cart:${header.id}"))
                .to(REDIS + "?command=GET")
                .choice()
                .when(body().isNull())
                .throwException(new CartNotFoundException())
                .end()
                .convertBodyTo(String.class)
                .unmarshal()
                .json(JsonLibrary.Jackson, Cart.class);

        from("direct:saveCartToRedis")
                .routeId("saveCartToRedis")
                .setProperty("savedCart", body())
                .setHeader(RedisConstants.KEY, simple("cart:${body.id}"))
                .marshal().json(JsonLibrary.Jackson)
                .convertBodyTo(String.class)
                .setHeader(RedisConstants.VALUE, body())
                .setHeader(RedisConstants.TIMEOUT, constant(CART_TTL_SECONDS))
                .to(REDIS + "?command=SETEX")
                .setBody(exchangeProperty("savedCart"));

        from("direct:deleteCartFromRedis")
                .routeId("deleteCartFromRedis")
                .setHeader(RedisConstants.KEY, simple("cart:${header.id}"))
                .to(REDIS + "?command=EXISTS")
                .choice()
                .when(body().isEqualTo(false))
                .throwException(new CartNotFoundException())
                .end()
                .setHeader(RedisConstants.KEYS, simple("cart:${header.id}"))
                .to(REDIS + "?command=DEL");

    }
}

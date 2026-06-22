package org.abondar.experimental.shoppingcart.cart;


import com.redis.testcontainers.RedisContainer;
import org.abondar.experimental.shoppingcart.exception.ErrorResponse;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.spring.boot.CamelAutoConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;


@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(classes = CartRedisRouteTest.TestApplication.class)
public class CartRedisRouteTest {

    @Container
    static RedisContainer redis = new RedisContainer("redis:7-alpine");

    @Autowired
    private ProducerTemplate producerTemplate;

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("redis.host", redis::getHost);
        registry.add("redis.port", () -> redis.getMappedPort(6379));
    }

    @BeforeEach
    public void setUp() throws Exception {
        redis.execInContainer("redis-cli", "FLUSHDB");
    }


    @Test
    public void saveAndLoadCart() {
        var cartId = UUID.randomUUID();

        var item = new CartItem(UUID.randomUUID(), "test", "test", BigDecimal.ONE, 2);
        var cart = new Cart(cartId, List.of(item));

        var saved = producerTemplate.requestBody("direct:saveCartToRedis", cart, Cart.class);

        var loaded = producerTemplate.
                requestBodyAndHeader("direct:loadCartFromRedis", null, "id", cartId, Cart.class);

        assertEquals(cart, saved);
        assertEquals(cartId, loaded.id());
        assertEquals(1, loaded.items().size());
        assertEquals(item, loaded.items().getFirst());
    }


    @Test
    public void loadNotFound() {
        var cartId = UUID.randomUUID();

        var exchange = producerTemplate.request(
                "direct:loadCartFromRedis",
                current -> current.getMessage().setHeader("id", cartId)
        );
        assertInstanceOf(ErrorResponse.class, exchange.getMessage().getBody());

        var caught = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
        assertInstanceOf(CartNotFoundException.class, caught);
    }

    @Test
    public void saveAndOverwriteCart() {
        var cartId = UUID.randomUUID();

        var item = new CartItem(UUID.randomUUID(), "test", "test", BigDecimal.ONE, 2);
        var cart = new Cart(cartId, List.of(item));

        producerTemplate.sendBody("direct:saveCartToRedis", cart);


        var item1 = new CartItem(UUID.randomUUID(), "test1", "test", BigDecimal.ONE, 6);
        var cart1 = new Cart(cartId, List.of(item, item1));
        producerTemplate.sendBody("direct:saveCartToRedis", cart1);

        var result = producerTemplate.requestBodyAndHeader("direct:loadCartFromRedis",
                null, "id", cartId, Cart.class);

        assertEquals(cartId, result.id());
        assertEquals(2, result.items().size());
        assertEquals(cart1, result);
    }

    @Test
    void deleteCart() {
        var cartId = UUID.randomUUID();
        var cart = new Cart(cartId, List.of());

        producerTemplate.sendBody("direct:saveCartToRedis", cart);

        var deleteExchange = producerTemplate.request("direct:deleteCartFromRedis",
                exchange -> exchange.getMessage().setHeader("id", cartId));
        assertFalse(deleteExchange.isFailed());

        var loadExchange = producerTemplate.request("direct:loadCartFromRedis",
                exchange -> exchange.getMessage().setHeader("id", cartId));
        assertInstanceOf(ErrorResponse.class, loadExchange.getMessage().getBody());

        var caught = loadExchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
        assertInstanceOf(CartNotFoundException.class, caught);

    }

    @Test
    public void deleteCartNotFound() {
        var cartId = UUID.randomUUID();
        var exchange = producerTemplate.request(
                "direct:deleteCartFromRedis",
                current -> current.getMessage().setHeader("id", cartId)
        );

        var caught = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
        assertInstanceOf(CartNotFoundException.class, caught);

    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import({
            CartRedisRoute.class,
            CartExceptionHandler.class
    })
    @ImportAutoConfiguration(CamelAutoConfiguration.class)
    static class TestApplication {
    }

}

package org.abondar.experimental.shoppingcart.cart;

import org.abondar.experimental.shoppingcart.api.CreateOrderRequest;
import org.abondar.experimental.shoppingcart.api.OrderItemResponse;
import org.abondar.experimental.shoppingcart.api.OrderResponse;
import org.abondar.experimental.shoppingcart.product.ProductClientException;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.bean.validator.BeanValidationException;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.spring.boot.CamelAutoConfiguration;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;


@CamelSpringBootTest
@SpringBootTest(
        classes = CartRouteTest.TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
public class CartRouteTest {

    private static final UUID CART_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID PRODUCT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");


    @Autowired
    private ProducerTemplate producerTemplate;
    @Autowired
    private TestCartStore cartStore;

    @EndpointInject("mock:createOrder")
    private MockEndpoint createOrder;

    private static Stream<CartItemAddRequest> invalidAddRequests() {
        return Stream.of(
                new CartItemAddRequest(null, 1),
                new CartItemAddRequest("", 1),
                new CartItemAddRequest("   ", 1),
                new CartItemAddRequest(PRODUCT_ID.toString(), 0),
                new CartItemAddRequest(PRODUCT_ID.toString(), -1)
        );
    }

    @BeforeEach
    public void clearStore() {
        cartStore.clear();
        createOrder.reset();
    }


    @Test
    public void createCart() {
        var exchange = producerTemplate.request("direct:createCart", ignored -> {
        });

        assertEquals(201, exchange.getIn().getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class));
        assertInstanceOf(CartResponse.class, exchange.getIn().getBody());
    }

    @Test
    public void getCart() {
        var cart = new Cart(CART_ID, List.of());
        cartStore.setCart(cart);

        var response = producerTemplate
                .requestBodyAndHeader("direct:getCart", null, "id", CART_ID, CartResponse.class);

        assertNotNull(response);
        assertEquals(CART_ID.toString(), response.cartId());
    }

    @Test
    public void addCartItem() {
        cartStore.setCart(new Cart(CART_ID, List.of()));
        var request = new CartItemAddRequest(PRODUCT_ID.toString(), 2);

        var response = producerTemplate
                .requestBodyAndHeader("direct:addCartItem", request, "id", CART_ID, CartResponse.class);

        assertEquals(1, response.items().size());
        assertEquals(2, response.items().getFirst().quantity());
    }

    @Test
    public void addCartItemIncreasesExistingQuantity() {
        cartStore.setCart(new Cart(CART_ID, List.of(cartItem(2))));

        var request = new CartItemAddRequest(PRODUCT_ID.toString(), 2);

        var response = producerTemplate
                .requestBodyAndHeader("direct:addCartItem", request, "id", CART_ID, CartResponse.class);

        assertEquals(1, response.items().size());
        assertEquals(4, response.items().getFirst().quantity());
        assertEquals(4, cartStore.getCart().items().getFirst().quantity());
    }

    @ParameterizedTest
    @MethodSource("invalidAddRequests")
    public void addCartItemInvalidRequest(CartItemAddRequest request) {
        var exception = assertThrows(CamelExecutionException.class, () ->
                producerTemplate.requestBodyAndHeader("direct:addCartItem", request,
                        "id", CART_ID, CartResponse.class));

        assertEquals(BeanValidationException.class, exception.getCause().getClass());
    }

    @Test
    public void updateCartQuantity() {
        var request = new CartItemUpdateQuantityRequest(2);
        cartStore.setCart(new Cart(CART_ID, List.of(cartItem(1))));

        var exchange = producerTemplate.request(
                "direct:updateCartQuantity",
                e -> {
                    e.getMessage().setBody(request);
                    e.getMessage().setHeader("id", CART_ID);
                    e.getMessage().setHeader("productId", PRODUCT_ID.toString());
                }
        );

        var response = exchange.getMessage().getBody(CartResponse.class);

        assertNotNull(response);
        assertEquals(1, response.items().size());
        assertEquals(2, response.items().getFirst().quantity());
        assertEquals(2, cartStore.getCart().items().getFirst().quantity());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -10})
    public void updateCartQuantityInvalidQuantity(int quantity) {
        var request = new CartItemUpdateQuantityRequest(quantity);

        var exception = assertThrows(CamelExecutionException.class, () ->
                producerTemplate.requestBodyAndHeaders("direct:updateCartQuantity", request,
                        Map.of(
                                "id", CART_ID,
                                "productId", PRODUCT_ID
                        ), CartResponse.class));

        assertEquals(BeanValidationException.class, exception.getCause().getClass());
    }

    @Test
    void deleteCartItem() {
        var item = cartItem(2);
        var productId1 = UUID.fromString("22222-2222-2222-2222-222222222222");
        var item1 = new CartItem(productId1, "test", "test", BigDecimal.ONE, 1);

        cartStore.setCart(new Cart(CART_ID, List.of(item, item1)));

        var exchange = producerTemplate.request(
                "direct:deleteCartItem",
                e -> {
                    e.getMessage().setHeader("id", CART_ID);
                    e.getMessage().setHeader(
                            "productId",
                            PRODUCT_ID.toString()
                    );
                }
        );

        var response = exchange.getMessage().getBody(CartResponse.class);

        assertNotNull(response);
        assertEquals(1, response.items().size());
        assertEquals(productId1.toString(), response.items().getFirst().productId());
        assertEquals(1, cartStore.getCart().items().size());
        assertEquals(productId1, cartStore.getCart().items().getFirst().productId());
    }

    @Test
    public void deleteCart() {
        cartStore.setCart(new Cart(CART_ID, List.of()));

        var exchange = producerTemplate.request("direct:deleteCart",
                current -> current.getMessage().setHeader("id", CART_ID)
        );

        assertEquals(204, exchange.getMessage().getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class));
        assertNull(exchange.getMessage().getBody());
        assertNull(cartStore.getCart());
    }


    @Test
    public void submitCart() throws Exception {
        var cart = new Cart(CART_ID, List.of(cartItem(2)));
        cartStore.setCart(cart);

        var orderItemResponse = new OrderItemResponse(PRODUCT_ID.toString(), "test", "test",
                new BigDecimal("10.00"), 2, new BigDecimal("20.00"));

        var orderResponse = new OrderResponse("33333333-3333-3333-3333-333333333333", CART_ID.toString(),
                "CREATED", List.of(orderItemResponse), 1, new BigDecimal("20.00"), Instant.now());

        createOrder.expectedMessageCount(1);
        createOrder.expectedMessagesMatches(exchange -> {
            var request = exchange.getMessage().getBody(CreateOrderRequest.class);

            return request != null
                    && CART_ID.toString().equals(request.cartId())
                    && request.items().size() == 1
                    && PRODUCT_ID.toString().equals(request.items().getFirst().productId())
                    && request.items().getFirst().quantity() == 2;
        });
        createOrder.whenAnyExchangeReceived(exchange -> exchange.getIn().setBody(orderResponse));

        var exchange = producerTemplate.request("direct:submitCart",
                current -> current.getMessage().setHeader("id", CART_ID)
        );

        var response = exchange.getMessage().getBody(OrderResponse.class);

        assertNotNull(response);
        assertEquals(201, exchange.getMessage().getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class));
        assertEquals("33333333-3333-3333-3333-333333333333", response.orderId());
        assertEquals("CREATED", response.status());
        assertEquals(1, response.itemsTotal());
        assertEquals(1, response.items().size());
        assertEquals(PRODUCT_ID.toString(), response.items().getFirst().productId());
        assertEquals(2, response.items().getFirst().quantity());
        assertEquals(0, new BigDecimal("20.00").compareTo(response.totalPrice()));

        assertNull(cartStore.getCart());
    }

    @Test
    public void submitCartDoNotDelete() {
        var cart = new Cart(CART_ID, List.of(cartItem(2)));
        cartStore.setCart(cart);

        createOrder.whenAnyExchangeReceived(exchange -> {
            throw new ProductClientException(502, "PRODUCT_SERVICE_UNAVAILABLE", "Product service is unavailable");
        });

        var exception = assertThrows(CamelExecutionException.class, () ->
                producerTemplate.requestBodyAndHeader("direct:submitCart", null,
                        "id", CART_ID, OrderResponse.class));


        assertInstanceOf(ProductClientException.class, exception.getCause());

        assertNotNull(cartStore.getCart());
        assertEquals(1, cartStore.getCart().items().size());
    }

    @Test

    public void submitCartInvalid() {
        cartStore.setCart(new Cart(CART_ID, List.of()));

        var exception = assertThrows(CamelExecutionException.class, () -> producerTemplate.requestBodyAndHeader(
                "direct:submitCart", null, "id", CART_ID));

        assertInstanceOf(BeanValidationException.class, exception.getCause());
        assertNotNull(cartStore.getCart());
        assertEquals(0, createOrder.getReceivedCounter());

    }


    private CartItem cartItem(int quantity) {
        return new CartItem(PRODUCT_ID, "test", "test", new BigDecimal("10.00"), quantity);
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import({
            CartRoute.class,
            CartService.class,
            CartDtoMapper.class,
            TestCartStore.class,
            TestRoutes.class
    })
    @ImportAutoConfiguration(CamelAutoConfiguration.class)
    static class TestApplication {
    }

}

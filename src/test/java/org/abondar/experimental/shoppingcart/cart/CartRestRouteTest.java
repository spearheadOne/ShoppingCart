package org.abondar.experimental.shoppingcart.cart;

import com.redis.testcontainers.RedisContainer;
import org.abondar.experimental.shoppingcart.exception.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.datasource.url=jdbc:h2:mem:shopping_cart_cart_rest;MODE=PostgreSQL;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false"
)
public class CartRestRouteTest {

    private static final String PRODUCT_ID = "11111111-1111-1111-1111-111111111111";
    private static final String MISSING_PRODUCT_ID = "00000000-0000-0000-0000-000000000000";

    @Container
    @ServiceConnection
    static RedisContainer redis = new RedisContainer("redis:7-alpine");

    @LocalServerPort
    private int port;

    private RestTestClient restTestClient;

    @BeforeEach
    void setUp() {
        restTestClient = RestTestClient.bindToServer()
                .baseUrl("http://localhost:" + port + "/api")
                .build();
    }

    @Test
    void createCart() {
        var cart = createCartRequest();

        assertNotNull(cart.cartId());
        assertEquals(0, cart.items().size());
        assertEquals(0, cart.itemsTotal());
        assertEquals(0, cart.totalPrice().compareTo(BigDecimal.ZERO));
    }

    @Test
    void getCart() {
        var createdCart = createCartRequest();

        restTestClient.get()
                .uri("/v1/carts/{id}", createdCart.cartId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(CartResponse.class)
                .value(response -> {
                    assertNotNull(response);
                    assertEquals(createdCart.cartId(), response.cartId());
                    assertEquals(0, response.items().size());
                    assertEquals(0, response.itemsTotal());
                    assertEquals(0, response.totalPrice().compareTo(BigDecimal.ZERO));
                });
    }

    @Test
    void getCartNotFound() {
        var cartId = UUID.randomUUID().toString();

        restTestClient.get()
                .uri("/v1/carts/{id}", cartId)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(ErrorResponse.class)
                .value(response -> {
                    assertNotNull(response);
                    assertEquals("CART_NOT_FOUND", response.code());
                });
    }

    @Test
    void addCartItem() {
        var cart = createCartRequest();

        var response = addItem(cart.cartId(), PRODUCT_ID, 2);

        assertEquals(cart.cartId(), response.cartId());
        assertEquals(1, response.items().size());
        assertEquals(2, response.itemsTotal());
        assertEquals(PRODUCT_ID, response.items().getFirst().productId());
        assertEquals("Keyboard", response.items().getFirst().name());
        assertEquals(2, response.items().getFirst().quantity());
        assertEquals(0, response.totalPrice().compareTo(new BigDecimal("199.98")));
    }

    @Test
    void addCartItemIncreasesExistingQuantity() {
        var cart = createCartRequest();
        addItem(cart.cartId(), PRODUCT_ID, 2);

        var response = addItem(cart.cartId(), PRODUCT_ID, 3);

        assertEquals(1, response.items().size());
        assertEquals(5, response.itemsTotal());
        assertEquals(5, response.items().getFirst().quantity());
        assertEquals(0, response.totalPrice().compareTo(new BigDecimal("499.95")));
    }

    @Test
    void addCartItemRejectsInvalidRequest() {
        var cart = createCartRequest();

        restTestClient.post()
                .uri("/v1/carts/{id}/items", cart.cartId())
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CartItemAddRequest(PRODUCT_ID, 0))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(response -> {
                    assertNotNull(response);
                    assertEquals("VALIDATION_ERROR", response.code());
                });
    }

    @Test
    void addCartItemReturnsProductNotFound() {
        var cart = createCartRequest();

        restTestClient.post()
                .uri("/v1/carts/{id}/items", cart.cartId())
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CartItemAddRequest(MISSING_PRODUCT_ID, 1))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(ErrorResponse.class)
                .value(response -> {
                    assertNotNull(response);
                    assertEquals("PRODUCT_NOT_FOUND", response.code());
                });
    }

    @Test
    void updateCartItemQuantity() {
        var cart = createCartRequest();
        addItem(cart.cartId(), PRODUCT_ID, 2);

        restTestClient.patch()
                .uri("/v1/carts/{id}/items/{productId}", cart.cartId(), PRODUCT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CartItemUpdateQuantityRequest(4))
                .exchange()
                .expectStatus().isOk()
                .expectBody(CartResponse.class)
                .value(response -> {
                    assertNotNull(response);
                    assertEquals(1, response.items().size());
                    assertEquals(4, response.itemsTotal());
                    assertEquals(4, response.items().getFirst().quantity());
                    assertEquals(0, response.totalPrice().compareTo(new BigDecimal("399.96")));
                });
    }

    @Test
    void deleteCartItem() {
        var cart = createCartRequest();
        addItem(cart.cartId(), PRODUCT_ID, 2);

        restTestClient.delete()
                .uri("/v1/carts/{id}/items/{productId}", cart.cartId(), PRODUCT_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CartResponse.class)
                .value(response -> {
                    assertNotNull(response);
                    assertEquals(0, response.items().size());
                    assertEquals(0, response.itemsTotal());
                    assertEquals(0, response.totalPrice().compareTo(BigDecimal.ZERO));
                });
    }

    @Test
    void deleteCartItemNotFound() {
        var cart = createCartRequest();

        restTestClient.delete()
                .uri("/v1/carts/{id}/items/{productId}", cart.cartId(), PRODUCT_ID)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(ErrorResponse.class)
                .value(response -> {
                    assertNotNull(response);
                    assertEquals("CART_ITEM_NOT_FOUND", response.code());
                });
    }

    @Test
    void deleteCart() {
        var cart = createCartRequest();

        restTestClient.delete()
                .uri("/v1/carts/{id}", cart.cartId())
                .exchange()
                .expectStatus().isNoContent()
                .expectBody()
                .isEmpty();

        restTestClient.get()
                .uri("/v1/carts/{id}", cart.cartId())
                .exchange()
                .expectStatus().isNotFound();
    }

    private CartResponse createCartRequest() {
        return restTestClient.post()
                .uri("/v1/carts")
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CartResponse.class)
                .returnResult()
                .getResponseBody();
    }

    private CartResponse addItem(String cartId, String productId, int quantity) {
        return restTestClient.post()
                .uri("/v1/carts/{id}/items", cartId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CartItemAddRequest(productId, quantity))
                .exchange()
                .expectStatus().isOk()
                .expectBody(CartResponse.class)
                .returnResult()
                .getResponseBody();
    }
}

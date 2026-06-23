package org.abondar.experimental.shoppingcart.cart;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.redis.testcontainers.RedisContainer;
import org.abondar.experimental.shoppingcart.ShoppingCartApplication;
import org.abondar.experimental.shoppingcart.api.OrderResponse;
import org.abondar.experimental.shoppingcart.exception.ErrorResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(
        classes = ShoppingCartApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class CartRestRouteTest {

    private static final String PRODUCT_ID = "11111111-1111-1111-1111-111111111111";
    private static final String MISSING_PRODUCT_ID = "00000000-0000-0000-0000-000000000000";
    private static final WireMockServer productService = new WireMockServer(options().dynamicPort());
    @Container
    static RedisContainer redis = new RedisContainer("redis:7-alpine");
    @LocalServerPort
    private int port;
    private RestTestClient restTestClient;

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("redis.host", redis::getHost);
        registry.add("redis.port", () -> redis.getMappedPort(6379));
        registry.add("services.product.base-url", () -> productService.baseUrl() + "/api");
    }

    @BeforeAll
    static void startProductService() {
        productService.start();
    }

    @AfterAll
    static void stopProductService() {
        productService.stop();
    }

    @BeforeEach
    void setUp() throws Exception {
        redis.execInContainer("redis-cli", "FLUSHDB");
        productService.resetAll();
        restTestClient = RestTestClient.bindToServer()
                .baseUrl("http://localhost:" + port + "/api")
                .build();

        productService.stubFor(
                get(urlEqualTo("/api/v1/products/" + PRODUCT_ID))
                        .willReturn(
                                okJson("""
                                        {
                                          "id": "11111111-1111-1111-1111-111111111111",
                                          "name": "Keyboard",
                                          "imgUrl": "https://example.com/keyboard.jpg",
                                          "price": 99.99
                                        }
                                        """)
                                        .withHeader(ACCESS_CONTROL_ALLOW_ORIGIN, "*")));

        productService.stubFor(
                get(urlEqualTo("/api/v1/products/" + MISSING_PRODUCT_ID))
                        .willReturn(aResponse()
                                .withStatus(404)
                                .withHeader(
                                        "Content-Type",
                                        "application/json"
                                )));

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
    void createCartReturnsSingleCorsOriginHeader() {
        restTestClient.post()
                .uri("/v1/carts")
                .header("Origin", "http://localhost:8081")
                .contentType(MediaType.APPLICATION_JSON)
                .body("{}")
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().valueEquals(ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:8081");
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
    void addCartItemReturnsSingleCorsOriginHeader() {
        var cart = createCartRequest();

        restTestClient.post()
                .uri("/v1/carts/{id}/items", cart.cartId())
                .header("Origin", "http://localhost:8081")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CartItemAddRequest(PRODUCT_ID, 1))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:8081");
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
    void submitCart() {
        var cart = createCartRequest();
        addItem(cart.cartId(), PRODUCT_ID, 2);

        productService.stubFor(post(urlEqualTo("/api/v1/orders"))
                .withRequestBody(matchingJsonPath("$.cartId", equalTo(cart.cartId())))
                .withRequestBody(matchingJsonPath("$.items[0].productId", equalTo(PRODUCT_ID)))
                .withRequestBody(matchingJsonPath("$.items[0].quantity", equalTo("2")))
                .willReturn(okJson("""
                        {
                          "orderId": "33333333-3333-3333-3333-333333333333",
                          "cartId": "%s",
                          "status": "CREATED",
                          "items": [
                            {
                              "productId": "11111111-1111-1111-1111-111111111111",
                              "name": "Keyboard",
                              "imgUrl": "https://example.com/keyboard.jpg",
                              "unitPrice": 99.99,
                              "quantity": 2,
                              "lineTotal": 199.98
                            }
                          ],
                          "itemsTotal": 2,
                          "totalPrice": 199.98,
                          "createdAt": "2026-06-23T12:00:00Z"
                        }
                        """.formatted(cart.cartId()))));

        restTestClient.post()
                .uri("/v1/carts/{id}/submit", cart.cartId())
                .exchange()
                .expectStatus().isCreated()
                .expectBody(OrderResponse.class)
                .value(response -> {
                    assertNotNull(response);
                    assertEquals("33333333-3333-3333-3333-333333333333", response.orderId());
                    assertEquals(cart.cartId(), response.cartId());
                    assertEquals("CREATED", response.status());
                    assertEquals(1, response.items().size());
                    assertEquals(2, response.itemsTotal());
                    assertEquals(0, new BigDecimal("199.98").compareTo(response.totalPrice()));
                });

        productService.verify(postRequestedFor(urlEqualTo("/api/v1/orders")));

        restTestClient.get()
                .uri("/v1/carts/{id}", cart.cartId())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void submitEmptyCartRejectsRequestAndKeepsCart() {
        var cart = createCartRequest();

        restTestClient.post()
                .uri("/v1/carts/{id}/submit", cart.cartId())
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(response -> {
                    assertNotNull(response);
                    assertEquals("VALIDATION_ERROR", response.code());
                });

        productService.verify(0, postRequestedFor(urlEqualTo("/api/v1/orders")));

        restTestClient.get()
                .uri("/v1/carts/{id}", cart.cartId())
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void submitCartNotFound() {
        restTestClient.post()
                .uri("/v1/carts/{id}/submit", UUID.randomUUID())
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(ErrorResponse.class)
                .value(response -> {
                    assertNotNull(response);
                    assertEquals("CART_NOT_FOUND", response.code());
                });
    }

    @Test
    void submitCartKeepsCartWhenOrderServiceUnavailable() {
        var cart = createCartRequest();
        addItem(cart.cartId(), PRODUCT_ID, 1);

        productService.stubFor(post(urlEqualTo("/api/v1/orders"))
                .willReturn(aResponse().withStatus(500)));

        restTestClient.post()
                .uri("/v1/carts/{id}/submit", cart.cartId())
                .exchange()
                .expectStatus().isEqualTo(502)
                .expectBody(ErrorResponse.class)
                .value(response -> {
                    assertNotNull(response);
                    assertEquals("PRODUCT_SERVICE_UNAVAILABLE", response.code());
                });

        restTestClient.get()
                .uri("/v1/carts/{id}", cart.cartId())
                .exchange()
                .expectStatus().isOk();
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

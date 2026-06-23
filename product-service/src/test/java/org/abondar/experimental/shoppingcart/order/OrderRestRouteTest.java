package org.abondar.experimental.shoppingcart.order;

import org.abondar.experimental.shoppingcart.ProductServiceApplication;
import org.abondar.experimental.shoppingcart.api.CreateOrderItemRequest;
import org.abondar.experimental.shoppingcart.api.CreateOrderRequest;
import org.abondar.experimental.shoppingcart.api.OrderResponse;
import org.abondar.experimental.shoppingcart.exception.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
        classes = ProductServiceApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.datasource.url=jdbc:h2:mem:order_rest;MODE=PostgreSQL;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false"
)
public class OrderRestRouteTest {

    private static final String PRODUCT_ID = "11111111-1111-1111-1111-111111111111";
    private static final String MISSING_PRODUCT_ID = "00000000-0000-0000-0000-000000000000";

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private OrderMapper orderMapper;

    private RestTestClient restTestClient;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM order_items");
        jdbcTemplate.update("DELETE FROM orders");

        restTestClient = RestTestClient.bindToServer()
                .baseUrl("http://localhost:" + port + "/api")
                .build();
    }

    @Test
    void createOrder() {
        var cartId = UUID.randomUUID();
        var request = new CreateOrderRequest(
                cartId.toString(),
                List.of(new CreateOrderItemRequest(PRODUCT_ID, 2))
        );

        restTestClient.post()
                .uri("/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(OrderResponse.class)
                .value(response -> {
                    assertNotNull(response);
                    assertNotNull(response.orderId());
                    assertEquals(cartId.toString(), response.cartId());
                    assertEquals(OrderStatus.CREATED.name(), response.status());
                    assertEquals(1, response.items().size());
                    assertEquals(2, response.itemsTotal());
                    assertEquals(0, new BigDecimal("199.98").compareTo(response.totalPrice()));

                    var item = response.items().getFirst();
                    assertEquals(PRODUCT_ID, item.productId());
                    assertEquals("Keyboard", item.name());
                    assertEquals(2, item.quantity());
                    assertEquals(0, new BigDecimal("99.99").compareTo(item.unitPrice()));
                    assertEquals(0, new BigDecimal("199.98").compareTo(item.lineTotal()));

                    var orderId = UUID.fromString(response.orderId());
                    assertTrue(orderMapper.findOrderById(orderId).isPresent());
                    assertEquals(1, orderMapper.findOrderItems(orderId).size());
                });
    }

    @Test
    void createOrderRejectsEmptyItems() {
        var request = new CreateOrderRequest(UUID.randomUUID().toString(), List.of());

        restTestClient.post()
                .uri("/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(response -> {
                    assertNotNull(response);
                    assertEquals("VALIDATION_ERROR", response.code());
                });
    }

    @Test
    void createOrderReturnsProductNotFound() {
        var request = new CreateOrderRequest(
                UUID.randomUUID().toString(),
                List.of(new CreateOrderItemRequest(MISSING_PRODUCT_ID, 1))
        );

        restTestClient.post()
                .uri("/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(ErrorResponse.class)
                .value(response -> {
                    assertNotNull(response);
                    assertEquals("PRODUCT_NOT_FOUND", response.code());
                });
    }

}

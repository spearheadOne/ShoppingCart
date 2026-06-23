package org.abondar.experimental.shoppingcart.product;


import org.abondar.experimental.shoppingcart.api.ProductListResponse;
import org.abondar.experimental.shoppingcart.api.ProductResponse;
import org.abondar.experimental.shoppingcart.exception.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.datasource.url=jdbc:h2:mem:shopping_cart_rest;MODE=PostgreSQL;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false"
)
public class ProductRestRouteTest {

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
    void getProductById() {
        restTestClient.get()
                .uri("/v1/products/{id}", "11111111-1111-1111-1111-111111111111")
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductResponse.class)
                .value(response -> {
                    assertNotNull(response);
                    assertEquals(UUID.fromString("11111111-1111-1111-1111-111111111111"), response.id());
                    assertEquals("Keyboard", response.name());
                    assertEquals("/images/keyboard.png", response.imgUrl());
                    assertEquals(0, response.price().compareTo(new BigDecimal("99.99")));
                });
    }

    @Test
    void getProductByIdNotFound() {
        restTestClient.get()
                .uri("/v1/products/{id}", "00000000-0000-0000-0000-000000000000")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(ErrorResponse.class)
                .value(response -> {
                    assertNotNull(response);
                    assertEquals("PRODUCT_NOT_FOUND", response.code());
                    assertEquals(
                            "Product with id 00000000-0000-0000-0000-000000000000 not found",
                            response.message());
                });
    }


    @Test
    void getProductList() {
        restTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/products")
                        .queryParam("limit", 5)
                        .queryParam("offset", 5)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductListResponse.class)
                .value(response -> {
                    assertNotNull(response);
                    assertEquals(5, response.limit());
                    assertEquals(5, response.offset());
                    assertEquals(15, response.total());
                    assertEquals(5, response.products().size());
                });
    }


    @Test
    void getProductListDefault() {
        restTestClient.get()
                .uri("/v1/products")
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductListResponse.class)
                .value(response -> {
                    assertNotNull(response);
                    assertEquals(10, response.limit());
                    assertEquals(0, response.offset());
                    assertEquals(15, response.total());
                    assertEquals(10, response.products().size());
                });
    }


    @Test
    void getProductListBadRequestForInvalidPagination() {
        restTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/products")
                        .queryParam("limit", 0)
                        .queryParam("offset", 0)
                        .build())
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(ErrorResponse.class)
                .value(response -> {
                    assertNotNull(response);
                    assertEquals("BAD_REQUEST", response.code());
                    assertEquals("Wrong pagination params", response.message());
                });

    }
}

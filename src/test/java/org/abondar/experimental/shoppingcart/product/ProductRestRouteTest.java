package org.abondar.experimental.shoppingcart.product;


import org.abondar.experimental.shoppingcart.exception.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.web.servlet.client.RestTestClient;

import static org.assertj.core.api.Assertions.assertThat;

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
    void getProductByIdR() {
        restTestClient.get()
                .uri("/v1/products/{id}", "11111111-1111-1111-1111-111111111111")
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductResponse.class)
                .value(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.id()).isEqualTo("11111111-1111-1111-1111-111111111111");
                    assertThat(response.name()).isEqualTo("Keyboard");
                    assertThat(response.imgUrl()).isEqualTo("/images/keyboard.png");
                    assertThat(response.price()).isEqualByComparingTo("99.99");
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
                    assertThat(response).isNotNull();
                    assertThat(response.code()).isEqualTo("PRODUCT_NOT_FOUND");
                    assertThat(response.message()).isEqualTo(
                            "Product with id 00000000-0000-0000-0000-000000000000 not found");
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
                    assertThat(response).isNotNull();
                    assertThat(response.limit()).isEqualTo(5);
                    assertThat(response.offset()).isEqualTo(5);
                    assertThat(response.total()).isEqualTo(15);
                    assertThat(response.products()).hasSize(5);
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
                    assertThat(response).isNotNull();
                    assertThat(response.limit()).isEqualTo(10);
                    assertThat(response.offset()).isZero();
                    assertThat(response.total()).isEqualTo(15);
                    assertThat(response.products()).hasSize(10);
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
                    assertThat(response).isNotNull();
                    assertThat(response.code()).isEqualTo("BAD_REQUEST");
                    assertThat(response.message()).isEqualTo("Wrong pagination params");
                });

    }
}

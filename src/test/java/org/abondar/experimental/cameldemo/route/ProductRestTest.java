package org.abondar.experimental.cameldemo.route;

import org.abondar.experimental.cameldemo.shoppingcart.model.CartProduct;
import org.abondar.experimental.cameldemo.shoppingcart.model.CartProductPostResponse;
import org.abondar.experimental.cameldemo.shoppingcart.processor.ProductProcessor;
import org.abondar.experimental.cameldemo.shoppingcart.route.FirebaseRoute;
import org.abondar.experimental.cameldemo.shoppingcart.route.ProductRestRoute;
import org.abondar.experimental.cameldemo.shoppingcart.transform.ResponseBodyTransformer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(
    properties = {"firebase.products=testProducts.json"},
    classes = {
      ProductRestRoute.class,
      FirebaseRoute.class,
      ProductProcessor.class,
      ResponseBodyTransformer.class
    },
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@EnableAutoConfiguration
@ActiveProfiles("integration")
public class ProductRestTest {

  private String testId;

  @Autowired private WebTestClient webTestClient;


  @Test
  @Order(1)
  public void postProductTest() {
    var testProduct = new CartProduct("test", "test", BigDecimal.valueOf(0));

    var body =
        webTestClient
            .post()
            .uri("/cart/product")
            .bodyValue(testProduct)
            .exchange()
            .expectStatus()
            .is2xxSuccessful()
            .expectBody(CartProductPostResponse.class)
            .returnResult()
            .getResponseBody();

    testId = body.name();
  }

  @Test
  @Order(2)
  public void getByIdProductTest() {
    var body =
        webTestClient
            .get()
            .uri("/cart/product/" + testId)
            .exchange()
            .expectStatus()
            .is2xxSuccessful()
            .expectBody(CartProduct.class)
            .returnResult()
            .getResponseBody();

    assertEquals("test", body.getName());
    assertEquals("test", body.getImgUrl());
    assertEquals(BigDecimal.valueOf(0), body.getPrice());
  }

  @Test
  @Order(3)
  public void getByLimitProductTest() {

    var body =
        webTestClient
            .get()
            .uri(uriBuilder -> uriBuilder.path("/cart/product/").queryParam("limit", "1").build())
            .exchange()
            .expectStatus()
            .is2xxSuccessful()
            .expectBody(CartProduct.class)
            .returnResult()
            .getResponseBody();

    assertEquals("test", body.getName());
    assertEquals("test", body.getImgUrl());
    assertEquals(BigDecimal.valueOf(0), body.getPrice());
  }
}

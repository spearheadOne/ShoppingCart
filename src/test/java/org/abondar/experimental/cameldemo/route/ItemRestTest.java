package org.abondar.experimental.cameldemo.route;

import org.abondar.experimental.cameldemo.shoppingcart.model.CartItems;
import org.abondar.experimental.cameldemo.shoppingcart.processor.ProductProcessor;
import org.abondar.experimental.cameldemo.shoppingcart.route.FirebaseRoute;
import org.abondar.experimental.cameldemo.shoppingcart.route.ItemRestRoute;
import org.abondar.experimental.cameldemo.shoppingcart.transform.ResponseBodyTransformer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
    properties = {"firebase.cartItems=testItems.json"},
    classes = {
      ItemRestRoute.class,
      FirebaseRoute.class,
      ProductProcessor.class,
      ResponseBodyTransformer.class
    },
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@EnableAutoConfiguration
@ActiveProfiles("integration")
public class ItemRestTest {
  @Autowired private WebTestClient webTestClient;

  @Test
  public void getItemTest() {
    var body =
        webTestClient
            .get()
            .uri("/cart/item")
            .exchange()
            .expectStatus()
            .is2xxSuccessful()
            .expectBody(CartItems.class)
            .returnResult()
            .getResponseBody();

    assertTrue(body.changed());
    assertTrue(body.showCart());
    assertEquals(2, body.itemsTotal());
    assertNull(body.items());
  }
}

package org.abondar.experimental.cameldemo.route;

//@SpringBootTest(
//    properties = {"firebase.cartItems=testItems.json"},
//    classes = {
//      ItemRestRoute.class,
//      FirebaseRoute.class,
//      ProductProcessor.class,
//      ResponseBodyTransformer.class
//    },
//    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@AutoConfigureWebTestClient
//@EnableAutoConfiguration
//@ActiveProfiles("integration")
//public class ItemRestTest {
//  @Autowired private WebTestClient webTestClient;
//
//  @Test
//  public void getItemTest() {
//    var body =
//        webTestClient
//            .get()
//            .uri("/cart/item")
//            .exchange()
//            .expectStatus()
//            .is2xxSuccessful()
//            .expectBody(CartItems.class)
//            .returnResult()
//            .getResponseBody();
//
//    assertTrue(body.changed());
//    assertTrue(body.showCart());
//    assertEquals(2, body.itemsTotal());
//    assertNull(body.items());
//  }
//}

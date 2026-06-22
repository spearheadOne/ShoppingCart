package org.abondar.experimental.shoppingcart.cart;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ProductClientTest {

    private static final UUID PRODUCT_ID =
            UUID.fromString("11111111-1111-1111-1111-111111111111");

    private MockRestServiceServer server;
    private ProductClient productClient;

    @BeforeEach
    void setUp() {
        var builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        productClient = new ProductClient(builder, "http://product-service/api");
    }

    @Test
    void getsProductFromProductService() {
        server.expect(once(), requestTo("http://product-service/api/v1/products/" + PRODUCT_ID))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "id": "11111111-1111-1111-1111-111111111111",
                          "name": "Keyboard",
                          "imgUrl": "/images/keyboard.png",
                          "price": 99.99
                        }
                        """, MediaType.APPLICATION_JSON));

        var product = productClient.getProduct(PRODUCT_ID);

        assertEquals("Keyboard", product.name());
        assertEquals(0, new BigDecimal("99.99").compareTo(product.price()));
        server.verify();
    }

    @Test
    void mapsMissingProductToDomainException() {
        server.expect(once(), requestTo("http://product-service/api/v1/products/" + PRODUCT_ID))
                .andRespond(withResourceNotFound());

        assertThrows(ProductNotFoundException.class, () -> productClient.getProduct(PRODUCT_ID));
        server.verify();
    }
}

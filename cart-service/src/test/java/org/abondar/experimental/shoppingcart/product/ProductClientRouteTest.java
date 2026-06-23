package org.abondar.experimental.shoppingcart.product;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.abondar.experimental.shoppingcart.cart.CartExceptionHandler;
import org.abondar.experimental.shoppingcart.exception.ErrorResponse;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.spring.boot.CamelAutoConfiguration;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.math.BigDecimal;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@CamelSpringBootTest
@SpringBootTest(
        classes = ProductClientRouteTest.TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
public class ProductClientRouteTest {

    private static final String PRODUCT_ID = "11111111-1111-1111-1111-111111111111";

    private static final WireMockServer productService = new WireMockServer(options().dynamicPort());

    @Autowired
    private ProducerTemplate producerTemplate;

    @DynamicPropertySource
    static void productServiceProperties(DynamicPropertyRegistry registry) {
        registry.add(
                "services.product.base-url",
                () -> productService.baseUrl() + "/api"

        );
    }

    @BeforeAll
    public static void startProductService() {
        productService.start();
    }

    @AfterAll
    public static void stopProductService() {
        productService.stop();
    }

    @BeforeEach
    public void resetProductService() {
        productService.resetAll();
    }

    @Test
    public void getProduct() {
        productService.stubFor(
                get(urlEqualTo("/api/v1/products/" + PRODUCT_ID))
                        .willReturn(okJson("""
                                {
                                                                          "id": "11111111-1111-1111-1111-111111111111",
                                                                          "name": "Test product",
                                                                          "imgUrl": "https://example.com/product.jpg",
                                                                          "price": 10.00
                                                                        }
                                """))
        );

        var exchange = producerTemplate.request("direct:getProduct",
                e -> e.setProperty("productId", PRODUCT_ID));
        assertInstanceOf(ProductClientResponse.class, exchange.getMessage().getBody());

        var product = exchange.getMessage().getBody(ProductClientResponse.class);

        assertEquals(PRODUCT_ID, product.id().toString());
        assertEquals("Test product", product.name());
        assertEquals(0, product.price().compareTo(new BigDecimal("10.00")));
    }

    @Test
    public void getProductNotFound() {
        productService.stubFor(
                get(urlEqualTo("/api/v1/products/" + PRODUCT_ID))
                        .willReturn(aResponse().withStatus(404))
        );

        var exchange = producerTemplate.request("direct:getProduct",
                e -> e.setProperty("productId", PRODUCT_ID));
        assertEquals(404, exchange.getMessage().getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class));


        var error = exchange.getMessage().getBody(ErrorResponse.class);
        assertNotNull(error);
        assertEquals("PRODUCT_NOT_FOUND", error.code());
        assertEquals("Product with id %s not found".formatted(PRODUCT_ID), error.message());
    }


    @Test
    public void productServiceUnavailable() {
        productService.stubFor(
                get(urlEqualTo("/api/v1/products/" + PRODUCT_ID))
                        .willReturn(aResponse().withStatus(500))
        );

        var exchange = producerTemplate.request("direct:getProduct",
                e -> e.setProperty("productId", PRODUCT_ID));
        assertEquals(502, exchange.getMessage().getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class));


        var error = exchange.getMessage().getBody(ErrorResponse.class);
        assertNotNull(error);
        assertEquals("PRODUCT_SERVICE_UNAVAILABLE", error.code());
        assertEquals("Product service is unavailable", error.message());
    }

    @Test
    public void productServiceBadRequest() {
        productService.stubFor(
                get(urlEqualTo("/api/v1/products/" + PRODUCT_ID))
                        .willReturn(aResponse().withStatus(400))
        );

        var exchange = producerTemplate.request("direct:getProduct",
                e -> e.setProperty("productId", PRODUCT_ID));
        assertEquals(502,  exchange.getMessage().getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class));


        var error = exchange.getMessage().getBody(ErrorResponse.class);
        assertNotNull(error);
        assertEquals("PRODUCT_SERVICE_ERROR", error.code());
        assertEquals("Product service rejected the request", error.message());
    }


    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import({ProductClientRoute.class, CartExceptionHandler.class})
    @ImportAutoConfiguration(CamelAutoConfiguration.class)
    static class TestApplication {
    }
}

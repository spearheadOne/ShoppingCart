package org.abondar.experimental.shoppingcart.product;

import org.abondar.experimental.shoppingcart.api.ProductListResponse;
import org.abondar.experimental.shoppingcart.api.ProductResponse;
import org.apache.camel.CamelContext;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProductRouteTest {
    private static final UUID PRODUCT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private CamelContext camelContext;
    private ProducerTemplate producerTemplate;

    private MockEndpoint findProductById;
    private MockEndpoint findProducts;
    private MockEndpoint countProducts;

    private static Stream<Map<String, Object>> badPaginationParams() {
        return Stream.of(
                Map.of("limit", "0", "offset", "0"),
                Map.of("limit", "-1", "offset", "0"),
                Map.of("limit", "101", "offset", "0"),
                Map.of("limit", "10", "offset", "-1"),
                Map.of("limit", "abc", "offset", "0"),
                Map.of("limit", "10", "offset", "abc"),
                Map.of("limit", "", "offset", "0"),
                Map.of("limit", "10", "offset", "")
        );

    }

    @BeforeEach
    public void setUp() throws Exception {
        var responseMapper = new ProductResponseMapper();

        camelContext = new DefaultCamelContext();
        camelContext.addRoutes(new ProductRoute(responseMapper));

        AdviceWith.adviceWith(camelContext, "getProductById",
                advice -> advice.weaveById("findProductByIdQuery")
                        .replace()
                        .to("mock:findProductById")
        );

        AdviceWith.adviceWith(camelContext, "getProducts",
                advice -> {
                    advice.weaveById("findProductsQuery")
                            .replace()
                            .to("mock:findProducts");

                    advice.weaveById("countProductsQuery")
                            .replace()
                            .to("mock:countProducts");
                }
        );


        camelContext.start();

        producerTemplate = camelContext.createProducerTemplate();

        findProductById = camelContext.getEndpoint("mock:findProductById", MockEndpoint.class);
        findProducts = camelContext.getEndpoint("mock:findProducts", MockEndpoint.class);
        countProducts = camelContext.getEndpoint("mock:countProducts", MockEndpoint.class);
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (producerTemplate != null) producerTemplate.stop();

        if (camelContext != null) camelContext.stop();

    }

    @Test
    public void getProductById() {
        var product = product();

        findProductById.expectedMessageCount(1);
        findProductById.expectedBodiesReceived(Map.of("id", PRODUCT_ID));
        findProductById.whenAnyExchangeReceived(exchange -> exchange.getMessage().setBody(product));

        var result = producerTemplate.requestBodyAndHeader("direct:getProductById", null, "id",
                PRODUCT_ID.toString(), ProductResponse.class);

        assertEquals(PRODUCT_ID, result.id());
        assertEquals(product.name(), result.name());
        assertEquals(product.imgUrl(), result.imgUrl());
        assertEquals(product.price(), result.price());
    }

    @Test
    void usesProductMapperFromCurrentPackage() {
        assertEquals(ProductMapper.class.getName(), ProductRoute.PRODUCT_MAPPER);
    }

    @Test
    public void getProductByIdNotFound() {
        findProductById.expectedMessageCount(1);
        findProductById.expectedBodiesReceived(Map.of("id", PRODUCT_ID));
        findProductById.whenAnyExchangeReceived(exchange -> exchange.getMessage().setBody(null));

        var exception = assertThrows(CamelExecutionException.class,
                () -> producerTemplate.requestBodyAndHeader(
                        "direct:getProductById", null, "id",
                        PRODUCT_ID.toString(), ProductResponse.class));


        assertInstanceOf(ProductNotFoundException.class, exception.getCause());
    }

    @Test
    public void getProductList() {
        var limit = 5;
        var offset = 0;
        var products = List.of(product());

        findProducts.expectedMessageCount(1);
        findProducts.expectedBodiesReceived(Map.of("limit", limit, "offset", offset));
        findProducts.whenAnyExchangeReceived(exchange -> exchange.getMessage().setBody(products));

        countProducts.expectedMessageCount(1);
        countProducts.whenAnyExchangeReceived(exchange -> exchange.getMessage().setBody(3L));

        var result = producerTemplate.requestBodyAndHeaders(
                "direct:getProducts", null,
                Map.of(
                        "limit", "5",
                        "offset", "0"
                ),
                ProductListResponse.class);

        assertEquals(1, result.products().size());
        assertEquals(limit, result.limit());
        assertEquals(offset, result.offset());
        assertEquals(3, result.total());
        assertEquals(PRODUCT_ID, result.products().getFirst().id());
    }

    @Test
    public void getProductListDefaultLimitAndOffset() {
        var limit = 10;
        var offset = 0;

        findProducts.expectedMessageCount(1);
        findProducts.expectedBodiesReceived(Map.of("limit", limit, "offset", offset));
        findProducts.whenAnyExchangeReceived(exchange -> exchange.getMessage().setBody(List.of()));

        countProducts.expectedMessageCount(1);
        countProducts.whenAnyExchangeReceived(exchange -> exchange.getMessage().setBody(3L));

        var result = producerTemplate.requestBody(
                "direct:getProducts",
                null,
                ProductListResponse.class);

        assertTrue(result.products().isEmpty());
        assertEquals(limit, result.limit());
        assertEquals(offset, result.offset());
        assertEquals(3, result.total());
    }

    @ParameterizedTest
    @MethodSource("badPaginationParams")
    public void getProductListBadPaginationParams(Map<String, Object> headers) {
        findProducts.expectedMessageCount(0);
        countProducts.expectedMessageCount(0);

        var exception = assertThrows(CamelExecutionException.class,
                () -> producerTemplate.requestBodyAndHeaders("direct:getProducts", null, headers));

        assertEquals(ProductPaginationException.class, exception.getCause().getClass());
    }

    private Product product() {
        return new Product(PRODUCT_ID, "test", "test", BigDecimal.ONE);
    }
}

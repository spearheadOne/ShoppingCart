package org.abondar.experimental.shoppingcart.product;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductRouteTest {

    @Mock
    private ProductService productService;

    private CamelContext camelContext;
    private ProducerTemplate producerTemplate;


    @BeforeEach
    public void setUp() throws Exception {
        camelContext = new DefaultCamelContext();
        camelContext.addRoutes(new ProductRoute(productService));
        camelContext.start();

        producerTemplate = camelContext.createProducerTemplate();
    }


    @AfterEach
    public void tearDown() throws Exception {
        if (producerTemplate != null) producerTemplate.stop();

        if (camelContext != null) camelContext.stop();

    }


    @Test
    public void getProductById() {
        var productId = "11111111-1111-1111-1111-111111111111";

        var response = new ProductResponse(productId, "test", "test", BigDecimal.ONE);

        when(productService.getProductById(productId)).thenReturn(response);

        var result = producerTemplate.requestBodyAndHeader("direct:getProductById", null, "id",
                productId, ProductResponse.class);

        assertEquals(productId, result.id());
        assertEquals(result.name(), response.name());
        assertEquals(result.imgUrl(), response.imgUrl());
        assertEquals(result.price(), response.price());
    }

    @Test
    public void getProductList() {
        var limit = 5;
        var offset = 0;
        var response = new ProductListResponse(List.of(), limit, offset, 3);

        when(productService.getProductList(limit, offset)).thenReturn(response);

        var result = producerTemplate.requestBodyAndHeaders("direct:getProducts", null, Map.of(
                "limit", "5",
                "offset", "0"
        ), ProductListResponse.class);

        assertEquals(response.products().size(), result.products().size());
        verify(productService).getProductList(limit, offset);
        verifyNoMoreInteractions(productService);

    }


    @Test
    public void getProductListDefaultLimitAndOffset() {
        var limit = 10;
        var offset = 0;
        var response = new ProductListResponse(List.of(), limit, offset, 3);

        when(productService.getProductList(limit, offset)).thenReturn(response);

        var result = producerTemplate.requestBody("direct:getProducts", null,
                ProductListResponse.class);

        assertEquals(response.products().size(), result.products().size());
        verify(productService).getProductList(limit, offset);
        verifyNoMoreInteractions(productService);

    }


    @ParameterizedTest
    @MethodSource("badPaginationParams")
    public void getProductListBadPaginationParams(Map<String, Object> headers) {
        var exception = assertThrows(CamelExecutionException.class,
                () -> producerTemplate.requestBodyAndHeaders("direct:getProducts", null, headers));

        assertEquals(ProductPaginationException.class, exception.getCause().getClass());
        verifyNoMoreInteractions(productService);
    }

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
}

package org.abondar.experimental.shoppingcart.cart;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.bean.validator.BeanValidationException;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class CartRouteTest {

    private static final String CART_ID = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa";

    private static final String PRODUCT_ID = "11111111-1111-1111-1111-111111111111";

    @Mock
    private CartService cartService;


    private CamelContext camelContext;
    private ProducerTemplate producerTemplate;

    private static Stream<CartItemAddRequest> invalidAddRequests() {
        return Stream.of(
                new CartItemAddRequest(null, 1),
                new CartItemAddRequest("", 1),
                new CartItemAddRequest("   ", 1),
                new CartItemAddRequest(PRODUCT_ID, 0),
                new CartItemAddRequest(PRODUCT_ID, -1)
        );
    }

    @BeforeEach
    public void setUp() throws Exception {
        camelContext = new DefaultCamelContext();
        camelContext.addRoutes(new CartRoute(cartService));
        camelContext.start();

        producerTemplate = camelContext.createProducerTemplate();
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (producerTemplate != null) producerTemplate.stop();

        if (camelContext != null) camelContext.stop();
    }

    @Test
    public void createCart() {
        var response = emptyCartResponse();

        when(cartService.createCart()).thenReturn(response);

        var exchange = producerTemplate.request("direct:createCart", ignored -> {
        });

        assertEquals(response, exchange.getIn().getBody(CartResponse.class));
        assertEquals(201, exchange.getIn().getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class));

        verify(cartService, times(1)).createCart();
        verifyNoMoreInteractions(cartService);
    }

    @Test
    public void getCart() {
        var response = emptyCartResponse();

        when(cartService.getCart(CART_ID)).thenReturn(response);

        var result = producerTemplate
                .requestBodyAndHeader("direct:getCart", null, "id", CART_ID, CartResponse.class);

        assertEquals(response, result);
        verify(cartService, times(1)).getCart(CART_ID);
        verifyNoMoreInteractions(cartService);
    }

    @Test
    public void addCartItem() {
        var request = new CartItemAddRequest(PRODUCT_ID, 2);
        var response = cartResponse(2);

        when(cartService.addCartItem(CART_ID, request))
                .thenReturn(response);

        var result = producerTemplate
                .requestBodyAndHeader("direct:addCartItem", request, "id", CART_ID, CartResponse.class);

        assertEquals(response, result);
        verify(cartService, times(1)).addCartItem(CART_ID, request);
        verifyNoMoreInteractions(cartService);
    }

    @ParameterizedTest
    @MethodSource("invalidAddRequests")
    public void addCartItemInvalidRequest(CartItemAddRequest request) {
        var exception = assertThrows(CamelExecutionException.class, () ->
                producerTemplate.requestBodyAndHeader("direct:addCartItem", request,
                        "id", CART_ID, CartResponse.class));

        assertEquals(BeanValidationException.class, exception.getCause().getClass());
    }

    @Test
    public void updateCartQuantity() {
        var request = new CartItemUpdateQuantityRequest(2);
        var response = cartResponse(5);

        when(cartService.updateCartQuantity(CART_ID, PRODUCT_ID, request)).thenReturn(response);

        var result = producerTemplate.requestBodyAndHeaders("direct:updateCartQuantity", request,
                Map.of(
                        "id", CART_ID,
                        "productId", PRODUCT_ID
                ), CartResponse.class);

        assertEquals(response, result);
        verify(cartService, times(1)).updateCartQuantity(CART_ID, PRODUCT_ID, request);
        verifyNoMoreInteractions(cartService);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -10})
    public void updateCartQuantityInvalidQuantity(int quantity) {
        var request = new CartItemUpdateQuantityRequest(quantity);

        var exception = assertThrows(CamelExecutionException.class, () ->
                producerTemplate.requestBodyAndHeaders("direct:updateCartQuantity", request,
                        Map.of(
                                "id", CART_ID,
                                "productId", PRODUCT_ID
                        ), CartResponse.class));

        assertEquals(BeanValidationException.class, exception.getCause().getClass());
        verifyNoMoreInteractions(cartService);
    }

    @Test
    void deleteCartItem() {
        var response = emptyCartResponse();

        when(cartService.deleteCartItem(CART_ID, PRODUCT_ID)).thenReturn(response);

        var result = producerTemplate.requestBodyAndHeaders("direct:deleteCartItem", null,
                Map.of(
                        "id", CART_ID,
                        "productId", PRODUCT_ID
                ),
                CartResponse.class);

        assertEquals(response, result);

        verify(cartService, times(1)).deleteCartItem(CART_ID, PRODUCT_ID);
        verifyNoMoreInteractions(cartService);
    }

    @Test
    public void deleteCart() {
        var exchange = producerTemplate.request("direct:deleteCart",
                current -> current.getMessage().setHeader("id", CART_ID)
        );

        assertEquals(204, exchange.getMessage().getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class));
        assertNull(exchange.getMessage().getBody());

        verify(cartService).deleteCart(CART_ID);
        verifyNoMoreInteractions(cartService);
    }

    private CartResponse emptyCartResponse() {
        return new CartResponse(CART_ID, List.of(), 0, BigDecimal.ZERO);
    }

    private CartResponse cartResponse(int quantity) {
        var unitPrice = new BigDecimal("10.00");
        var lineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));

        var item = new CartItemResponse(PRODUCT_ID, "test", "test", unitPrice, quantity, lineTotal);

        return new CartResponse(CART_ID, List.of(item), quantity, lineTotal);
    }
}

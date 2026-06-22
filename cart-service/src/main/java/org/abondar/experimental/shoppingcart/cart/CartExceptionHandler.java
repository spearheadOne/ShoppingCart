package org.abondar.experimental.shoppingcart.cart;

import com.fasterxml.jackson.core.JsonParseException;
import jakarta.validation.ConstraintViolationException;
import org.abondar.experimental.shoppingcart.exception.ErrorResponse;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteConfigurationBuilder;
import org.apache.camel.component.bean.validator.BeanValidationException;
import org.springframework.stereotype.Component;

@Component
public class CartExceptionHandler extends RouteConfigurationBuilder {
    @Override
    public void configuration() {
        var cartExceptionConfiguration = routeConfiguration();

        cartExceptionConfiguration.onException(ProductNotFoundException.class)
                .handled(true)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(404))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .setBody(exchange ->
                        new ErrorResponse("PRODUCT_NOT_FOUND", exchange.getProperty(Exchange.EXCEPTION_CAUGHT,
                                        Exception.class)
                                .getMessage()));

        cartExceptionConfiguration.onException(ProductServiceException.class)
                .handled(true)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(503))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .setBody(exchange -> new ErrorResponse(
                        "PRODUCT_SERVICE_UNAVAILABLE",
                        exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class).getMessage()
                ));

        cartExceptionConfiguration.onException(CartNotFoundException.class)
                .handled(true)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(404))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .setBody(exchange ->
                        new ErrorResponse("CART_NOT_FOUND", exchange.getProperty(Exchange.EXCEPTION_CAUGHT,
                                        Exception.class)
                                .getMessage()));

        cartExceptionConfiguration.onException(CartItemNotFoundException.class)
                .handled(true)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(404))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .setBody(exchange ->
                        new ErrorResponse("CART_ITEM_NOT_FOUND", exchange.getProperty(Exchange.EXCEPTION_CAUGHT,
                                        Exception.class)
                                .getMessage()));


        cartExceptionConfiguration.onException(InvalidCartItemException.class)
                .handled(true)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(404))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .setBody(exchange ->
                        new ErrorResponse("BAD_REQUEST", exchange.getProperty(Exchange.EXCEPTION_CAUGHT,
                                        Exception.class)
                                .getMessage()));

        cartExceptionConfiguration.onException(BeanValidationException.class)
                .handled(true)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .setBody(exchange -> new ErrorResponse("VALIDATION_ERROR",
                        exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class).getMessage()));

        cartExceptionConfiguration.onException(ConstraintViolationException.class)
                .handled(true)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .setBody(exchange -> new ErrorResponse("VALIDATION_ERROR",
                        exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class).getMessage()));

        cartExceptionConfiguration.onException(JsonParseException.class)
                .handled(true)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .setBody(exchange -> new ErrorResponse("INVALID_JSON",
                        exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class).getMessage()));

        cartExceptionConfiguration.onException(Exception.class)
                .handled(true)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(500))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .setBody(exchange -> new ErrorResponse("INTERNAL_ERROR",
                        "Internal server error"));
    }
}

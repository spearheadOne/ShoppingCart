package org.abondar.experimental.shoppingcart.product;

import com.fasterxml.jackson.core.JsonParseException;
import jakarta.validation.ConstraintViolationException;
import org.abondar.experimental.shoppingcart.exception.ErrorResponse;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteConfigurationBuilder;
import org.apache.camel.component.bean.validator.BeanValidationException;
import org.springframework.stereotype.Component;

@Component
public class ProductExceptionHandler extends RouteConfigurationBuilder {

    @Override
    public void configuration() {
        var productExceptionConfiguration = routeConfiguration();

        productExceptionConfiguration.onException(ProductNotFoundException.class)
                .handled(true)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(404))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .setBody(exchange -> error(exchange, "PRODUCT_NOT_FOUND"));

        productExceptionConfiguration.onException(ProductPaginationException.class)
                .handled(true)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .setBody(exchange -> error(exchange, "BAD_REQUEST"));

        productExceptionConfiguration.onException(BeanValidationException.class, ConstraintViolationException.class)
                .handled(true)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .setBody(exchange -> error(exchange, "VALIDATION_ERROR"));

        productExceptionConfiguration.onException(JsonParseException.class)
                .handled(true)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .setBody(exchange -> error(exchange, "INVALID_JSON"));

        productExceptionConfiguration.onException(Exception.class)
                .handled(true)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(500))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .setBody(constant(new ErrorResponse("INTERNAL_ERROR", "Internal server error")));
    }

    private ErrorResponse error(Exchange exchange, String code) {
        var exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
        return new ErrorResponse(code, exception.getMessage());
    }
}

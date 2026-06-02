package org.abondar.experimental.shoppingcart.exception;

public record ErrorResponse(
        String code,
        String message
) {

}
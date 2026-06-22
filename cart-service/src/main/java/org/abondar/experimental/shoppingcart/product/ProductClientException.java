package org.abondar.experimental.shoppingcart.product;

import lombok.Getter;

@Getter
public class ProductClientException extends RuntimeException {

    private final int status;
    private final String code;

    public ProductClientException(int status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }
}

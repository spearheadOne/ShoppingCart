package org.abondar.experimental.shoppingcart.product;

public class ProductPaginationException extends RuntimeException{
    public ProductPaginationException() {
        super("Wrong pagination params");
    }
}

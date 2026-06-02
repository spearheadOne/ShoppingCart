package org.abondar.experimental.shoppingcart.route;

import org.abondar.experimental.shoppingcart.model.CartProduct;
import org.abondar.experimental.shoppingcart.model.CartProductPostResponse;
import org.abondar.experimental.shoppingcart.model.CartProductRequest;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestParamType;
import org.springframework.stereotype.Component;

@Component
public class ProductRestRoute extends RouteBuilder {
  @Override
  public void configure(){
    rest()
        .path("/product")
            .consumes("application/json")
            .produces("application/json")

        .post()
        .apiDocs(true)
            .type(CartProductRequest.class)
            .outType(CartProductPostResponse.class)
            .to("direct:post")

        .get("/{id}")
            .apiDocs(true)
            .outType(CartProduct.class)
            .to("direct:getById")

        .get()
            .apiDocs(true)
            .param()
            .name("limit")
            .type(RestParamType.query)
            .endParam()
            .outType(CartProduct.class)
            .to("direct:getByLimit");

  }
}

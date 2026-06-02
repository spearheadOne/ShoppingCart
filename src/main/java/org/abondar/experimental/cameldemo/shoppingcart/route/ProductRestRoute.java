package org.abondar.experimental.cameldemo.shoppingcart.route;

import org.abondar.experimental.cameldemo.shoppingcart.model.CartProduct;
import org.abondar.experimental.cameldemo.shoppingcart.model.CartProductPostResponse;
import org.abondar.experimental.cameldemo.shoppingcart.model.CartProductRequest;
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
        .to("log:org.abondar.experimental.cameldemo.shoppingcart.route?level=INFO")
            .to("direct:post")

        .get("/{id}")
            .apiDocs(true)
            .outType(CartProduct.class)
            .to("log:org.abondar.experimental.cameldemo.shoppingcart.route?level=INFO")
            .to("direct:getById")

        .get()
            .apiDocs(true)
            .param()
            .name("limit")
            .type(RestParamType.query)
            .endParam()
            .outType(CartProduct.class)
            .to("log:org.abondar.experimental.cameldemo.shoppingcart.route?level=INFO")
            .to("direct:getByLimit");

  }
}

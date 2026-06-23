package org.abondar.experimental.shoppingcart.product;

import org.abondar.experimental.shoppingcart.api.ProductListResponse;
import org.abondar.experimental.shoppingcart.api.ProductResponse;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestParamType;
import org.springframework.stereotype.Component;


@Component
public class ProductRestRoute extends RouteBuilder {
    @Override
    public void configure() {
        rest("/v1/products")
                .consumes("application/json")
                .produces("application/json")

                .get("/{id}")
                .description("Get product by id")
                .apiDocs(true)
                .outType(ProductResponse.class)
                .param()
                .name("id")
                .type(RestParamType.path)
                .description("Product identifier")
                .required(true)
                .endParam()
                .responseMessage()
                .code(200)
                .message("Product found")
                .endResponseMessage()
                .responseMessage()
                .code(404)
                .message("Product not found")
                .endResponseMessage()
                .responseMessage()
                .code(500)
                .message("Internal server error")
                .endResponseMessage()
                .to("direct:getProductById")

                .get()
                .description("Get paginated list of products")
                .apiDocs(true)
                .param()
                .name("limit")
                .type(RestParamType.query)
                .description("Maximum number of products to return(max 100)")
                .required(true)
                .defaultValue("10")
                .dataType("integer")
                .endParam()
                .param()
                .name("offset")
                .type(RestParamType.query)
                .description("Number of products to skip")
                .required(true)
                .defaultValue("0")
                .dataType("integer")
                .endParam()
                .outType(ProductListResponse.class)
                .responseMessage()
                .code(200)
                .message("Products fetched")
                .endResponseMessage()
                .responseMessage()
                .code(400)
                .message("Invalid pagination parameters")
                .endResponseMessage()
                .responseMessage()
                .code(500)
                .message("Internal server error")
                .endResponseMessage()
                .to("direct:getProducts");
    }
}

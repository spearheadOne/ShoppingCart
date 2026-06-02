package org.abondar.experimental.shoppingcart.product;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestParamType;
import org.springframework.stereotype.Component;

@Component
public class ProductRestRoute extends RouteBuilder {
    @Override
    public void configure() {
        rest()
                .path("/v1/products")
                .consumes("application/json")
                .produces("application/json")

                .post()
                .apiDocs(true)
                .type(ProductCreateUpdateRequest.class)
                .outType(ProductResponse.class)
                .to("direct:createProduct")

                .put("/{id}")
                .apiDocs(true)
                .type(ProductCreateUpdateRequest.class)
                .outType(ProductResponse.class)
                .to("direct:updateProduct")

                .get("/{id}")
                .apiDocs(true)
                .outType(ProductResponse.class)
                .to("direct:getProductById")

                .get()
                .apiDocs(true)

                .param()
                .name("limit")
                .type(RestParamType.query)
                .endParam()

                .param()
                .name("offset")
                .type(RestParamType.query)
                .endParam()

                .outType(ProductListResponse.class)
                .to("direct:getProducts")

                .delete("/{id}")
                .apiDocs(true)
                .to("direct:deleteProduct");

    }
}

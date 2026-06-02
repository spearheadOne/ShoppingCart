package org.abondar.experimental.shoppingcart.product;

import lombok.AllArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.http.base.HttpOperationFailedException;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class MongoRoute extends RouteBuilder {

    //todo: remove or save to mongo properly

    @Override
    public void configure() {

        onException(HttpOperationFailedException.class)
                .handled(true)
                .to("log:org.abondar.experimental.cameldemo.shoppingcart.route?level=ERROR");

        from("direct:post")
                .routeId("postRoute")
                .log("Posting product to mongo");

        from("direct:put")
                .routeId("putRoute")
                        .log("Putting product to mongo");

        from("direct:getById")
                .routeId("getByIdRoute")
                .log("Getting product by id");

        from("direct:getProducts")
                .routeId("getByLimitRoute")
                .log("Getting products by limit");


        from("direct:getItems")
                .routeId("getItemsRoute")
                .log("Getting items from mongo");

        from("direct:delete")
                .routeId("deleteRoute")
                .log("Deleting product from mongo");
    }
}

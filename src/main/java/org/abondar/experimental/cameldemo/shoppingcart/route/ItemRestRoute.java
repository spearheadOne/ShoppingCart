package org.abondar.experimental.cameldemo.shoppingcart.route;

import org.abondar.experimental.cameldemo.shoppingcart.model.CartItems;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class ItemRestRoute extends RouteBuilder {
    @Override
    public void configure() {
           rest()
                   .path("/item")
                   .consumes("application/json")
                   .produces("application/json")

                   .get()
                   .apiDocs(true)
                   .outType(CartItems.class)
                   .to("log:org.abondar.experimental.cameldemo.shoppingcart.route?level=INFO")
                   .to("direct:getItems");
    }
}

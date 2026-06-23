package org.abondar.experimental.shoppingcart.order;

import org.abondar.experimental.shoppingcart.api.CreateOrderRequest;
import org.abondar.experimental.shoppingcart.api.OrderResponse;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class OrderRestRoute  extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        rest("/v1/orders")
                .consumes("application/json")
                .produces("application/json")

                .post()
                .description("Create a new shopping cart")
                .consumes("application/json")
                .apiDocs(true)
                .type(CreateOrderRequest.class)
                .outType(OrderResponse.class)
                .responseMessage()
                .code(201)
                .message("Order created")
                .endResponseMessage()
                .responseMessage()
                .code(400)
                .message("Invalid order request")
                .endResponseMessage()
                .responseMessage()
                .code(404)
                .message("Product not found")
                .endResponseMessage()
                .responseMessage()
                .code(500)
                .message("Internal server error")
                .endResponseMessage()
                .to("direct:createOrder");
    }
}

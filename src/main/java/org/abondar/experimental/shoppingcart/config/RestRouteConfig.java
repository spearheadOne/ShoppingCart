package org.abondar.experimental.shoppingcart.config;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.stereotype.Component;

@Component
public class RestRouteConfig extends RouteBuilder {

    @Override
    public void configure() {

        restConfiguration()
                .enableCORS(true)
                .contextPath("/api")
                .apiProperty("api.title", "Shopping Cart backend")
                .apiProperty("api.version", "v1")
                .component("servlet")
                .bindingMode(RestBindingMode.auto);
    }
}

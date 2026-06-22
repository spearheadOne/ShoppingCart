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
                .apiProperty("api.title", "Product service")
                .apiProperty("api.version", "v1")
                .component("platform-http")
                .clientRequestValidation(true)
                .bindingMode(RestBindingMode.json);
    }
}

package org.abondar.experimental.cameldemo.shoppingcart.config;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.stereotype.Component;

@Component
public class RestRouteConfig extends RouteBuilder {

  @Override
  public void configure() {

    restConfiguration()
        .host("localhost")
        .port(8080)
        .enableCORS(true)
        .contextPath("/cart")
        .apiContextPath("/doc")
        .apiProperty("api.title", "Shopping Cart backend")
        .apiProperty("api.version", "v1")
        .component("servlet")
        .bindingMode(RestBindingMode.auto);
  }
}

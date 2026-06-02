package org.abondar.experimental.cameldemo.shoppingcart.route;

import org.abondar.experimental.cameldemo.shoppingcart.processor.ProductProcessor;
import org.abondar.experimental.cameldemo.shoppingcart.transform.ResponseBodyTransformer;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.http.base.HttpOperationFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FirebaseRoute extends RouteBuilder {

  @Value("${firebase.url}")
  private String firebaseUrl;

  @Value("${firebase.products}")
  private String prodcutsJson;

  @Value("${firebase.cartItems}")
  private String cartItemsJson;

  private final ProductProcessor productProcessor;

  private final ResponseBodyTransformer bodyTransformer;

  @Autowired
  public FirebaseRoute(ProductProcessor productProcessor, ResponseBodyTransformer bodyTransformer) {
    this.productProcessor = productProcessor;
    this.bodyTransformer = bodyTransformer;
  }

  @Override
  public void configure() {

    onException(HttpOperationFailedException.class)
        .handled(true)
        .to("log:org.abondar.experimental.cameldemo.shoppingcart.route?level=ERROR");

    from("direct:post")
            .routeId("postRoute")
        .process(productProcessor)
        .setHeader(Exchange.HTTP_METHOD, constant("PATCH"))
        .to(firebaseUrl + prodcutsJson)
        .transform()
        .body(bodyTransformer::transformBody);

    from("direct:getById")
            .routeId("getByIdRoute")
        .removeHeader(Exchange.HTTP_URI)
        .setHeader(Exchange.HTTP_METHOD, constant("GET"))
        .toD(firebaseUrl + prodcutsJson + "?orderBy=\"$key\"&equalTo=\"${header.id}\"")
        .transform()
        .body(bodyTransformer::transformBody);

    from("direct:getByLimit")
            .routeId("getByLimitRoute")
        .removeHeader(Exchange.HTTP_URI)
        .setHeader(Exchange.HTTP_METHOD, constant("GET"))
        .toD(firebaseUrl + prodcutsJson + "?orderBy=\"$key\"&limitToFirst=${header.limit}")
        .transform()
        .body(bodyTransformer::transformBody);

    from("direct:getItems")
            .routeId("getItemsRoute")
        .removeHeader(Exchange.HTTP_URI)
        .setHeader(Exchange.HTTP_METHOD, constant("GET"))
        .to(firebaseUrl + cartItemsJson)
        .transform()
        .body(bodyTransformer::transformBody);
  }
}

package org.abondar.experimental.shoppingcart.product;

import org.abondar.experimental.shoppingcart.api.OrderResponse;
import org.abondar.experimental.shoppingcart.api.ProductResponse;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;

@Component
public class ProductClientRoute extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        from("direct:getProduct")
                .routeId("getProduct")
                .removeHeaders("CamelHttp*")
                .setHeader(Exchange.HTTP_METHOD, constant("GET"))
                .setBody(constant((Object) null))
                .toD(
                        "{{services.product.base-url}}"
                                + "/v1/products/"
                                + "${exchangeProperty.productId}"
                                + "?throwExceptionOnFailure=false"
                )
                .removeHeaders("Access-Control-*")
                .choice()
                .when(header(Exchange.HTTP_RESPONSE_CODE).isEqualTo(404))
                .process(exchange -> {
                    var productId = exchange.getProperty("productId", String.class);
                    throw new ProductClientException(404, "PRODUCT_NOT_FOUND",
                            "Product with id %s not found".formatted(productId));
                })
                .when(header(Exchange.HTTP_RESPONSE_CODE).isGreaterThanOrEqualTo(500))
                .throwException(new ProductClientException(502, "PRODUCT_SERVICE_UNAVAILABLE",
                        "Product service is unavailable"))
                .when(header(Exchange.HTTP_RESPONSE_CODE).isGreaterThanOrEqualTo(400))
                .throwException(new ProductClientException(502, "PRODUCT_SERVICE_ERROR",
                        "Product service rejected the request"))
                .end()
                .unmarshal().json(JsonLibrary.Jackson, ProductResponse.class);

        from("direct:createOrder")
                .routeId("createOrder")
                .removeHeaders("CamelHttp*")
                .setHeader(Exchange.HTTP_METHOD,constant("POST"))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .marshal().json(JsonLibrary.Jackson)
                .convertBodyTo(String.class)
                .toD("{{services.product.base-url}}"
                        + "/v1/orders"
                        + "?throwExceptionOnFailure=false"
                )
                .removeHeaders("Access-Control-*")
                .choice()
                .when(header(Exchange.HTTP_RESPONSE_CODE).isEqualTo(404))
                .throwException(new ProductClientException(404, "PRODUCT_NOT_FOUND",
                        "Product referenced by cart was not found"))
                .when(header(Exchange.HTTP_RESPONSE_CODE).isGreaterThanOrEqualTo(500))
                .throwException(new ProductClientException(502, "PRODUCT_SERVICE_UNAVAILABLE",
                        "Product service is unavailable"))
                .when(header(Exchange.HTTP_RESPONSE_CODE).isGreaterThanOrEqualTo(400))
                .throwException(new ProductClientException(502, "PRODUCT_SERVICE_ERROR",
                        "Product service rejected the request"))
                .end()
                .unmarshal().json(JsonLibrary.Jackson, OrderResponse.class);


    }
}

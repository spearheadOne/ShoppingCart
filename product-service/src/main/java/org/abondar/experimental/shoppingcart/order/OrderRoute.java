package org.abondar.experimental.shoppingcart.order;

import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class OrderRoute extends RouteBuilder {

    static final String PRODUCT_MAPPER =
            "org.abondar.experimental.shoppingcart.product.ProductMapper";
    static final String ORDER_MAPPER =
            "org.abondar.experimental.shoppingcart.order.OrderMapper";

    private static final String FIND_PRODUCTS_BY_IDS = "mybatis:" + PRODUCT_MAPPER
            + ".findProductsByIds"
            + "?statementType=SelectList";

    private static final String INSERT_ORDER = "mybatis:" + ORDER_MAPPER
            + ".insertOrder"
            + "?statementType=Insert";

    private static final String INSERT_ORDER_ITEMS = "mybatis:" + ORDER_MAPPER
            + ".insertOrderItems"
            + "?statementType=Insert";

    private final OrderService orderService;
    private final OrderResponseMapper orderResponseMapper;

    @Override
    public void configure() throws Exception {
              from("direct:createOrder")
                      .routeId("createOrder")
                      .transacted()
                      .to("bean-validator:createOrderRequest")

                      .setProperty("createOrderRequest", body())
                      .bean(orderService, "toProductQueryParams")
                      .to(FIND_PRODUCTS_BY_IDS)
                      .id("findProductsQuery")
                      .bean(orderService, "createOrder(${exchangeProperty.createOrderRequest}, ${body})")
                      .setProperty("createdOrder", body())
                      .to(INSERT_ORDER)
                      .id("insertOrderQuery")
                      .process(exchange -> {
                          var order = exchange.getProperty("createdOrder", Order.class);

                          exchange.getMessage().setBody(Map.of("items", order.items()));
                      })
                      .to(INSERT_ORDER_ITEMS)
                      .id("insertOrderItemsQuery")
                      .setBody(exchangeProperty("createdOrder"))
                      .bean(orderResponseMapper, "toResponse")
                      .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(201));

    }
}

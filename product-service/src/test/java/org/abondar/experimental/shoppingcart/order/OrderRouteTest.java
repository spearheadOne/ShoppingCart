package org.abondar.experimental.shoppingcart.order;

import org.abondar.experimental.shoppingcart.api.CreateOrderItemRequest;
import org.abondar.experimental.shoppingcart.api.CreateOrderRequest;
import org.abondar.experimental.shoppingcart.api.OrderResponse;
import org.abondar.experimental.shoppingcart.product.Product;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.spring.spi.SpringTransactionPolicy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class OrderRouteTest {

    private static final UUID PRODUCT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final String CART_ID = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa";

    private CamelContext camelContext;
    private ProducerTemplate producerTemplate;

    private MockEndpoint findProductsByIds;
    private MockEndpoint insertOrder;
    private MockEndpoint insertOrderItems;


    @BeforeEach
    public void setUp() throws Exception {
        var orderService = new OrderService();
        var responseMapper = new OrderResponseMapper();

        camelContext = new DefaultCamelContext();
        camelContext.getRegistry().bind(
                "PROPAGATION_REQUIRED",
                new SpringTransactionPolicy(new TestTransactionManager())
        );

        camelContext.addRoutes(new OrderRoute(orderService, responseMapper));

        AdviceWith.adviceWith(camelContext, "createOrder",
                advice -> {
                    advice.weaveByToUri("bean-validator:createOrderRequest")
                            .replace()
                            .process(_ -> {
                                // No-op.
                            });

                    advice.weaveById("findProductsQuery")
                            .replace()
                            .to("mock:findProductsByIds");

                    advice.weaveById("insertOrderQuery")
                            .replace()
                            .to("mock:insertOrder");

                    advice.weaveById("insertOrderItemsQuery")
                            .replace()
                            .to("mock:insertOrderItems");
                });

        camelContext.start();

        producerTemplate = camelContext.createProducerTemplate();

        findProductsByIds = camelContext.getEndpoint("mock:findProductsByIds", MockEndpoint.class);
        insertOrder = camelContext.getEndpoint("mock:insertOrder", MockEndpoint.class);
        insertOrderItems = camelContext.getEndpoint("mock:insertOrderItems", MockEndpoint.class);
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (producerTemplate != null) producerTemplate.stop();

        if (camelContext != null) camelContext.stop();
    }

    @Test
    public void createOrder() throws Exception{
        var itemRequest = new CreateOrderItemRequest(PRODUCT_ID.toString(),2);
        var request = new CreateOrderRequest(CART_ID, List.of(itemRequest));
        var product = new Product(PRODUCT_ID, "test", "test", BigDecimal.ONE);

        findProductsByIds.expectedMessageCount(1);
        findProductsByIds.expectedBodiesReceived(Map.of("ids", List.of(PRODUCT_ID.toString())));
        findProductsByIds.whenAnyExchangeReceived(exchange -> exchange.getMessage().setBody(List.of(product)));

        insertOrder.expectedMessageCount(1);
        insertOrder.expectedMessagesMatches(exchange -> exchange.getMessage().getBody() instanceof Order);

        insertOrderItems.expectedMessageCount(1);
        insertOrderItems.expectedMessagesMatches(exchange -> {
            var body = exchange.getMessage().getBody();

            if (!(body instanceof Map<?,?> parameters)) return false;

            var itemsValue = parameters.get("items");
            if (!(itemsValue instanceof List<?> items)) return false;

            return items.size() == 1 && items.getFirst() instanceof OrderItem;
        });

        var response = producerTemplate.requestBody("direct:createOrder", request, OrderResponse.class);
        assertNotNull(response);
        assertNotNull(response.orderId());
        assertEquals(CART_ID, response.cartId());
        assertEquals(OrderStatus.CREATED.toString(), response.status());
        assertEquals(0, new BigDecimal("2.00").compareTo(response.totalPrice()));
        assertEquals(1, response.items().size());
        assertEquals(2, response.itemsTotal());

        var item = response.items().getFirst();
        assertEquals(PRODUCT_ID.toString(),item.productId());
        assertEquals(product.name(),item.name());
        assertEquals(2,item.quantity());
        assertEquals(0, BigDecimal.ONE.compareTo(item.unitPrice()));
        assertEquals(0, new BigDecimal("2.00").compareTo(item.lineTotal()));
    }

    private static class TestTransactionManager extends AbstractPlatformTransactionManager {

        @Override
        protected Object doGetTransaction() {
            return new Object();
        }

        @Override
        protected void doBegin(Object transaction, org.springframework.transaction.TransactionDefinition definition) {
        }

        @Override
        protected void doCommit(DefaultTransactionStatus status) {
        }

        @Override
        protected void doRollback(DefaultTransactionStatus status) {
        }
    }

}

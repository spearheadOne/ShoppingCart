package org.abondar.experimental.shoppingcart.order;

import org.abondar.experimental.shoppingcart.api.CreateOrderItemRequest;
import org.abondar.experimental.shoppingcart.api.CreateOrderRequest;
import org.abondar.experimental.shoppingcart.product.Product;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OrderServiceTest {

    private static final String CART_ID = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa";

    private static final String PRODUCT_ID ="11111111-1111-1111-1111-111111111111";


    private final OrderService service = new OrderService();


    @Test
    public void createOrderTest(){
        var itemReq = new CreateOrderItemRequest(PRODUCT_ID, 2);
        var req = new CreateOrderRequest(CART_ID, List.of(itemReq));
        var product = new Product(UUID.fromString(PRODUCT_ID), "test", "test", BigDecimal.ONE);

        var order = service.createOrder(req,List.of(product));

        assertNotNull(order.id());
        assertEquals(UUID.fromString(CART_ID), order.cartId());
        assertEquals(OrderStatus.CREATED, order.status());
        assertEquals(0, new BigDecimal("2.00").compareTo(order.totalPrice()));
        assertNotNull(order.createdAt());
        assertEquals(1,order.items().size());
    }

    @Test
    public void createQueryParams(){
        var itemReq = new CreateOrderItemRequest(PRODUCT_ID, 2);
        var req = new CreateOrderRequest(CART_ID, List.of(itemReq));

        var res = service.toProductQueryParams(req);
        assertEquals(1,res.size());
        assertTrue(res.containsKey("ids"));
    }

}

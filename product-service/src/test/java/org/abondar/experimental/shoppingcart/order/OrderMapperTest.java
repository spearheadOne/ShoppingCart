package org.abondar.experimental.shoppingcart.order;


import org.abondar.experimental.shoppingcart.ProductServiceApplication;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.liquibase.autoconfigure.LiquibaseAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MybatisTest(properties = """
        spring.datasource.url=jdbc:h2:mem:order_mapper;MODE=PostgreSQL;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false
        """)
@MapperScan(basePackageClasses = OrderMapper.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration(LiquibaseAutoConfiguration.class)
@ContextConfiguration(classes = ProductServiceApplication.class)
public class OrderMapperTest {

    private static final UUID CART_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID PRODUCT_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

    @Autowired
    private OrderMapper mapper;


    @Test
    public void insertFindOrder() {
        var orderId = insertOrder();
        assertNotNull(orderId);

        var order = mapper.findOrderById(orderId);
        assertTrue(order.isPresent());
        assertEquals(orderId, order.get().id());
        assertEquals(OrderStatus.CREATED, order.get().status());
    }

    @Test
    public void findOrderByCartId() {
        var orderId = insertOrder();
        assertNotNull(orderId);

        var order = mapper.findOrderByCartId(CART_ID);
        assertTrue(order.isPresent());
        assertEquals(orderId, order.get().id());
    }

    @Test
    public void insertFindOrderItems() {
        var orderId = insertOrder();
        var item = new OrderItem(orderId, PRODUCT_ID, "test",
                "test", BigDecimal.ONE, 1, BigDecimal.ONE);

       var res = mapper.insertOrderItems(List.of(item));
       assertTrue(res>0);

       var items = mapper.findOrderItems(orderId);
       assertEquals(1, items.size());
       assertEquals(orderId, items.getFirst().orderId());
       assertEquals(item.productId(), items.getFirst().productId());
    }


    private UUID insertOrder() {
        var order = new Order(UUID.randomUUID(), CART_ID, OrderStatus.CREATED, BigDecimal.ONE, Instant.now(), List.of());
        mapper.insertOrder(order);
        return order.id();
    }

}

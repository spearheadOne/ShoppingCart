package org.abondar.experimental.shoppingcart.cart;


import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(
        classes = {
                CartRepository.class,
                RedisConfig.class
        }
)
@ImportAutoConfiguration(DataRedisAutoConfiguration.class)
public class CartRepositoryTest {

    @Container
    @ServiceConnection
    static RedisContainer redis = new RedisContainer("redis:7-alpine");

    @Autowired
    private CartRepository cartRepository;


    @Test
    public void saveAndFindCart(){
        var cartId = UUID.randomUUID();

        var item = new CartItem(UUID.randomUUID(), "test", "test", BigDecimal.ONE, 2);
        var cart = new Cart(cartId, List.of(item));

        cartRepository.save(cart);

        var result = cartRepository.findById(cartId);

        assertTrue(result.isPresent());
        assertEquals(cartId, result.get().id());
    }


    @Test
    public void findByIdNotFound(){
        var result = cartRepository.findById(UUID.randomUUID());

        assertTrue(result.isEmpty());
    }

    @Test
    public void saveAndOverwriteCart(){
        var cartId = UUID.randomUUID();

        var item = new CartItem(UUID.randomUUID(), "test", "test", BigDecimal.ONE, 2);
        var cart = new Cart(cartId, List.of(item));
        cartRepository.save(cart);

        var item1 = new CartItem(UUID.randomUUID(), "test1", "test", BigDecimal.ONE, 6);
        var cart1 = new Cart(cartId, List.of(item,item1));
        cartRepository.save(cart1);

        var result = cartRepository.findById(cartId);

        assertTrue(result.isPresent());
        assertEquals(cartId, result.get().id());
        assertEquals(2, result.get().items().size());
    }

    @Test
    public void deleteCart(){
        var cartId = UUID.randomUUID();

        var cart = new Cart(cartId, List.of());
        cartRepository.save(cart);

        assertTrue(cartRepository.deleteById(cartId));
        assertTrue(cartRepository.findById(cartId).isEmpty());
    }

    @Test
    public void deleteCartNotFound(){
        assertFalse(cartRepository.deleteById(UUID.randomUUID()));
    }

}

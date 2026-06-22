package org.abondar.experimental.shoppingcart.cart;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;


@Repository
@RequiredArgsConstructor
public class CartRepository {

    private static final String CART_PREFIX = "cart:";
    private static final Duration CART_TTL = Duration.ofHours(24);

    private final RedisTemplate<String, Cart> redisTemplate;

    public void save(Cart cart) {
        redisTemplate.opsForValue()
                .set(key(cart.id()),cart,CART_TTL);
    }


    public Optional<Cart> findById(UUID id) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(key(id)));
    }

    public boolean deleteById(UUID id) {
        return Boolean.TRUE.equals(redisTemplate.delete(key(id)));
    }


    private String key(UUID id) {
        return CART_PREFIX + id;
    }
}

package org.abondar.experimental.shoppingcart.cart;

import org.abondar.experimental.shoppingcart.api.ProductResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CartServiceTest {

    private static final UUID CART_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

    private static final UUID PRODUCT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private static final UUID SECOND_PRODUCT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");


    private final CartService cartService = new CartService();


    @Test
    public void createCart() {
        var response = cartService.createCart();

        assertNotNull(response);
        assertTrue(response.items().isEmpty());
    }


    @Test
    public void addNewItemToCart() {
        var cart = new Cart(CART_ID, List.of());
        var request = new CartItemAddRequest(PRODUCT_ID.toString(), 2);
        var result = cartService.addCartItem(cart, request, product());

        assertEquals(1, result.items().size());
        assertEquals(2, result.items().getFirst().quantity());
    }

    @Test
    public void addItemToCartIncreaseQuantity() {
        var cart = new Cart(CART_ID, List.of(cartItem()));
        var request = new CartItemAddRequest(PRODUCT_ID.toString(), 5);

        var result = cartService.addCartItem(cart, request, product());

        assertEquals(1, result.items().size());
        assertEquals(7, result.items().getFirst().quantity());
        assertEquals(0, result.items().getFirst().lineTotal().compareTo(new BigDecimal("70.00")));
    }

    @Test
    public void updateCartQuantity() {
        var cart = new Cart(CART_ID, List.of(cartItem()));
        var request = new CartItemUpdateQuantityRequest(3);

        var response = cartService.updateCartQuantity(cart, PRODUCT_ID.toString(), request);

        assertEquals(1, response.items().size());
        assertEquals(3, response.items().getFirst().quantity());
        assertEquals(0, response.items().getFirst().lineTotal().compareTo(new BigDecimal("30.00")));
    }


    @Test
    public void deleteCartItem() {
        var firstItem = cartItem();
        var secondItem = new CartItem(SECOND_PRODUCT_ID,
                "test", "test", new BigDecimal("10.00"), 1);
        var cart = new Cart(CART_ID, List.of(firstItem, secondItem));

        var response = cartService.deleteCartItem(cart, PRODUCT_ID.toString());

        assertEquals(1, response.items().size());
        assertEquals(SECOND_PRODUCT_ID, response.items().getFirst().productId());
        assertEquals(1, response.items().size());
    }


    private CartItem cartItem() {
        return new CartItem(CartServiceTest.PRODUCT_ID,
                "test", "test", new BigDecimal("10.00"), 2);
    }

    private ProductResponse product() {
        return new ProductResponse(PRODUCT_ID, "test", "test", new BigDecimal("10.00"));
    }
}

package org.abondar.experimental.shoppingcart.cart;

import org.abondar.experimental.shoppingcart.product.Product;
import org.abondar.experimental.shoppingcart.product.ProductMapper;
import org.abondar.experimental.shoppingcart.product.ProductNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CartServiceTest {

    private static final UUID CART_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

    private static final UUID PRODUCT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private static final UUID SECOND_PRODUCT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private CartService cartService;

    @Test
    public void createCart() {
        doNothing().when(cartRepository).save(any(Cart.class));

        var response = cartService.createCart();

        assertNotNull(response.cartId());
        assertTrue(response.items().isEmpty());
        assertEquals(0, response.itemsTotal());
        assertEquals(0, response.totalPrice().compareTo(BigDecimal.ZERO));

        verify(cartRepository, times(1)).save(any(Cart.class));
        verifyNoInteractions(productMapper);
    }

    @Test
    public void getCart() {
        var cart = new Cart(CART_ID, List.of(cartItem()));

        when(cartRepository.findById(CART_ID)).thenReturn(Optional.of(cart));

        var response = cartService.getCart(CART_ID.toString());

        assertEquals(CART_ID.toString(), response.cartId());
        assertEquals(1, response.items().size());
        assertEquals(2, response.itemsTotal());
        assertEquals(0, response.totalPrice().compareTo(new BigDecimal("20.00")));
    }

    @Test
    public void getCartNotFound() {
        when(cartRepository.findById(CART_ID)).thenReturn(Optional.empty());

        assertThrows(CartNotFoundException.class, () -> cartService.getCart(CART_ID.toString()));
        verify(cartRepository, times(1)).findById(CART_ID);
        verifyNoInteractions(productMapper);
    }


    @Test
    public void addNewItemToCart() {
        var cart = new Cart(CART_ID, List.of());
        var product = new Product(PRODUCT_ID, "test", "test", new BigDecimal("10.00"));

        var request = new CartItemAddRequest(PRODUCT_ID.toString(), 2);

        when(cartRepository.findById(CART_ID)).thenReturn(Optional.of(cart));
        when(productMapper.findProductById(PRODUCT_ID)).thenReturn(Optional.of(product));

        var response = cartService.addCartItem(CART_ID.toString(), request);
        assertEquals(1, response.items().size());
        assertEquals(2, response.itemsTotal());
        assertEquals(0, response.totalPrice().compareTo(new BigDecimal("20.00")));

        verify(cartRepository, times(1)).findById(CART_ID);
        verify(productMapper, times(1)).findProductById(PRODUCT_ID);
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    public void addItemToCartIncreaseQuantity() {
        var existingItem = cartItem();
        var cart = new Cart(CART_ID, List.of(existingItem));

        var request = new CartItemAddRequest(PRODUCT_ID.toString(), 5);

        when(cartRepository.findById(CART_ID)).thenReturn(Optional.of(cart));

        var response = cartService.addCartItem(CART_ID.toString(), request);

        assertEquals(1, response.items().size());
        assertEquals(7, response.itemsTotal());
        assertEquals(0, response.totalPrice().compareTo(new BigDecimal("70.00")));

        verify(cartRepository, times(1)).findById(CART_ID);
        verify(cartRepository, times(1)).save(any(Cart.class));
        verifyNoInteractions(productMapper);
    }

    @Test
    public void addCartItemProductNotFound() {
        var cart = new Cart(CART_ID, List.of());
        var request = new CartItemAddRequest(PRODUCT_ID.toString(), 1);

        when(cartRepository.findById(CART_ID)).thenReturn(Optional.of(cart));
        when(productMapper.findProductById(PRODUCT_ID)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> cartService.addCartItem(CART_ID.toString(), request));

        verify(cartRepository, times(1)).findById(CART_ID);
        verify(productMapper, times(1)).findProductById(PRODUCT_ID);
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    public void addCartItemCartNotFound() {
        var request = new CartItemAddRequest(PRODUCT_ID.toString(), 1);

        when(cartRepository.findById(CART_ID)).thenReturn(Optional.empty());

        assertThrows(CartNotFoundException.class, () -> cartService.addCartItem(CART_ID.toString(), request));

        verify(cartRepository, times(1)).findById(CART_ID);
        verifyNoInteractions(productMapper);
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    public void updateCartQuantity() {
        var cart = new Cart(CART_ID, List.of(cartItem()));
        var request = new CartItemUpdateQuantityRequest(3);

        when(cartRepository.findById(CART_ID)).thenReturn(Optional.of(cart));

        var response = cartService.updateCartQuantity(CART_ID.toString(), PRODUCT_ID.toString(), request);

        assertEquals(1, response.items().size());
        assertEquals(3, response.itemsTotal());
        assertEquals(3, response.items().getFirst().quantity());
        assertEquals(0, response.totalPrice().compareTo(new BigDecimal("30.00")));
    }

    @Test
    public void updateCartQuantityNotFound() {
        var cart = new Cart(CART_ID, List.of(cartItem()));
        var request = new CartItemUpdateQuantityRequest(3);

        when(cartRepository.findById(CART_ID)).thenReturn(Optional.of(cart));

        assertThrows(CartItemNotFoundException.class, () ->
                cartService.updateCartQuantity(CART_ID.toString(), SECOND_PRODUCT_ID.toString(), request));

        verify(cartRepository, times(1)).findById(CART_ID);
        verify(cartRepository, never()).save(any());
        verifyNoInteractions(productMapper);
    }

    @Test
    public void deleteCart() {
        when(cartRepository.deleteById(CART_ID)).thenReturn(true);

        cartService.deleteCart(CART_ID.toString());

        verify(cartRepository, times(1)).deleteById(CART_ID);
        verifyNoInteractions(productMapper);
    }

    @Test
    public void deleteCartNotFound() {
        when(cartRepository.deleteById(CART_ID)).thenReturn(false);

        assertThrows(CartNotFoundException.class, () -> cartService.deleteCart(CART_ID.toString()));

        verify(cartRepository, times(1)).deleteById(CART_ID);
        verifyNoInteractions(productMapper);
    }

    @Test
    public void deleteCartItem() {
        var firstItem = cartItem();

        var secondItem = new CartItem(SECOND_PRODUCT_ID,
                "test", "test", new BigDecimal("10.00"), 1);

        var cart = new Cart(CART_ID, List.of(firstItem, secondItem));

        when(cartRepository.findById(CART_ID)).thenReturn(Optional.of(cart));

        var response = cartService.deleteCartItem(CART_ID.toString(), PRODUCT_ID.toString());

        assertEquals(1, response.items().size());
        assertEquals(SECOND_PRODUCT_ID.toString(), response.items().getFirst().productId());
        assertEquals(1, response.itemsTotal());

        verify(cartRepository, times(1)).findById(CART_ID);
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    public void deleteCartItemNotFound() {
        var cart = new Cart(CART_ID, List.of(cartItem()));

        when(cartRepository.findById(CART_ID)).thenReturn(Optional.of(cart));

        assertThrows(CartItemNotFoundException.class, () ->
                cartService.deleteCartItem(CART_ID.toString(), SECOND_PRODUCT_ID.toString()));

        verify(cartRepository, times(1)).findById(CART_ID);
        verify(cartRepository, never()).save(any(Cart.class));
    }

    private CartItem cartItem() {
        return new CartItem(CartServiceTest.PRODUCT_ID,
                "test", "test", new BigDecimal("10.00"), 2);

    }
}

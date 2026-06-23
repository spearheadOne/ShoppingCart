package org.abondar.experimental.shoppingcart.cart;

import org.abondar.experimental.shoppingcart.api.ProductResponse;
import org.abondar.experimental.shoppingcart.util.UuidUtil;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.stream.IntStream;

@Service
public class CartService {

    public Cart createCart() {
        return new Cart(UUID.randomUUID(), List.of());
    }


    public Cart addCartItem(Cart cart, CartItemAddRequest request, ProductResponse productItem) {
        var productId = UuidUtil.parseUuid(request.productId());
        var items = copyItems(cart);

        var existingItemIndex = findFromIndex(items, productId);
        if (existingItemIndex.isPresent()) {
            int index = existingItemIndex.getAsInt();
            var existingItem = items.get(index);

            items.set(index, withQuantity(existingItem, existingItem.quantity() + request.quantity()));
        } else {
            items.add(toCartItem(productItem, request.quantity()));
        }

        return new Cart(cart.id(), List.copyOf(items));
    }

    public Cart updateCartQuantity(Cart cart, String productId, CartItemUpdateQuantityRequest request) {
        var parsedProductId = UuidUtil.parseUuid(productId);
        var items = copyItems(cart);

        var index = findItemIndexOrThrow(items, parsedProductId);

        items.set(index, withQuantity(items.get(index), request.quantity()));

        return new Cart(cart.id(), List.copyOf(items));
    }

    public Cart deleteCartItem(Cart cart, String productId) {
        var parsedProductId = UuidUtil.parseUuid(productId);
        var items = copyItems(cart);

        if (!items.removeIf(item -> item.productId().equals(parsedProductId))) {
            throw new CartItemNotFoundException(parsedProductId);
        }

        return new Cart(cart.id(), List.copyOf(items));
    }


    private OptionalInt findFromIndex(List<CartItem> items, UUID productId) {
        return IntStream.range(0, items.size())
                .filter(i -> items.get(i).productId().equals(productId))
                .findFirst();
    }

    private int findItemIndexOrThrow(List<CartItem> items, UUID productId) {
        return findFromIndex(items, productId)
                .orElseThrow(() -> new CartItemNotFoundException(productId));
    }

    private List<CartItem> copyItems(Cart cart) {
        return new ArrayList<>(cart.items());
    }

    private CartItem toCartItem(ProductResponse product, int quantity) {
        return new CartItem(
                product.id(),
                product.name(),
                product.imgUrl(),
                product.price(),
                quantity
        );
    }

    private CartItem withQuantity(CartItem item, int quantity) {
        return new CartItem(
                item.productId(),
                item.name(),
                item.imgUrl(),
                item.unitPrice(),
                quantity
        );
    }


}

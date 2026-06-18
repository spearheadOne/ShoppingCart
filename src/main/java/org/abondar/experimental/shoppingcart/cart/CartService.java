package org.abondar.experimental.shoppingcart.cart;

import lombok.RequiredArgsConstructor;
import org.abondar.experimental.shoppingcart.product.Product;
import org.abondar.experimental.shoppingcart.product.ProductMapper;
import org.abondar.experimental.shoppingcart.product.ProductNotFoundException;
import org.abondar.experimental.shoppingcart.util.UuidUtil;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ProductMapper productMapper;

    public CartResponse createCart() {
        var cart = new Cart(UUID.randomUUID(), List.of());

        cartRepository.save(cart);

        return toResponse(cart);
    }

    public CartResponse getCart(String id) {
        var cartId = UuidUtil.parseUuid(id);

        return toResponse(getCartModel(cartId));
    }

    public CartResponse addCartItem(String id, CartItemAddRequest request) {
        var cartId = UuidUtil.parseUuid(id);
        var productId = UuidUtil.parseUuid(request.productId());

        var cart = getCartModel(cartId);
        var items = copyItems(cart);

        var existingItemIndex = findFromIndex(items, productId);
        if (existingItemIndex.isPresent()) {
            int index = existingItemIndex.getAsInt();
            items.set(index, withQuantity(items.get(index), items.get(index).quantity() + request.quantity()));
        } else {
            items.add(toCartItem(getProduct(productId), request.quantity()));
        }

        return updateCart(cart, items);
    }

    public CartResponse updateCartQuantity(String id, String productId, CartItemUpdateQuantityRequest request) {
        var cartId = UuidUtil.parseUuid(id);
        var parsedProductId = UuidUtil.parseUuid(productId);

        var cart = getCartModel(cartId);
        var items = copyItems(cart);

        var index = findItemIndexOrThrow(items, parsedProductId);

        items.set(index, withQuantity(items.get(index), request.quantity()));

        return updateCart(cart, items);
    }

    public void deleteCart(String id) {
        var cartId = UuidUtil.parseUuid(id);

        if (!cartRepository.deleteById(cartId)) {
            throw new CartNotFoundException(cartId);
        }
    }

    public CartResponse deleteCartItem(String id, String productId) {
        var cartId = UuidUtil.parseUuid(id);
        var parsedProductId = UuidUtil.parseUuid(productId);

        var cart = getCartModel(cartId);
        var items = copyItems(cart);

        if (!items.removeIf(item -> item.productId().equals(parsedProductId))) {
            throw new CartItemNotFoundException(parsedProductId);
        }

        return updateCart(cart, items);
    }

    private CartResponse updateCart(Cart cart, List<CartItem> items) {
        var updatedCart = new Cart(cart.id(), List.copyOf(items));
        cartRepository.save(updatedCart);
        return toResponse(updatedCart);
    }


    private Cart getCartModel(UUID cartId) {
        return cartRepository.findById(cartId)
                .orElseThrow(() -> new CartNotFoundException(cartId));
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

    private Product getProduct(UUID productId) {
        return productMapper.findProductById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    private CartItem toCartItem(Product product, int quantity) {
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

    private CartResponse toResponse(Cart cart) {
        var items = cart.items().stream()
                .map(this::toResponse)
                .toList();

        int itemsTotal = items.stream()
                .mapToInt(CartItemResponse::quantity)
                .sum();

        var totalPrice = items.stream()
                .map(CartItemResponse::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(cart.id().toString(), items, itemsTotal, totalPrice);
    }

    private CartItemResponse toResponse(CartItem item) {
        return new CartItemResponse(
                item.productId().toString(),
                item.name(),
                item.imgUrl(),
                item.unitPrice(),
                item.quantity(),
                item.lineTotal()
        );
    }

}

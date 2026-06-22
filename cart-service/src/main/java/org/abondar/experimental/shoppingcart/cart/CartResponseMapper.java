package org.abondar.experimental.shoppingcart.cart;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CartResponseMapper {

    public CartResponse toResponse(Cart cart) {
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

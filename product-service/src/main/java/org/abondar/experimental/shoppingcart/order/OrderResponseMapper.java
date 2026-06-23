package org.abondar.experimental.shoppingcart.order;

import org.abondar.experimental.shoppingcart.api.OrderItemResponse;
import org.abondar.experimental.shoppingcart.api.OrderResponse;
import org.springframework.stereotype.Component;

@Component
public class OrderResponseMapper {

    public OrderResponse toResponse(Order order) {
        var items = order.items()
                .stream()
                .map(this::toResponse)
                .toList();

        var itemsTotal = items.stream()
                .mapToInt(OrderItemResponse::quantity)
                .sum();

        return new OrderResponse(
                order.id().toString(),
                order.cartId().toString(),
                order.status().toString(),
                items,
                itemsTotal,
                order.totalPrice(),
                order.createdAt()
        );

    }

    private OrderItemResponse toResponse(OrderItem item) {
        return new OrderItemResponse(
                item.productId().toString(),
                item.productName(),
                item.productImgUrl(),
                item.unitPrice(),
                item.quantity(),
                item.lineTotal()
        );
    }

}

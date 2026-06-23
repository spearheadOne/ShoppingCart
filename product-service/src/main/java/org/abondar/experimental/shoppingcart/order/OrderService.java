package org.abondar.experimental.shoppingcart.order;

import org.abondar.experimental.shoppingcart.api.CreateOrderItemRequest;
import org.abondar.experimental.shoppingcart.api.CreateOrderRequest;
import org.abondar.experimental.shoppingcart.product.Product;
import org.abondar.experimental.shoppingcart.product.ProductNotFoundException;
import org.abondar.experimental.shoppingcart.util.UuidUtil;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OrderService {

    public Map<String, Object> toProductQueryParams(CreateOrderRequest request) {
        var productIds = request.items()
                .stream()
                .map(CreateOrderItemRequest::productId)
                .distinct()
                .toList();

        return Map.of("ids", productIds);

    }


    public Order createOrder(CreateOrderRequest request, List<Product> products) {
        var cartId = UuidUtil.parseUuid(request.cartId());
        var orderId = UUID.randomUUID();
        var createdAt = Instant.now();

        var productsById = products.stream()
                .collect(Collectors.toMap(Product::id, Function.identity()));

        var items = request.items()
                .stream()
                .map(requestItem -> createOrderItem(orderId,requestItem,productsById))
                .toList();

        var totalPrice = items.stream()
                .map(OrderItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new Order(
                orderId,
                cartId,
                OrderStatus.CREATED,
                totalPrice,
                createdAt,
                items
        );
    }

    private OrderItem createOrderItem(UUID orderId, CreateOrderItemRequest requestItem, Map<UUID, Product> products){
        var productId = UuidUtil.parseUuid(requestItem.productId());
        var product = Optional.ofNullable(products.get(productId))
                .orElseThrow(()-> new ProductNotFoundException(productId));

        var lineTotal = product.price()
                .multiply(BigDecimal.valueOf(requestItem.quantity()));

        return new OrderItem(
                orderId,
                product.id(),
                product.name(),
                product.imgUrl(),
                product.price(),
                requestItem.quantity(),
                lineTotal
        );
    }

}

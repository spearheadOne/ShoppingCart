package org.abondar.experimental.shoppingcart.order;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Mapper
public interface OrderMapper {


    @Insert("""
                  INSERT INTO orders(
                                     id,
                                     cart_id,
                                     status,
                                     total_price,
                                     created_at
                  ) VALUES (
                            #{id},
                            #{cartId},
                            #{status},
                            #{totalPrice},
                            #{createdAt}
                  )
            """)
    int insertOrder(Order order);


    @Insert("""
                     <script>
                       INSERT INTO order_items(
                                               order_id,
                                               product_id,
                                               product_name,
                                               product_img_url,
                                               unit_price,
                                               quantity,
                                               line_total
                       ) VALUES 
                                <foreach
                                   collection="items"
                                   item="item"
                                   separator=",">
                             (
                              #{item.orderId},
                              #{item.productId},
                              #{item.productName},
                              #{item.productImgUrl},
                              #{item.unitPrice},
                              #{item.quantity},
                              #{item.lineTotal}
                             )
                             </foreach>
                     </script>
            """)
    int insertOrderItems(@Param("items") List<OrderItem> item);


    @Select("""
            SELECT
                id,
                cart_id AS cartId,
                status,
                total_price AS totalPrice,
                created_at AS createdAt
            FROM orders
            WHERE id = CAST(#{id} AS UUID)
            """)
    Optional<OrderRecord> findOrderById(@Param("id") UUID id);

    @Select("""
            SELECT
                id,
                cart_id AS cartId,
                status,
                total_price AS totalPrice,
                created_at AS createdAt
            FROM orders
            WHERE cart_id = CAST(#{cartId} AS UUID)
            """)
    Optional<OrderRecord> findOrderByCartId(@Param("cartId") UUID cartId);

    @Select("""
            SELECT
                order_id AS orderId,
                product_id AS productId,
                product_name AS productName,
                product_img_url AS productImgUrl,
                unit_price AS unitPrice,
                quantity,
                line_total AS lineTotal
            FROM order_items
            WHERE order_id = CAST(#{orderId} AS UUID)
            ORDER BY product_name
            """)
    List<OrderItem> findOrderItems(@Param("orderId") UUID orderId);
}

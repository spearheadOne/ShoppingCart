package org.abondar.experimental.shoppingcart.product;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Mapper
public interface ProductMapper {

    @Select("""
            SELECT
                id,
                name,
                img_url AS imgUrl,
                price
            FROM products
            WHERE id = CAST(#{id} AS UUID)
            """)
    Optional<Product> findProductById(@Param("id") UUID id);


    @Select("""
            SELECT
                id,
                name,
                img_url AS imgUrl,
                price
            
            FROM products
            ORDER BY name
            LIMIT #{limit}
            OFFSET #{offset}
            """)
    List<Product> findAll(
            @Param("limit") int limit,
            @Param("offset") int offset
    );


    @Select("""
            <script>
            SELECT
                id,
                name,
                img_url AS imgUrl,
                price
            FROM products
            WHERE id IN
            <foreach
                collection="ids"
                item="id"
                open="("
                separator=","
                close=")">
                CAST(#{id} AS UUID)
            </foreach>
            </script>
            """)
    List<Product> findProductsByIds(@Param("ids") List<String> ids);


    @Select("""
            SELECT COUNT(*)
            FROM products
            """)
    long count();

}

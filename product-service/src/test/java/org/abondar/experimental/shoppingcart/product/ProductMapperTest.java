package org.abondar.experimental.shoppingcart.product;

import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.liquibase.autoconfigure.LiquibaseAutoConfiguration;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MybatisTest(properties = """
        spring.datasource.url=jdbc:h2:mem:product_mapper;MODE=PostgreSQL;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false
        """)
@MapperScan(basePackageClasses = ProductMapper.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration(LiquibaseAutoConfiguration.class)
public class ProductMapperTest {

    @Autowired
    private ProductMapper mapper;


    @Test
    public void findById() {
        var product = mapper.findProductById(UUID.fromString("11111111-1111-1111-1111-111111111111"));

        assertTrue(product.isPresent());
        assertEquals("Keyboard", product.get().name());

    }

    @Test
    public void findAll() {
        var products = mapper.findAll(10, 0);
        assertEquals(10, products.size());
    }

    @Test
    public void findProductsByIds() {
        var ids = List.of("11111111-1111-1111-1111-111111111111", "22222222-2222-2222-2222-222222222222");
        var products = mapper.findProductsByIds(ids);

        assertFalse(products.isEmpty());
        assertEquals(2, products.size());
    }


    @Test
    public void count() {
        var count = mapper.count();
        assertEquals(15, count);
    }

}

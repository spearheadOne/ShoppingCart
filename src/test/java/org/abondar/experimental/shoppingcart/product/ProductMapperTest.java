package org.abondar.experimental.shoppingcart.product;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.liquibase.autoconfigure.LiquibaseAutoConfiguration;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@MybatisTest
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
    public void count() {
        var count = mapper.count();
        assertEquals(15, count);
    }

}

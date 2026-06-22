package org.abondar.experimental.shoppingcart.product;

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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductMapper mapper;

    @InjectMocks
    private ProductService service;

    @Test
    public void getProductById() {
        var productId = UUID.randomUUID();
        var product = new Product(productId, "test", "test", BigDecimal.ONE);

        when(mapper.findProductById(productId)).thenReturn(Optional.of(product));

        var response = service.getProductById(productId.toString());

        assertEquals(productId.toString(), response.id());
    }

    @Test
    public void getProductByIdBadId() {
        var productId = "1111";

        assertThrows(IllegalArgumentException.class, () -> service.getProductById(productId));
    }


    @Test
    public void getProductNotFound() {
        var productId = UUID.randomUUID();
        when(mapper.findProductById(productId)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> service.getProductById(productId.toString()));
    }

    @Test
    public void getProductList() {
        var limit = 10;
        var offset = 0;

        var product = new Product(UUID.randomUUID(), "test", "test", BigDecimal.ONE);
        var product1 = new Product(UUID.randomUUID(), "test1", "test1", BigDecimal.ONE);

        when(mapper.findAll(limit, offset)).thenReturn(List.of(product, product1));
        when(mapper.count()).thenReturn(2L);

        var response = service.getProductList(limit, offset);

        assertFalse(response.products().isEmpty());
        assertEquals(2, response.total());
        assertEquals(limit, response.limit());
        assertEquals(offset, response.offset());
    }
}

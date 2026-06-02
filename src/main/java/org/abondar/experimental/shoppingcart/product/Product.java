package org.abondar.experimental.shoppingcart.product;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class Product {

   private String id;
   private String name;
   private String imgUrl;
   private BigDecimal price;

}



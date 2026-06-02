package org.abondar.experimental.cameldemo.shoppingcart.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CartProduct{

   private String name;
   private String imgUrl;
   private BigDecimal price;

}



package org.abondar.experimental.shoppingcart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "org.abondar.experimental.shoppingcart")
public class ShoppingCartApplication {

 static void main(String[] args) {
      SpringApplication.run(ShoppingCartApplication.class,args);
  }
}

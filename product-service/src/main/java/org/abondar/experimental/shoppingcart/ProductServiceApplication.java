package org.abondar.experimental.shoppingcart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "org.abondar.experimental.shoppingcart")
public class ProductServiceApplication {

    static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }
}

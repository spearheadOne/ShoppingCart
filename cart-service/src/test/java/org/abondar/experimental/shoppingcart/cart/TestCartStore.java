package org.abondar.experimental.shoppingcart.cart;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TestCartStore {

    private Cart cart;

    public void clear() {
        this.cart = null;
    }

}

package org.abondar.experimental.shoppingcart.util;

import java.util.UUID;

public class UuidUtil {

    public static UUID parseUuid(String id) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid product id: " + id);
        }
    }


    private UuidUtil(){}
}

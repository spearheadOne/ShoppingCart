package org.abondar.experimental.shoppingcart.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class UuidUtilTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "invalid",
            "123",
            " ",
            "11111111-1111-1111-1111"
    })
    void failOnInvalidUuid(String invalidId) {
        assertThrows(
                IllegalArgumentException.class,
                () -> UuidUtil.parseUuid(invalidId)
        );
    }

}

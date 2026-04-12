package com.example.shoppingcart.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class CartItemTest {

    // ─── Constructor ───────────────────────────────────────────────

    @Test
    void constructor_shouldCreateItemWithValidPriceAndQuantity() {
        CartItem item = new CartItem(9.99, 3);
        assertEquals(9.99, item.getPrice());
        assertEquals(3, item.getQuantity());
    }

    @Test
    void constructor_shouldAllowZeroPrice() {
        CartItem item = new CartItem(0.0, 1);
        assertEquals(0.0, item.getPrice());
    }

    @Test
    void constructor_shouldAllowZeroQuantity() {
        CartItem item = new CartItem(5.0, 0);
        assertEquals(0, item.getQuantity());
    }

    @Test
    void constructor_shouldThrowWhenPriceIsNegative() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new CartItem(-1.0, 2)
        );
        assertEquals("Price cannot be negative", ex.getMessage());
    }

    @Test
    void constructor_shouldThrowWhenQuantityIsNegative() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new CartItem(5.0, -1)
        );
        assertEquals("Quantity cannot be negative", ex.getMessage());
    }

    // ─── setPrice ──────────────────────────────────────────────────

    @Test
    void setPrice_shouldUpdatePrice() {
        CartItem item = new CartItem(5.0, 2);
        item.setPrice(19.99);
        assertEquals(19.99, item.getPrice());
    }

    @Test
    void setPrice_shouldAllowZero() {
        CartItem item = new CartItem(5.0, 2);
        item.setPrice(0.0);
        assertEquals(0.0, item.getPrice());
    }

    @Test
    void setPrice_shouldThrowWhenNegative() {
        CartItem item = new CartItem(5.0, 2);
        assertThrows(IllegalArgumentException.class, () -> item.setPrice(-0.01));
    }

    // ─── setQuantity ───────────────────────────────────────────────

    @Test
    void setQuantity_shouldUpdateQuantity() {
        CartItem item = new CartItem(5.0, 2);
        item.setQuantity(10);
        assertEquals(10, item.getQuantity());
    }

    @Test
    void setQuantity_shouldAllowZero() {
        CartItem item = new CartItem(5.0, 2);
        item.setQuantity(0);
        assertEquals(0, item.getQuantity());
    }

    @Test
    void setQuantity_shouldThrowWhenNegative() {
        CartItem item = new CartItem(5.0, 2);
        assertThrows(IllegalArgumentException.class, () -> item.setQuantity(-1));
    }

    // ─── Parameterized edge cases ───────────────────────────────────

    @ParameterizedTest(name = "price={0}, qty={1}")
    @CsvSource({
            "0.01, 1",
            "999.99, 50",
            "1.0, 100"
    })
    void constructor_shouldAcceptVariousValidCombinations(double price, int qty) {
        CartItem item = new CartItem(price, qty);
        assertEquals(price, item.getPrice());
        assertEquals(qty, item.getQuantity());
    }
}
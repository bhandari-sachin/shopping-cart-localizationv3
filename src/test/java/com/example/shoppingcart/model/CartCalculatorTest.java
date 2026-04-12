package com.example.shoppingcart.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CartCalculatorTest {

    private CartCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new CartCalculator();
    }

    // ─── Empty / null list ─────────────────────────────────────────

    @Test
    void calculateTotal_shouldReturnZeroForEmptyList() {
        double total = calculator.calculateTotal(Collections.emptyList());
        assertEquals(0.0, total);
    }

    // ─── Single item ───────────────────────────────────────────────

    @Test
    void calculateTotal_shouldReturnCorrectTotalForSingleItem() {
        List<CartItem> items = List.of(new CartItem(10.0, 3));
        assertEquals(30.0, calculator.calculateTotal(items));
    }

    @Test
    void calculateTotal_shouldReturnZeroForItemWithZeroQuantity() {
        List<CartItem> items = List.of(new CartItem(99.99, 0));
        assertEquals(0.0, calculator.calculateTotal(items));
    }

    @Test
    void calculateTotal_shouldReturnZeroForItemWithZeroPrice() {
        List<CartItem> items = List.of(new CartItem(0.0, 5));
        assertEquals(0.0, calculator.calculateTotal(items));
    }

    // ─── Multiple items ────────────────────────────────────────────

    @Test
    void calculateTotal_shouldSumMultipleItems() {
        List<CartItem> items = List.of(
                new CartItem(5.0, 2),   // 10.0
                new CartItem(3.0, 4),   // 12.0
                new CartItem(1.5, 6)    //  9.0
        );
        assertEquals(31.0, calculator.calculateTotal(items), 0.001);
    }

    @Test
    void calculateTotal_shouldHandleDecimalPrecision() {
        List<CartItem> items = List.of(
                new CartItem(0.1, 3),   // 0.3
                new CartItem(0.2, 1)    // 0.2
        );
        assertEquals(0.5, calculator.calculateTotal(items), 0.0001);
    }

    @Test
    void calculateTotal_shouldHandleLargeQuantities() {
        List<CartItem> items = List.of(new CartItem(1.0, 1000));
        assertEquals(1000.0, calculator.calculateTotal(items));
    }

    @Test
    void calculateTotal_shouldHandleLargePrice() {
        List<CartItem> items = List.of(new CartItem(9999.99, 2));
        assertEquals(19999.98, calculator.calculateTotal(items), 0.001);
    }

    // ─── Multiple items with quantity 1 ────────────────────────────

    @Test
    void calculateTotal_shouldCorrectlyAddSingleQuantityItems() {
        List<CartItem> items = List.of(
                new CartItem(10.0, 1),
                new CartItem(20.0, 1),
                new CartItem(30.0, 1)
        );
        assertEquals(60.0, calculator.calculateTotal(items));
    }
}
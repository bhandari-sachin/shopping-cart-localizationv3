package com.example.shoppingcart.model;

import java.util.List;

/**
 * Calculates totals for shopping cart items.
 */
public class CartCalculator {

    /**
     * Calculates the total cost of the supplied cart items.
     *
     * @param items the cart items to total
     * @return the sum of each item's price multiplied by its quantity
     */
    public double calculateTotal(List<CartItem> items) {
        return items.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
    }
}
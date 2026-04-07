package com.example.shoppingcart.model;

import java.util.List;

public class CartCalculator {

    public double calculateTotal(List<CartItem> items) {
        return items.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
    }
}
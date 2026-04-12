package com.example.shoppingcart.model;

/**
 * Represents a single item in the shopping cart.
 */
public class CartItem {
    private double price;
    private int quantity;

    /**
     * Creates a cart item with the given price and quantity.
     *
     * @param price the item price; must be non-negative
     * @param quantity the item quantity; must be non-negative
     * @throws IllegalArgumentException if either argument is negative
     */
    public CartItem(double price, int quantity) {
        if (price < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        this.price = price;
        this.quantity = quantity;
    }

    /**
     * Returns the item price.
     *
     * @return the current price
     */
    public double getPrice() {
        return price;
    }

    /**
     * Updates the item price.
     *
     * @param price the new price; must be non-negative
     * @throws IllegalArgumentException if the price is negative
     */
    public void setPrice(double price) {
        if (price < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        this.price = price;
    }

    /**
     * Returns the item quantity.
     *
     * @return the current quantity
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Updates the item quantity.
     *
     * @param quantity the new quantity; must be non-negative
     * @throws IllegalArgumentException if the quantity is negative
     */
    public void setQuantity(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        this.quantity = quantity;
    }
}
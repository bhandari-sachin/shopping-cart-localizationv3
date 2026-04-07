package com.example.shoppingcart.model;

public class CartItem {
    private double price;
    private int quantity;

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

    public double getPrice() { 
        return price; 
    }
    
    public void setPrice(double price) { 
        if (price < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        this.price = price; 
    }

    public int getQuantity() { 
        return quantity; 
    }
    
    public void setQuantity(int quantity) { 
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        this.quantity = quantity; 
    }
}
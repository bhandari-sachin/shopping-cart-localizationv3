package com.example.shoppingcart.model;

public class CartItem {
    private double price;
    private int quantity;

    public CartItem(double price, int quantity) {
        this.price = price;
        this.quantity = quantity;
    }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
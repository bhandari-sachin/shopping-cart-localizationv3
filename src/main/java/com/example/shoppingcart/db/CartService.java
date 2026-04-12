package com.example.shoppingcart.db;

import com.example.shoppingcart.model.CartItem;

import java.sql.*;
import java.util.List;

/**
 * Persists shopping cart records and their items to the database.
 */
public class CartService {

    /**
     * Saves a cart record and its associated items.
     *
     * @param totalItems the number of items in the cart
     * @param totalCost the total cart cost
     * @param language the selected language code
     * @param items the cart items to persist
     */
    public void saveCart(int totalItems, double totalCost, String language, List<CartItem> items) {
        String cartSql = "INSERT INTO cart_records (total_items, total_cost, language) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(cartSql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, totalItems);
            ps.setDouble(2, totalCost);
            ps.setString(3, language);
            ps.executeUpdate();

            ResultSet generatedKeys = ps.getGeneratedKeys();
            if (generatedKeys.next()) {
                int cartId = generatedKeys.getInt(1);
                saveCartItems(conn, cartId, items);
            }

        } catch (SQLException e) {
            System.err.println("CartService error while saving cart: " + e.getMessage());
            System.err.println("Failed to save cart with " + totalItems + " items and total cost " + totalCost);
            e.printStackTrace();
        }
    }

    private void saveCartItems(Connection conn, int cartId, List<CartItem> items) throws SQLException {
        String sql = "INSERT INTO cart_items (cart_record_id, item_number, price, quantity, subtotal) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < items.size(); i++) {
                CartItem item = items.get(i);
                double subtotal = item.getPrice() * item.getQuantity();

                ps.setInt(1, cartId);
                ps.setInt(2, i + 1);
                ps.setDouble(3, item.getPrice());
                ps.setInt(4, item.getQuantity());
                ps.setDouble(5, subtotal);
                ps.executeUpdate();
            }
        }
    }
}
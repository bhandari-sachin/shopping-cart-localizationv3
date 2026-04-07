package com.example.shoppingcart.db;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class LocalizationService {

    public Map<String, String> getStrings(String language) {
        Map<String, String> messages = new HashMap<>();

        String sql = "SELECT `key`, value FROM localization_strings WHERE language = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, language);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                messages.put(rs.getString("key"), rs.getString("value"));
            }
            rs.close();

        } catch (SQLException e) {
            System.err.println("LocalizationService error retrieving strings for language '" + language + "': " + e.getMessage());
            e.printStackTrace();
            // Return fallback English strings when database is unavailable
            return getFallbackStrings();
        }

        return messages;
    }

    private Map<String, String> getFallbackStrings() {
        Map<String, String> messages = new HashMap<>();
        messages.put("select.language", "Select Language:");
        messages.put("prompt.num.items", "Number of items:");
        messages.put("btn.generate.items", "Generate Items");
        messages.put("btn.calculate.total", "Calculate Total");
        messages.put("total.cost", "Total Cost:");
        messages.put("prompt.price", "Price");
        messages.put("prompt.quantity", "Quantity");
        messages.put("item.prompt", "Item");
        messages.put("error.invalid.number", "Invalid number format");
        messages.put("error.positive.number", "Please enter a positive number");
        return messages;
    }
}
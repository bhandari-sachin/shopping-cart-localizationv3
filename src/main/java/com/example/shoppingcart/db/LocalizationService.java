package com.example.shoppingcart.db;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads localized UI strings from the database, with a fallback set for error cases.
 */
public class LocalizationService {

    /**
     * Returns localized strings for the requested language.
     *
     * @param language the language code to load
     * @return a map of localization keys to translated values
     */
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
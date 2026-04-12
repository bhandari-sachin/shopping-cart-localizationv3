package com.example.shoppingcart.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Creates and validates JDBC connections for the application database.
 */
public class DatabaseConnection {

    private static final String DB_HOST = System.getenv().getOrDefault("DB_HOST", "localhost");
    private static final String DB_PORT = System.getenv().getOrDefault("DB_PORT", "3306");
    private static final String DB_NAME = System.getenv().getOrDefault("DB_NAME", "shopping_cart_localization");
    private static final String DB_USER = System.getenv().getOrDefault("DB_USER", "root");
    private static final String DB_PASSWORD = System.getenv().getOrDefault("DB_PASSWORD", "");

    private static final String URL = String.format(
            "jdbc:mysql://%s:%s/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
            DB_HOST, DB_PORT, DB_NAME
    );

    /**
     * Opens a JDBC connection to the configured MySQL database.
     *
     * @return an open JDBC connection
     * @throws SQLException if the driver is missing or the connection cannot be established
     */
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, DB_USER, DB_PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException(
                    "MySQL JDBC Driver (mysql-connector-j) not found on classpath. Ensure it is included in pom.xml as a dependency.",
                    e
            );
        } catch (SQLException e) {
            String errorMsg = "Failed to connect to MySQL database at " + DB_HOST + ":" + DB_PORT + "/" + DB_NAME
                    + " using user '" + DB_USER + "'.\n"
                    + "Ensure:\n"
                    + "  1. MySQL server is running on " + DB_HOST + ":" + DB_PORT + "\n"
                    + "  2. The database '" + DB_NAME + "' exists\n"
                    + "  3. The user '" + DB_USER + "' exists with correct password\n"
                    + "  4. The schema has been initialized (run database-schema.sql)\n"
                    + "Override with environment variables: DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD";

            throw new SQLException(errorMsg, e);
        }
    }

    /**
     * Attempts to open and immediately close a database connection.
     *
     * @return {@code true} if a usable connection was established; otherwise {@code false}
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            return false;
        }
    }
}
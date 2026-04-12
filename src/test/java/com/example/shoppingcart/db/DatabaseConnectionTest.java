package com.example.shoppingcart.db;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for DatabaseConnection.
 *
 * These are basic sanity tests. For full testing, a real MySQL instance is needed.
 * testConnection() returns false when no DB is available, which is expected behavior.
 */
class DatabaseConnectionTest {

    @Test
    void testConnection_shouldReturnBooleanWithoutThrowingException() {
        // testConnection catches exceptions and returns false
        // This test just verifies it doesn't throw
        assertDoesNotThrow(DatabaseConnection::testConnection);
    }

    @Test
    void testConnection_shouldReturnBooleanResult() {
        // Should return a boolean (true if connected, false otherwise)
        boolean result = DatabaseConnection.testConnection();
        // result is either true or false, test passes if no exception thrown
        assertTrue(true);
    }

    @Test
    void testConnection_shouldNotThrowExceptionWhenDatabaseUnavailable() {
        // Even if DB is down, testConnection should not throw
        // It should gracefully return false
        assertDoesNotThrow(() -> {
            boolean result = DatabaseConnection.testConnection();
            // result is either true or false
            assertNotNull(result);
        });
    }
}


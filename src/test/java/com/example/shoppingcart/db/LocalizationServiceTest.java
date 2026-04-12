package com.example.shoppingcart.db;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.*;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests for LocalizationService.
 * DatabaseConnection is statically mocked so no real DB is needed.
 */
class LocalizationServiceTest {

    private LocalizationService localizationService;
    private MockedStatic<DatabaseConnection> mockedDbConnection;

    private Connection mockConnection;
    private PreparedStatement mockStatement;
    private ResultSet mockResultSet;

    @BeforeEach
    void setUp() throws SQLException {
        localizationService = new LocalizationService();

        mockConnection  = mock(Connection.class);
        mockStatement   = mock(PreparedStatement.class);
        mockResultSet   = mock(ResultSet.class);

        mockedDbConnection = Mockito.mockStatic(DatabaseConnection.class);
        mockedDbConnection.when(DatabaseConnection::getConnection).thenReturn(mockConnection);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
    }

    @AfterEach
    void tearDown() {
        mockedDbConnection.close();
    }

    // ─── Happy path ────────────────────────────────────────────────

    @Test
    void getStrings_shouldReturnMessagesFromDatabase() throws SQLException {
        // Simulate two rows returned from DB
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getString("key"))
                .thenReturn("total.cost", "prompt.price");
        when(mockResultSet.getString("value"))
                .thenReturn("Total Cost:", "Price");

        Map<String, String> messages = localizationService.getStrings("en");

        assertFalse(messages.isEmpty());
        assertEquals("Total Cost:", messages.get("total.cost"));
        assertEquals("Price",       messages.get("prompt.price"));
    }

    @Test
    void getStrings_shouldSetLanguageParameterOnQuery() throws SQLException {
        when(mockResultSet.next()).thenReturn(false);

        localizationService.getStrings("fi");

        verify(mockStatement).setString(1, "fi");
    }

    @Test
    void getStrings_shouldReturnEmptyMapWhenNoRowsInDB() throws SQLException {
        when(mockResultSet.next()).thenReturn(false);

        Map<String, String> messages = localizationService.getStrings("ja");

        assertNotNull(messages);
        assertTrue(messages.isEmpty());
    }

    // ─── Fallback when DB is unavailable ───────────────────────────

    @Test
    void getStrings_shouldReturnFallbackStringsOnSQLException() throws SQLException {
        when(mockConnection.prepareStatement(anyString()))
                .thenThrow(new SQLException("Connection refused"));

        Map<String, String> messages = localizationService.getStrings("en");

        // Fallback map must always contain these keys
        assertAll(
                () -> assertTrue(messages.containsKey("select.language")),
                () -> assertTrue(messages.containsKey("prompt.num.items")),
                () -> assertTrue(messages.containsKey("btn.generate.items")),
                () -> assertTrue(messages.containsKey("btn.calculate.total")),
                () -> assertTrue(messages.containsKey("total.cost")),
                () -> assertTrue(messages.containsKey("prompt.price")),
                () -> assertTrue(messages.containsKey("prompt.quantity")),
                () -> assertTrue(messages.containsKey("item.prompt")),
                () -> assertTrue(messages.containsKey("error.invalid.number")),
                () -> assertTrue(messages.containsKey("error.positive.number"))
        );
    }

    @Test
    void getStrings_fallbackShouldHaveCorrectEnglishValues() throws SQLException {
        when(mockConnection.prepareStatement(anyString()))
                .thenThrow(new SQLException("DB down"));

        Map<String, String> messages = localizationService.getStrings("en");

        assertEquals("Select Language:",  messages.get("select.language"));
        assertEquals("Number of items:",  messages.get("prompt.num.items"));
        assertEquals("Total Cost:",        messages.get("total.cost"));
        assertEquals("Invalid number format", messages.get("error.invalid.number"));
    }

    @Test
    void getStrings_shouldReturnFallbackWhenDBStatementFails() throws SQLException {
        when(mockConnection.prepareStatement(anyString()))
                .thenThrow(new SQLException("No DB"));

        // Should not throw — must gracefully return fallback
        assertDoesNotThrow(() -> {
            Map<String, String> messages = localizationService.getStrings("sv");
            assertFalse(messages.isEmpty());
        });
    }
}
package com.example.shoppingcart.db;

import com.example.shoppingcart.model.CartItem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.*;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for CartService.
 * All DB interactions are mocked — no real MySQL required.
 */
class CartServiceTest {

    private CartService cartService;
    private MockedStatic<DatabaseConnection> mockedDbConnection;

    private Connection     mockConnection;
    private PreparedStatement mockCartStmt;
    private PreparedStatement mockItemStmt;
    private ResultSet      mockGeneratedKeys;

    @BeforeEach
    void setUp() throws SQLException {
        cartService = new CartService();

        mockConnection    = mock(Connection.class);
        mockCartStmt      = mock(PreparedStatement.class);
        mockItemStmt      = mock(PreparedStatement.class);
        mockGeneratedKeys = mock(ResultSet.class);

        mockedDbConnection = Mockito.mockStatic(DatabaseConnection.class);
        mockedDbConnection.when(DatabaseConnection::getConnection).thenReturn(mockConnection);

        // prepareStatement returns different stubs depending on call order
        when(mockConnection.prepareStatement(
                contains("cart_records"), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(mockCartStmt);
        when(mockConnection.prepareStatement(contains("cart_items")))
                .thenReturn(mockItemStmt);

        when(mockCartStmt.getGeneratedKeys()).thenReturn(mockGeneratedKeys);
        when(mockGeneratedKeys.next()).thenReturn(true);
        when(mockGeneratedKeys.getInt(1)).thenReturn(42);
    }

    @AfterEach
    void tearDown() {
        mockedDbConnection.close();
    }

    // ─── saveCart happy path ────────────────────────────────────────

    @Test
    void saveCart_shouldExecuteCartInsert() throws SQLException {
        List<CartItem> items = List.of(new CartItem(5.0, 2));

        cartService.saveCart(1, 10.0, "en", items);

        verify(mockCartStmt).setInt(1, 1);
        verify(mockCartStmt).setDouble(2, 10.0);
        verify(mockCartStmt).setString(3, "en");
        verify(mockCartStmt).executeUpdate();
    }

    @Test
    void saveCart_shouldInsertEachCartItem() throws SQLException {
        List<CartItem> items = List.of(
                new CartItem(5.0, 2),
                new CartItem(3.0, 4)
        );

        cartService.saveCart(2, 22.0, "fi", items);

        // executeUpdate called once per item
        verify(mockItemStmt, times(2)).executeUpdate();
    }

    @Test
    void saveCart_shouldSetCorrectSubtotalForEachItem() throws SQLException {
        List<CartItem> items = List.of(new CartItem(10.0, 3)); // subtotal = 30.0

        cartService.saveCart(1, 30.0, "en", items);

        verify(mockItemStmt).setDouble(eq(5), eq(30.0)); // 5th param = subtotal
    }

    @Test
    void saveCart_shouldSetItemNumberStartingAtOne() throws SQLException {
        List<CartItem> items = List.of(
                new CartItem(1.0, 1),
                new CartItem(2.0, 1)
        );

        cartService.saveCart(2, 3.0, "en", items);

        verify(mockItemStmt).setInt(2, 1); // first item  → item_number = 1
        verify(mockItemStmt).setInt(2, 2); // second item → item_number = 2
    }

    @Test
    void saveCart_shouldHandleEmptyItemList() throws SQLException {
        // No exception should be thrown for empty list
        assertDoesNotThrow(() ->
                cartService.saveCart(0, 0.0, "en", Collections.emptyList())
        );
        verify(mockItemStmt, never()).executeUpdate();
    }

    // ─── DB failure ─────────────────────────────────────────────────

    @Test
    void saveCart_shouldNotThrowWhenDBConnectionFails() {
        mockedDbConnection.when(DatabaseConnection::getConnection)
                .thenThrow(new SQLException("DB unavailable"));

        // CartService catches SQLException and doesn't rethrow
        assertDoesNotThrow(() ->
                cartService.saveCart(1, 10.0, "en", List.of(new CartItem(10.0, 1)))
        );
    }

    @Test
    void saveCart_shouldNotThrowWhenCartInsertFails() throws SQLException {
        when(mockCartStmt.executeUpdate()).thenThrow(new SQLException("Insert failed"));

        assertDoesNotThrow(() ->
                cartService.saveCart(1, 10.0, "en", List.of(new CartItem(10.0, 1)))
        );
    }

    @Test
    void saveCart_shouldNotSaveItemsWhenNoGeneratedKeyReturned() throws SQLException {
        when(mockGeneratedKeys.next()).thenReturn(false); // no key generated

        cartService.saveCart(1, 10.0, "en", List.of(new CartItem(5.0, 2)));

        verify(mockItemStmt, never()).executeUpdate();
    }

    // ─── Language parameter ─────────────────────────────────────────

    @Test
    void saveCart_shouldPassLanguageCorrectly() throws SQLException {
        cartService.saveCart(1, 9.99, "ja", List.of(new CartItem(9.99, 1)));

        verify(mockCartStmt).setString(3, "ja");
    }
}
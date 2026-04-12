package com.example.shoppingcart.controller;

import com.example.shoppingcart.db.CartService;
import com.example.shoppingcart.db.LocalizationService;
import com.example.shoppingcart.model.CartCalculator;
import com.example.shoppingcart.model.CartItem;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ShoppingCartController {

    private static final List<String> SUPPORTED_LANGUAGE_OPTIONS = List.of(
            "English", "Finnish", "Swedish", "Japanese", "Arabic"
    );
    private static final Map<String, String> LANGUAGE_CODES = Map.of(
            "English", "en",
            "Finnish", "fi",
            "Swedish", "sv",
            "Japanese", "ja",
            "Arabic", "ar"
    );
    private static final int MAX_ITEM_COUNT = 50;
    private static final String DEFAULT_LANGUAGE_CODE = "en";
    private static final String DEFAULT_LANGUAGE_NAME = "English";
    private static final String ITEM_COUNT_PROMPT = "Enter number (1-50)";
    private static final String ERROR_TITLE = "Error";
    private static final String SUCCESS_TITLE = "Success";
    private static final String WARNING_TITLE = "Warning";
    private static final String CART_SAVED_SUCCESS_MESSAGE = "Cart saved to database successfully!";
    private static final String ENTER_NUMBER_MESSAGE = "Please enter a number";
    private static final String NO_ITEMS_MESSAGE = "No items to calculate. Please generate items first.";
    private static final String FILL_FIELDS_MESSAGE = "Please fill in all price and quantity fields";
    private static final String DB_SAVE_FAILURE_TEMPLATE = "Total calculated: %.2f\nBut failed to save to database: %s";
    private static final String TOTAL_FORMAT = " %.2f";

    private static final String KEY_SELECT_LANGUAGE = "select.language";
    private static final String KEY_PROMPT_NUM_ITEMS = "prompt.num.items";
    private static final String KEY_GENERATE_ITEMS = "btn.generate.items";
    private static final String KEY_CALCULATE_TOTAL = "btn.calculate.total";
    private static final String KEY_TOTAL_COST = "total.cost";
    private static final String KEY_PROMPT_PRICE = "prompt.price";
    private static final String KEY_PROMPT_QUANTITY = "prompt.quantity";
    private static final String KEY_ITEM_PROMPT = "item.prompt";
    private static final String KEY_INVALID_NUMBER = "error.invalid.number";
    private static final String KEY_POSITIVE_NUMBER = "error.positive.number";

    private static final String FALLBACK_SELECT_LANGUAGE = "Select Language:";
    private static final String FALLBACK_NUMBER_OF_ITEMS = "Number of items:";
    private static final String FALLBACK_GENERATE_ITEMS = "Generate Items";
    private static final String FALLBACK_CALCULATE_TOTAL = "Calculate Total";
    private static final String FALLBACK_TOTAL_COST = "Total Cost:";
    private static final String FALLBACK_PRICE = "Price";
    private static final String FALLBACK_QUANTITY = "Quantity";
    private static final String FALLBACK_ITEM = "Item";

    @FunctionalInterface
    interface AlertHandler {
        void show(Alert.AlertType type, String title, String contentText);
    }

    @FXML private ChoiceBox<String> languageChoiceBox;
    @FXML private TextField itemCountField;
    @FXML private VBox itemsContainer;
    @FXML private Label totalLabel;
    @FXML private Label languageLabel;
    @FXML private Label itemCountLabel;
    @FXML private Button generateItemsButton;
    @FXML private Button calculateTotalButton;

    private LocalizationService localizationService = new LocalizationService();
    private CartService cartService = new CartService();
    private CartCalculator calculator = new CartCalculator();
    private AlertHandler alertHandler = this::showDefaultAlert;

    private Map<String, String> localizedMessages;
    private String selectedLanguageCode = DEFAULT_LANGUAGE_CODE;
    private final List<TextField[]> itemInputFields = new ArrayList<>();

    /**
     * Initializes the controller, default language selection, and labels.
     */
    @FXML
    public void initialize() {
        languageChoiceBox.getItems().addAll(SUPPORTED_LANGUAGE_OPTIONS);
        languageChoiceBox.setValue(DEFAULT_LANGUAGE_NAME);

        localizedMessages = localizationService.getStrings(DEFAULT_LANGUAGE_CODE);
        refreshLabels();

        languageChoiceBox.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldValue, newValue) -> applyLanguageSelection(newValue));

        itemCountField.setPromptText(ITEM_COUNT_PROMPT);
    }

    void setLocalizationService(LocalizationService localizationService) {
        this.localizationService = localizationService;
    }

    void setCartService(CartService cartService) {
        this.cartService = cartService;
    }

    void setCalculator(CartCalculator calculator) {
        this.calculator = calculator;
    }

    void setAlertHandler(AlertHandler alertHandler) {
        this.alertHandler = alertHandler != null ? alertHandler : this::showDefaultAlert;
    }

    void applyLanguageSelection(String selectedLanguageName) {
        selectedLanguageCode = LANGUAGE_CODES.getOrDefault(selectedLanguageName, DEFAULT_LANGUAGE_CODE);
        localizedMessages = localizationService.getStrings(selectedLanguageCode);
        refreshLabels();
        refreshGeneratedItemLabels();
    }

    void refreshLabels() {
        languageLabel.setText(localizedMessages.getOrDefault(KEY_SELECT_LANGUAGE, FALLBACK_SELECT_LANGUAGE));
        itemCountLabel.setText(localizedMessages.getOrDefault(KEY_PROMPT_NUM_ITEMS, FALLBACK_NUMBER_OF_ITEMS));
        generateItemsButton.setText(localizedMessages.getOrDefault(KEY_GENERATE_ITEMS, FALLBACK_GENERATE_ITEMS));
        calculateTotalButton.setText(localizedMessages.getOrDefault(KEY_CALCULATE_TOTAL, FALLBACK_CALCULATE_TOTAL));
        totalLabel.setText(localizedMessages.getOrDefault(KEY_TOTAL_COST, FALLBACK_TOTAL_COST) + " 0.00");
    }

    void refreshGeneratedItemLabels() {
        for (int i = 0; i < itemsContainer.getChildren().size(); i++) {
            HBox row = (HBox) itemsContainer.getChildren().get(i);
            Label itemLabel = (Label) row.getChildren().get(0);
            itemLabel.setText(localizedMessages.getOrDefault(KEY_ITEM_PROMPT, FALLBACK_ITEM) + " " + (i + 1));

            TextField priceField = (TextField) row.getChildren().get(1);
            TextField qtyField = (TextField) row.getChildren().get(2);
            priceField.setPromptText(localizedMessages.getOrDefault(KEY_PROMPT_PRICE, FALLBACK_PRICE));
            qtyField.setPromptText(localizedMessages.getOrDefault(KEY_PROMPT_QUANTITY, FALLBACK_QUANTITY));
        }
    }

    /**
     * Generates item input rows from the requested item count.
     */
    @FXML
    void handleGenerateItemsAction() {
        try {
            String input = itemCountField.getText().trim();

            if (input.isEmpty()) {
                showErrorAlert(ENTER_NUMBER_MESSAGE);
                return;
            }

            int numItems = Integer.parseInt(input);

            if (numItems <= 0) {
                showErrorAlert(KEY_POSITIVE_NUMBER);
                return;
            }
            if (numItems > MAX_ITEM_COUNT) {
                showErrorAlert(KEY_INVALID_NUMBER);
                return;
            }

            itemsContainer.getChildren().clear();
            itemInputFields.clear();

            for (int i = 0; i < numItems; i++) {
                TextField priceField = new TextField();
                priceField.setPromptText(localizedMessages.getOrDefault(KEY_PROMPT_PRICE, "Price"));
                priceField.setPrefWidth(100);

                TextField qtyField = new TextField();
                qtyField.setPromptText(localizedMessages.getOrDefault(KEY_PROMPT_QUANTITY, "Quantity"));
                qtyField.setPrefWidth(100);

                Label itemLabel = new Label(localizedMessages.getOrDefault(KEY_ITEM_PROMPT, "Item") + " " + (i + 1));
                itemLabel.setPrefWidth(60);

                HBox row = new HBox(10, itemLabel, priceField, qtyField);
                itemsContainer.getChildren().add(row);
                itemInputFields.add(new TextField[]{priceField, qtyField});
            }

        } catch (NumberFormatException e) {
            showErrorAlert(KEY_INVALID_NUMBER);
        }
    }

    /**
     * Calculates the cart total and attempts to persist the current cart.
     */
    @FXML
    void handleCalculateTotalAction() {
        if (itemInputFields.isEmpty()) {
            showErrorAlert(NO_ITEMS_MESSAGE);
            return;
        }

        List<CartItem> items = new ArrayList<>();

        for (TextField[] fields : itemInputFields) {
            try {
                String priceText = fields[0].getText().trim();
                String qtyText = fields[1].getText().trim();

                if (priceText.isEmpty() || qtyText.isEmpty()) {
                    showErrorAlert(FILL_FIELDS_MESSAGE);
                    return;
                }

                double price = Double.parseDouble(priceText);
                int quantity = Integer.parseInt(qtyText);

                if (price <= 0 || quantity <= 0) {
                    showErrorAlert(KEY_POSITIVE_NUMBER);
                    return;
                }

                items.add(new CartItem(price, quantity));

            } catch (NumberFormatException e) {
                showErrorAlert(KEY_INVALID_NUMBER);
                return;
            }
        }

        double total = calculator.calculateTotal(items);
        totalLabel.setText(localizedMessages.getOrDefault(KEY_TOTAL_COST, "Total Cost:") + String.format(TOTAL_FORMAT, total));

        try {
            cartService.saveCart(items.size(), total, selectedLanguageCode, items);

            alertHandler.show(Alert.AlertType.INFORMATION, SUCCESS_TITLE, CART_SAVED_SUCCESS_MESSAGE);
        } catch (Exception e) {
            alertHandler.show(Alert.AlertType.WARNING, WARNING_TITLE,
                    String.format(DB_SAVE_FAILURE_TEMPLATE, total, e.getMessage()));
        }
    }

    void showErrorAlert(String messageKey) {
        String message = localizedMessages != null ?
                localizedMessages.getOrDefault(messageKey, messageKey) : messageKey;
        alertHandler.show(Alert.AlertType.ERROR, ERROR_TITLE, message);
    }

    private void showDefaultAlert(Alert.AlertType type, String title, String contentText) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(contentText);
        alert.showAndWait();
    }
}
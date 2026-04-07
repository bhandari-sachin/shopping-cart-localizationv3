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

    @FXML private ChoiceBox<String> choiceLanguage;
    @FXML private TextField txtNumItems;
    @FXML private VBox itemsContainer;
    @FXML private Label lblTotal;
    @FXML private Label lblLanguage;
    @FXML private Label lblNumItems;
    @FXML private Button btnGenerate;
    @FXML private Button btnCalculate;

    private LocalizationService localizationService = new LocalizationService();
    private CartService cartService = new CartService();
    private CartCalculator calculator = new CartCalculator();

    private Map<String, String> messages;
    private String currentLanguage = "en";
    private List<TextField[]> itemFields = new ArrayList<>();

    @FXML
    public void initialize() {
        System.out.println("Controller initializing..."); // Debug

        // Initialize language choice box
        choiceLanguage.getItems().addAll("English", "Finnish", "Swedish", "Japanese", "Arabic");
        choiceLanguage.setValue("English");

        // Load English strings
        messages = localizationService.getStrings("en");
        updateLabels();

        // Language change listener
        choiceLanguage.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> switchLanguage(newVal));

        // Set prompt text for number input
        txtNumItems.setPromptText("Enter number (1-50)");

        System.out.println("Controller initialized successfully"); // Debug
    }

    private void switchLanguage(String language) {
        switch (language) {
            case "Finnish":
                currentLanguage = "fi";
                break;
            case "Swedish":
                currentLanguage = "sv";
                break;
            case "Japanese":
                currentLanguage = "ja";
                break;
            case "Arabic":
                currentLanguage = "ar";
                break;
            default:
                currentLanguage = "en";
        }

        messages = localizationService.getStrings(currentLanguage);
        updateLabels();
        refreshItemLabels();
    }

    private void updateLabels() {
        lblLanguage.setText(messages.getOrDefault("select.language", "Select Language:"));
        lblNumItems.setText(messages.getOrDefault("prompt.num.items", "Number of items:"));
        btnGenerate.setText(messages.getOrDefault("btn.generate.items", "Generate Items"));
        btnCalculate.setText(messages.getOrDefault("btn.calculate.total", "Calculate Total"));
        lblTotal.setText(messages.getOrDefault("total.cost", "Total Cost:") + " 0.00");
    }

    private void refreshItemLabels() {
        for (int i = 0; i < itemsContainer.getChildren().size(); i++) {
            HBox row = (HBox) itemsContainer.getChildren().get(i);
            Label itemLabel = (Label) row.getChildren().get(0);
            itemLabel.setText(messages.getOrDefault("item.prompt", "Item") + " " + (i + 1));

            TextField priceField = (TextField) row.getChildren().get(1);
            TextField qtyField = (TextField) row.getChildren().get(2);
            priceField.setPromptText(messages.getOrDefault("prompt.price", "Price"));
            qtyField.setPromptText(messages.getOrDefault("prompt.quantity", "Quantity"));
        }
    }

    @FXML
    public void generateItemFields() {
        System.out.println("Generate button clicked!"); // Debug

        try {
            String input = txtNumItems.getText().trim();
            System.out.println("Input value: '" + input + "'"); // Debug

            if (input.isEmpty()) {
                showError("Please enter a number");
                return;
            }

            int numItems = Integer.parseInt(input);
            System.out.println("Parsed number: " + numItems); // Debug

            if (numItems <= 0) {
                showError("error.positive.number");
                return;
            }
            if (numItems > 50) {
                showError("error.invalid.number");
                return;
            }

            itemsContainer.getChildren().clear();
            itemFields.clear();

            for (int i = 0; i < numItems; i++) {
                TextField priceField = new TextField();
                priceField.setPromptText(messages.getOrDefault("prompt.price", "Price"));
                priceField.setPrefWidth(100);

                TextField qtyField = new TextField();
                qtyField.setPromptText(messages.getOrDefault("prompt.quantity", "Quantity"));
                qtyField.setPrefWidth(100);

                Label itemLabel = new Label(messages.getOrDefault("item.prompt", "Item") + " " + (i + 1));
                itemLabel.setPrefWidth(60);

                HBox row = new HBox(10, itemLabel, priceField, qtyField);
                itemsContainer.getChildren().add(row);
                itemFields.add(new TextField[]{priceField, qtyField});
            }

            System.out.println("Generated " + numItems + " item fields"); // Debug

        } catch (NumberFormatException e) {
            System.err.println("Number format error: " + e.getMessage()); // Debug
            showError("error.invalid.number");
        }
    }

    @FXML
    public void calculateTotal() {
        System.out.println("Calculate button clicked!"); // Debug

        if (itemFields.isEmpty()) {
            showError("No items to calculate. Please generate items first.");
            return;
        }

        List<CartItem> items = new ArrayList<>();

        for (TextField[] fields : itemFields) {
            try {
                String priceText = fields[0].getText().trim();
                String qtyText = fields[1].getText().trim();

                if (priceText.isEmpty() || qtyText.isEmpty()) {
                    showError("Please fill in all price and quantity fields");
                    return;
                }

                double price = Double.parseDouble(priceText);
                int quantity = Integer.parseInt(qtyText);

                if (price <= 0 || quantity <= 0) {
                    showError("error.positive.number");
                    return;
                }

                items.add(new CartItem(price, quantity));

            } catch (NumberFormatException e) {
                showError("error.invalid.number");
                return;
            }
        }

        double total = calculator.calculateTotal(items);
        lblTotal.setText(messages.getOrDefault("total.cost", "Total Cost:") + String.format(" %.2f", total));

        // Save to database (comment out if database not working yet)
        try {
            cartService.saveCart(items.size(), total, currentLanguage, items);

            // Show success message
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Cart saved to database successfully!");
            alert.showAndWait();
        } catch (Exception e) {
            System.err.println("Database save error: " + e.getMessage());
            // Still show total even if database save fails
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText(null);
            alert.setContentText("Total calculated: " + String.format("%.2f", total) +
                    "\nBut failed to save to database: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void showError(String messageKey) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        String message = messages != null ?
                messages.getOrDefault(messageKey, messageKey) : messageKey;
        alert.setContentText(message);
        alert.showAndWait();
    }
}
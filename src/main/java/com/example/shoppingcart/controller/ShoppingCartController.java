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
        // Initialize language choice box
        choiceLanguage.getItems().addAll("English", "Finnish", "Swedish", "Japanese", "Arabic");
        choiceLanguage.setValue("English");

        // Load English strings
        messages = localizationService.getStrings("en");
        updateLabels();

        // Language change listener
        choiceLanguage.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> switchLanguage(newVal));
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
        try {
            int numItems = Integer.parseInt(txtNumItems.getText().trim());
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

        } catch (NumberFormatException e) {
            showError("error.invalid.number");
        }
    }

    @FXML
    public void calculateTotal() {
        if (itemFields.isEmpty()) {
            showError("error.invalid.number");
            return;
        }

        List<CartItem> items = new ArrayList<>();

        for (TextField[] fields : itemFields) {
            try {
                double price = Double.parseDouble(fields[0].getText().trim());
                int quantity = Integer.parseInt(fields[1].getText().trim());

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

        // Save to database
        cartService.saveCart(items.size(), total, currentLanguage, items);

        // Show success message
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText("Cart saved to database successfully!");
        alert.showAndWait();
    }

    private void showError(String messageKey) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(messages.getOrDefault(messageKey, "Invalid input"));
        alert.showAndWait();
    }
}
package com.example.shoppingcart.controller;

import com.example.shoppingcart.db.CartService;
import com.example.shoppingcart.db.LocalizationService;
import com.example.shoppingcart.model.CartCalculator;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("ShoppingCartController Tests")
class ShoppingCartControllerTest {

    private static boolean javafxStarted;

    private ShoppingCartController controller;
    private LocalizationService localizationService;
    private CartService cartService;
    private CartCalculator calculator;
    private ChoiceBox<String> languageChoiceBox;
    private TextField itemCountField;
    private VBox itemsContainer;
    private Label totalLabel;
    private Label languageLabel;
    private Label itemCountLabel;
    private Button generateItemsButton;
    private Button calculateTotalButton;
    private final List<RecordedAlert> alerts = new CopyOnWriteArrayList<>();

    private static class RecordedAlert {
        final Alert.AlertType type;
        final String title;
        final String content;

        RecordedAlert(Alert.AlertType type, String title, String content) {
            this.type = type;
            this.title = title;
            this.content = content;
        }
    }

    @BeforeAll
    static void startJavaFx() throws Exception {
        if (javafxStarted) {
            return;
        }

        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(latch::countDown);
        } catch (IllegalStateException alreadyStarted) {
            latch.countDown();
        }
        assertTrue(latch.await(10, TimeUnit.SECONDS), "JavaFX platform did not start in time");
        javafxStarted = true;
    }

    @AfterAll
    static void cleanupJavaFx() {
        // Leave the JavaFX runtime alive for the rest of the test JVM.
    }

    @BeforeEach
    void setUp() throws Exception {
        controller = new ShoppingCartController();
        localizationService = mock(LocalizationService.class);
        cartService = mock(CartService.class);
        calculator = mock(CartCalculator.class);
        alerts.clear();

        Map<String, String> en = createMessages("Select Language:", "Number of items:", "Generate Items",
                "Calculate Total", "Total Cost:", "Price", "Quantity", "Item",
                "Invalid number format", "Please enter a positive number");
        Map<String, String> fi = createMessages("Valitse kieli:", "Määrä:", "Luo tuotteet",
                "Laske summa", "Kokonaishinta:", "Hinta", "Määrä", "Tuote",
                "Virheellinen lukumuoto", "Syötä positiivinen luku");
        Map<String, String> sv = createMessages("Välj språk:", "Antal artiklar:", "Skapa artiklar",
                "Beräkna total", "Total kostnad:", "Pris", "Antal", "Artikel",
                "Felaktigt talformat", "Ange ett positivt tal");
        Map<String, String> ja = createMessages("言語を選択:", "項目数:", "項目を生成",
                "合計を計算", "合計金額:", "価格", "数量", "項目",
                "無効な数値形式", "正の数を入力してください");
        Map<String, String> ar = createMessages("اختر اللغة:", "عدد العناصر:", "إنشاء عناصر",
                "احسب المجموع", "إجمالي التكلفة:", "السعر", "الكمية", "عنصر",
                "تنسيق رقم غير صالح", "الرجاء إدخال رقم موجب");

        when(localizationService.getStrings("en")).thenReturn(en);
        when(localizationService.getStrings("fi")).thenReturn(fi);
        when(localizationService.getStrings("sv")).thenReturn(sv);
        when(localizationService.getStrings("ja")).thenReturn(ja);
        when(localizationService.getStrings("ar")).thenReturn(ar);

        controller.setLocalizationService(localizationService);
        controller.setCartService(cartService);
        controller.setCalculator(calculator);
        controller.setAlertHandler((type, title, content) -> alerts.add(new RecordedAlert(type, title, content)));

        createControlsOnFxThread();
    }

    @Test
    @DisplayName("initialize should configure the language control and default labels")
    void initializeShouldConfigureControls() throws Exception {
        runOnFxThread(() -> controller.initialize());

        assertEquals(List.of("English", "Finnish", "Swedish", "Japanese", "Arabic"), languageChoiceBox.getItems());
        assertEquals("English", languageChoiceBox.getValue());
        assertEquals("Select Language:", languageLabel.getText());
        assertEquals("Number of items:", itemCountLabel.getText());
        assertEquals("Generate Items", generateItemsButton.getText());
        assertEquals("Calculate Total", calculateTotalButton.getText());
        assertEquals("Total Cost: 0.00", totalLabel.getText());
        assertEquals("Enter number (1-50)", itemCountField.getPromptText());
    }

    @Test
    @DisplayName("switchLanguage should update labels and refresh visible item rows")
    void switchLanguageShouldUpdateLabelsAndItemRows() throws Exception {
        runOnFxThread(() -> {
            controller.initialize();
            itemsContainer.getChildren().add(createRow("Item 1", "Price", "Quantity"));
            controller.applyLanguageSelection("Finnish");
        });

        HBox row = (HBox) itemsContainer.getChildren().get(0);
        assertEquals("Tuote 1", ((Label) row.getChildren().get(0)).getText());
        assertEquals("Hinta", ((TextField) row.getChildren().get(1)).getPromptText());
        assertEquals("Määrä", ((TextField) row.getChildren().get(2)).getPromptText());
        assertEquals("Valitse kieli:", languageLabel.getText());
        assertEquals("Määrä:", itemCountLabel.getText());
        assertEquals("Luo tuotteet", generateItemsButton.getText());
        assertEquals("Laske summa", calculateTotalButton.getText());
        assertEquals("Kokonaishinta: 0.00", totalLabel.getText());
    }

    @Test
    @DisplayName("switchLanguage should fall back to English for unknown language names")
    void switchLanguageShouldFallBackToEnglish() throws Exception {
        runOnFxThread(() -> {
            controller.initialize();
            controller.applyLanguageSelection("German");
        });

        assertEquals("Select Language:", languageLabel.getText());
        assertEquals("Total Cost: 0.00", totalLabel.getText());
    }

    @Test
    @DisplayName("showError should use the raw key if messages have not been loaded")
    void showErrorShouldUseRawKeyWhenMessagesAreNull() {
        ShoppingCartController rawController = new ShoppingCartController();
        rawController.setAlertHandler((type, title, content) -> alerts.add(new RecordedAlert(type, title, content)));

        rawController.showErrorAlert("error.invalid.number");

        assertEquals(1, alerts.size());
        assertEquals(Alert.AlertType.ERROR, alerts.get(0).type);
        assertEquals("Error", alerts.get(0).title);
        assertEquals("error.invalid.number", alerts.get(0).content);
    }

    @Test
    @DisplayName("generateItemFields should create rows for valid input")
    void generateItemFieldsShouldCreateRowsForValidInput() throws Exception {
        runOnFxThread(() -> {
            controller.initialize();
            itemCountField.setText("2");
            controller.handleGenerateItemsAction();
        });

        assertEquals(2, itemsContainer.getChildren().size());
        HBox firstRow = (HBox) itemsContainer.getChildren().get(0);
        HBox secondRow = (HBox) itemsContainer.getChildren().get(1);
        assertEquals("Item 1", ((Label) firstRow.getChildren().get(0)).getText());
        assertEquals("Item 2", ((Label) secondRow.getChildren().get(0)).getText());
        assertEquals("Price", ((TextField) firstRow.getChildren().get(1)).getPromptText());
        assertEquals("Quantity", ((TextField) firstRow.getChildren().get(2)).getPromptText());
        assertTrue(alerts.isEmpty());
    }

    @Test
    @DisplayName("generateItemFields should show an error for empty input")
    void generateItemFieldsShouldShowErrorForEmptyInput() throws Exception {
        runOnFxThread(() -> {
            controller.initialize();
            itemCountField.setText(" ");
            controller.handleGenerateItemsAction();
        });

        assertSingleAlert(Alert.AlertType.ERROR, "Error", "Please enter a number");
        assertTrue(itemsContainer.getChildren().isEmpty());
    }

    @Test
    @DisplayName("generateItemFields should show an error for invalid number format")
    void generateItemFieldsShouldShowErrorForInvalidNumber() throws Exception {
        runOnFxThread(() -> {
            controller.initialize();
            itemCountField.setText("abc");
            controller.handleGenerateItemsAction();
        });

        assertSingleAlert(Alert.AlertType.ERROR, "Error", "Invalid number format");
    }

    @Test
    @DisplayName("generateItemFields should show an error for zero or negative input")
    void generateItemFieldsShouldShowErrorForNonPositiveInput() throws Exception {
        runOnFxThread(() -> {
            controller.initialize();
            itemCountField.setText("0");
            controller.handleGenerateItemsAction();
        });

        assertSingleAlert(Alert.AlertType.ERROR, "Error", "Please enter a positive number");
    }

    @Test
    @DisplayName("generateItemFields should show an error when input exceeds the maximum allowed")
    void generateItemFieldsShouldShowErrorForTooManyItems() throws Exception {
        runOnFxThread(() -> {
            controller.initialize();
            itemCountField.setText("51");
            controller.handleGenerateItemsAction();
        });

        assertSingleAlert(Alert.AlertType.ERROR, "Error", "Invalid number format");
    }

    @Test
    @DisplayName("calculateTotal should show an error when no items exist")
    void calculateTotalShouldShowErrorWhenNoItemsExist() throws Exception {
        runOnFxThread(() -> {
            controller.initialize();
            controller.handleCalculateTotalAction();
        });

        assertSingleAlert(Alert.AlertType.ERROR, "Error", "No items to calculate. Please generate items first.");
    }

    @Test
    @DisplayName("calculateTotal should show an error for blank item fields")
    void calculateTotalShouldShowErrorForBlankFields() throws Exception {
        runOnFxThread(() -> {
            controller.initialize();
            itemsContainer.getChildren().add(createRow("Item 1", "Price", "Quantity"));
            setItemFields(createItemFieldList(new TextField(), new TextField()));
            controller.handleCalculateTotalAction();
        });

        assertSingleAlert(Alert.AlertType.ERROR, "Error", "Please fill in all price and quantity fields");
    }

    @Test
    @DisplayName("calculateTotal should show an error for invalid numbers")
    void calculateTotalShouldShowErrorForInvalidNumbers() throws Exception {
        runOnFxThread(() -> {
            controller.initialize();
            TextField price = new TextField("abc");
            TextField qty = new TextField("2");
            itemsContainer.getChildren().add(createRow("Item 1", "Price", "Quantity"));
            setItemFields(createItemFieldList(price, qty));
            controller.handleCalculateTotalAction();
        });

        assertSingleAlert(Alert.AlertType.ERROR, "Error", "Invalid number format");
    }

    @Test
    @DisplayName("calculateTotal should show an error for non-positive values")
    void calculateTotalShouldShowErrorForNonPositiveValues() throws Exception {
        runOnFxThread(() -> {
            controller.initialize();
            TextField price = new TextField("0");
            TextField qty = new TextField("2");
            itemsContainer.getChildren().add(createRow("Item 1", "Price", "Quantity"));
            setItemFields(createItemFieldList(price, qty));
            controller.handleCalculateTotalAction();
        });

        assertSingleAlert(Alert.AlertType.ERROR, "Error", "Please enter a positive number");
    }

    @Test
    @DisplayName("calculateTotal should calculate, save, and show success")
    void calculateTotalShouldCalculateAndShowSuccess() throws Exception {
        when(calculator.calculateTotal(anyList())).thenReturn(30.0);

        runOnFxThread(() -> {
            controller.initialize();
            TextField price1 = new TextField("10");
            TextField qty1 = new TextField("2");
            TextField price2 = new TextField("5");
            TextField qty2 = new TextField("2");
            itemsContainer.getChildren().add(createRow("Item 1", "Price", "Quantity"));
            itemsContainer.getChildren().add(createRow("Item 2", "Price", "Quantity"));
            setItemFields(createItemFieldList(price1, qty1, price2, qty2));
            controller.handleCalculateTotalAction();
        });

        verify(cartService).saveCart(eq(2), eq(30.0), eq("en"), anyList());
        assertEquals("Total Cost: 30.00", totalLabel.getText());
        assertSingleAlert(Alert.AlertType.INFORMATION, "Success", "Cart saved to database successfully!");
    }

    @Test
    @DisplayName("calculateTotal should show a warning when saving the cart fails")
    void calculateTotalShouldShowWarningWhenSaveFails() throws Exception {
        when(calculator.calculateTotal(anyList())).thenReturn(20.0);
        doThrow(new RuntimeException("DB down")).when(cartService)
                .saveCart(anyInt(), anyDouble(), anyString(), anyList());

        runOnFxThread(() -> {
            controller.initialize();
            TextField price = new TextField("10");
            TextField qty = new TextField("2");
            itemsContainer.getChildren().add(createRow("Item 1", "Price", "Quantity"));
            setItemFields(createItemFieldList(price, qty));
            controller.handleCalculateTotalAction();
        });

        assertEquals("Total Cost: 20.00", totalLabel.getText());
        assertSingleAlert(Alert.AlertType.WARNING, "Warning",
                "Total calculated: 20.00\nBut failed to save to database: DB down");
    }

    @Test
    @DisplayName("calculateTotal should use the selected language when saving the cart")
    void calculateTotalShouldUseSelectedLanguage() throws Exception {
        when(calculator.calculateTotal(anyList())).thenReturn(10.0);

        runOnFxThread(() -> {
            controller.initialize();
            controller.applyLanguageSelection("Japanese");
            TextField price = new TextField("10");
            TextField qty = new TextField("1");
            itemsContainer.getChildren().add(createRow("Item 1", "Price", "Quantity"));
            setItemFields(createItemFieldList(price, qty));
            controller.handleCalculateTotalAction();
        });

        verify(cartService).saveCart(eq(1), eq(10.0), eq("ja"), anyList());
    }

    private static Map<String, String> createMessages(String selectLanguage, String numItems, String generate,
                                                     String calculate, String totalCost, String price, String quantity,
                                                     String item, String invalidNumber, String positiveNumber) {
        Map<String, String> messages = new HashMap<>();
        messages.put("select.language", selectLanguage);
        messages.put("prompt.num.items", numItems);
        messages.put("btn.generate.items", generate);
        messages.put("btn.calculate.total", calculate);
        messages.put("total.cost", totalCost);
        messages.put("prompt.price", price);
        messages.put("prompt.quantity", quantity);
        messages.put("item.prompt", item);
        messages.put("error.invalid.number", invalidNumber);
        messages.put("error.positive.number", positiveNumber);
        return messages;
    }

    private void createControlsOnFxThread() throws Exception {
        runOnFxThread(() -> {
            languageChoiceBox = new ChoiceBox<>();
            itemCountField = new TextField();
            itemsContainer = new VBox();
            totalLabel = new Label();
            languageLabel = new Label();
            itemCountLabel = new Label();
            generateItemsButton = new Button();
            calculateTotalButton = new Button();

            setField(controller, "languageChoiceBox", languageChoiceBox);
            setField(controller, "itemCountField", itemCountField);
            setField(controller, "itemsContainer", itemsContainer);
            setField(controller, "totalLabel", totalLabel);
            setField(controller, "languageLabel", languageLabel);
            setField(controller, "itemCountLabel", itemCountLabel);
            setField(controller, "generateItemsButton", generateItemsButton);
            setField(controller, "calculateTotalButton", calculateTotalButton);
            return null;
        });
    }

    private HBox createRow(String labelText, String pricePrompt, String qtyPrompt) {
        Label label = new Label(labelText);
        TextField priceField = new TextField();
        priceField.setPromptText(pricePrompt);
        TextField qtyField = new TextField();
        qtyField.setPromptText(qtyPrompt);
        return new HBox(10, label, priceField, qtyField);
    }

    private void setItemFields(List<TextField[]> itemFields) throws Exception {
        setField(controller, "itemInputFields", itemFields);
    }

    private static List<TextField[]> createItemFieldList(TextField... fields) {
        List<TextField[]> itemFields = new ArrayList<>();
        for (int i = 0; i < fields.length; i += 2) {
            itemFields.add(new TextField[]{fields[i], fields[i + 1]});
        }
        return itemFields;
    }

    private void assertSingleAlert(Alert.AlertType type, String title, String content) {
        assertEquals(1, alerts.size(), "Expected exactly one captured alert");
        assertEquals(type, alerts.get(0).type);
        assertEquals(title, alerts.get(0).title);
        assertEquals(content, alerts.get(0).content);
        alerts.clear();
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    private static void runOnFxThread(ThrowingRunnable action) throws Exception {
        runOnFxThread(() -> {
            action.run();
            return null;
        });
    }

    private static <T> T runOnFxThread(Callable<T> callable) throws Exception {
        if (Platform.isFxApplicationThread()) {
            return callable.call();
        }

        AtomicReference<T> result = new AtomicReference<>();
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                result.set(callable.call());
            } catch (Throwable t) {
                error.set(t);
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(10, TimeUnit.SECONDS), "FX task did not finish in time");
        if (error.get() != null) {
            if (error.get() instanceof Exception exception) {
                throw exception;
            }
            throw new RuntimeException(error.get());
        }
        return result.get();
    }
}





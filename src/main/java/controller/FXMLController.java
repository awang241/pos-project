package controller;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

import controller.dialog.CashInputDialogController;
import data.Persistence;
import enums.PaymentType;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import model.Product;
import model.Transaction;

public class FXMLController implements Initializable {

    private static final String CASH_TEMPLATE = "$%.2f";
    private static final KeyCode CASH = KeyCode.F1;
    private static final KeyCode EFTPOS = KeyCode.F2;
    private static final KeyCode CHEQUE = KeyCode.F3;
    private static final KeyCode CASH_OUT = KeyCode.F4;
    private static final KeyCode PRINT = KeyCode.F5;
    private static final KeyCode TILL = KeyCode.F6;
    private static final KeyCode CANCEL = KeyCode.F7;
    private static final KeyCode UNCODED = KeyCode.F10;
    private static final KeyCode DELETE = KeyCode.F9;
    private static final KeyCode REFUND = KeyCode.F8;
    private static final KeyCode HOTKEY_1 = KeyCode.F12;

    @FXML Pane cashPane;
    @FXML Label cashLabel;
    @FXML Pane changePane;
    @FXML Label changeLabel;

    @FXML
    private TextField textField;

    @FXML
    private TableView<Product> itemTable;

    @FXML
    private TableColumn<Product, String> itemColumn;

    @FXML
    private TableColumn<Product, Float> unitPriceColumn;

    @FXML
    private TableColumn<Product, Integer> quantityColumn;

    @FXML
    private TableColumn<Product, Number> priceColumn;

    @FXML
    private Label subtotalLabel;

    @FXML
    private Label cancelLabel;

    private Persistence persistence;

    private boolean keepOldSubtotal = false;

    private ObservableMap<Product, Integer> itemCounts = FXCollections.observableHashMap();

    private ObservableList<Product> items;

    private int uncodedCount = 0;

    public FXMLController(Persistence persistence) {
        this.persistence = persistence;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        persistence = new Persistence();
        itemTable.setPlaceholder(new Label(""));
        items = FXCollections.observableArrayList(itemCounts.keySet());

        itemTable.setMouseTransparent(true);
        itemTable.setFocusTraversable(false);

        itemColumn.setCellValueFactory(data -> Bindings.createStringBinding(()-> data.getValue().getName()));

        unitPriceColumn.setCellValueFactory(data -> Bindings.createFloatBinding(()->
                isDiscounted(data.getValue()) ? data.getValue().getDrp(): data.getValue().getPrice(),
                itemCounts).asObject());

        quantityColumn.setCellValueFactory(cellData -> Bindings.valueAt(itemCounts, cellData.getValue()));

        priceColumn.setCellValueFactory(data -> Bindings.floatValueAt(itemCounts,
                data.getValue()).multiply(isDiscounted(data.getValue())? data.getValue().getDrp(): data.getValue().getPrice()));

        itemCounts.addListener((MapChangeListener<Product,Integer>) change -> {
            boolean removed = change.wasRemoved();
            if (removed != change.wasAdded()) {
                if (removed) {
                    items.remove(change.getKey());
                } else {
                    items.add(change.getKey());
                    cancelLabel.setVisible(false);
                    cashPane.setVisible(false);
                    changePane.setVisible(false);
                }
            }
            if (!keepOldSubtotal) {
                subtotalLabel.setText(String.format(CASH_TEMPLATE, calculateSubtotal()));
            }
        });
        itemTable.setItems(items);
    }

    private boolean isDiscounted(Product product) {
        if (!itemCounts.containsKey(product) || product.getDrp() <= 0f) {
            return false;
        } else {
            return itemCounts.get(product) >= product.getUnit();
        }
    }

    private float calculateSubtotal() {
        float total = 0f;
        for (Map.Entry<Product, Integer> item: itemCounts.entrySet()) {
            boolean discounted = (item.getValue() >= item.getKey().getUnit()) && (item.getKey().getDrp() > 0);
            float price = discounted ? item.getKey().getDrp(): item.getKey().getPrice();
            total += price * item.getValue();
        }
        return total;
    }


    /**
     * Clears all items from the transaction and resets the uncoded product counter.
     */
    private void resetFields() {
        keepOldSubtotal = true;
        itemCounts.clear();
        uncodedCount = 0;
        keepOldSubtotal = false;
    }

    public void addItemByBarcode(String barcode){
        try {
            Optional<Product> result = persistence.findProductByBarcode(barcode);
            if (!result.isPresent()) {
                throw new IllegalArgumentException("No product with that barcode exists");
            }
            Product product = result.get();
            int quantity = product.isCarton() ? product.getUnit(): 1;

            if (itemCounts.isEmpty()) {
                cashPane.setVisible(false);
                changePane.setVisible(false);
            }

            if (itemCounts.containsKey(product)) {
                itemCounts.put(product, itemCounts.get(product) + quantity);
            } else {
                itemCounts.put(product, quantity);
            }

            cashPane.setVisible(false);
            changePane.setVisible(false);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves the current transaction to the database.
     *
     * The database will record the transaction, the items in the transaction, including the items' stock levels. If
     * the transaction is a refund, the subtotal is negative and the stock levels are increased instead.
     * @param type The payment type of the transaction
     */
    public void saveTransaction(PaymentType type) {
        Transaction transaction = new Transaction(new HashMap<>(itemCounts), LocalDateTime.now(), type);
        try {
            persistence.saveTransaction(transaction);
            for (Map.Entry<Product, Integer> item: itemCounts.entrySet()) {
                int refundMultiplier = type == PaymentType.REFUND ? 1: -1;
                item.getKey().addStock(refundMultiplier * item.getValue());
                persistence.updateProduct(item.getKey());
            }

        } catch (SQLException exception) {
            Dialog<ButtonType> dialog = new Alert(Alert.AlertType.WARNING);
            dialog.setContentText("Error recording transaction");
            dialog.showAndWait();
        }
    }

    /**
     * Event handler for key press/release events on the text input field.
     * @param e the key event that was pressed
     */
    public void textFieldKeyReleaseHandler(KeyEvent e) {
        if (e.getCode() == KeyCode.ENTER) {
            addItemByBarcode(textField.getText());
            textField.setText("");
        } else if (e.getCode() == CASH && !itemCounts.isEmpty()){
            completeTransaction(PaymentType.CASH);
        } else if (e.getCode() == EFTPOS && !itemCounts.isEmpty()) {
            completeTransaction(PaymentType.EFTPOS);
        } else if (e.getCode() == CASH_OUT) {
            Optional<Float> cashAmount = showCashInputDialog(0f);
            cashAmount.ifPresent(aFloat -> itemCounts.put(Product.createCashProduct(aFloat), 1));
        } else if (e.getCode() == CANCEL && !itemCounts.isEmpty()) {
            cancelLabel.setVisible(true);
            resetFields();
        } else if (e.getCode() == TILL) {
            Print.Kick();
        } else if (e.getCode() == PRINT) {
            Optional<Transaction> transaction = persistence.findLastInsertedTransaction();
            transaction.ifPresent(t -> Print.printSheet(t));
        } else if (e.getCode() == UNCODED) {
            Optional<Float> cash = showCashInputDialog(0.01f);
            if (cash.isPresent()) {
                Product uncoded = Product.createUncodedProduct(uncodedCount + 1, cash.get());
                itemCounts.put(uncoded, 1);
                uncodedCount++;
            }
        } else if (e.getCode() == DELETE) {
            Product deleted = items.get(items.size() - 1);
            itemCounts.remove(deleted);
        } else if (e.getCode() == REFUND && !itemCounts.isEmpty()) {
            completeTransaction(PaymentType.REFUND);
        }
    }

    /**
     * Saves the current transaction to the database
     * @param type
     */
    public void completeTransaction(PaymentType type) {
        boolean accepted = false;
        float cash = 0f;
        if (type == PaymentType.EFTPOS || type == PaymentType.CHEQUE) {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            dialog.getDialogPane().setContentText("Waiting for EFTPOS transaction");
            Optional<ButtonType> buttonType = dialog.showAndWait();
            if (buttonType.isPresent() && ButtonType.OK.equals(buttonType.get())) {
                accepted = true;
                cash = calculateSubtotal();
            }
        } else if (type == PaymentType.REFUND) {
            cash = -1 * calculateSubtotal();
            accepted = true;
        } else {
            Optional<Float> cashResponse = showCashInputDialog(calculateSubtotal());
            if (cashResponse.isPresent()) {
                accepted = true;
                cash = cashResponse.get();
            }
        }
        if (accepted) {
            cashPane.setVisible(true);
            changePane.setVisible(true);
            cashLabel.setText(String.format(CASH_TEMPLATE, cash));
            changeLabel.setText(String.format(CASH_TEMPLATE, cash - calculateSubtotal()));
            saveTransaction(type);
            resetFields();
        }
    }

    /**
     * Displays a dialog for users to input a cash amount.
     * @param minCash The minimum cash level that is accepted by the dialog.
     * @return The cash amount input by the user, or an empty optional if the user cancels the dialog.
     */
    public Optional<Float> showCashInputDialog(float minCash) {
        Dialog<Float> dialog;
        Optional<Float> input = Optional.empty();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/cashInputDialog.fxml"));
            dialog = loader.load();
            CashInputDialogController controller = loader.getController();
            controller.setMinCashInCents(minCash);
            input = dialog.showAndWait();
        } catch (IOException error) {
            error.printStackTrace();
        }
        return input;
    }
}
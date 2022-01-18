package controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.*;

import controller.dialog.CashInputDialogController;
import data.Persistence;
import enums.PaymentType;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import model.Product;
import model.Transaction;
import model.TransactionItem;

public class POSController implements Initializable {

    private static final String CASH_TEMPLATE = "$%.2f";
    private static final KeyCode CASH = KeyCode.F1;
    private static final KeyCode EFTPOS = KeyCode.F2;
    private static final KeyCode CHEQUE = KeyCode.F3;
    private static final KeyCode CASH_OUT = KeyCode.F4;
    private static final KeyCode PRINT = KeyCode.F5;
    private static final KeyCode TILL = KeyCode.F6;
    private static final KeyCode CANCEL = KeyCode.F7;
    private static final KeyCode REFUND = KeyCode.F8;
    private static final KeyCode DELETE = KeyCode.F9;
    private static final KeyCode UNCODED = KeyCode.F10;
    private static final KeyCode ADD_STOCK = KeyCode.F11;
    private static final KeyCode HOTKEY_1 = KeyCode.F12;

    @FXML Pane paymentPane;
    @FXML Label paymentLabel;
    @FXML private Label typeLabel;
    @FXML Pane changePane;
    @FXML Label changeLabel;
    @FXML
    private TextField textField;
    @FXML
    private TableView<TransactionItem> itemTable;
    @FXML
    private TableColumn<TransactionItem, String> itemColumn;
    @FXML
    private TableColumn<TransactionItem, String> unitPriceColumn;
    @FXML
    private TableColumn<TransactionItem, Integer> quantityColumn;
    @FXML
    private TableColumn<TransactionItem, String> priceColumn;
    @FXML
    private Label subtotalLabel;
    @FXML
    private Label cancelLabel;

    private final Persistence persistence;

    private boolean keepOldSubtotal = false;

    private final Set<Product> productIndex = new HashSet<>();

    private final ObservableList<TransactionItem> items = FXCollections.observableArrayList(param -> new Observable[]{param.quantityProperty(), param.priceProperty()});

    private int uncodedCount = 0;

    public POSController(Persistence persistence) {
        this.persistence = persistence;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        itemTable.setPlaceholder(new Label(""));
        itemTable.setMouseTransparent(true);

        itemColumn.setCellValueFactory(data -> Bindings.createStringBinding(()-> data.getValue().getProductName()));

        unitPriceColumn.setCellValueFactory(data -> Bindings.createStringBinding(
                ()-> NumberFormat.getCurrencyInstance().format(data.getValue().getPrice()), items));

        quantityColumn.setCellValueFactory(cellData -> Bindings.createIntegerBinding(
                () -> cellData.getValue().getQuantity(), items)
            .asObject());

        priceColumn.setCellValueFactory(data -> Bindings.createStringBinding(()->{
                TransactionItem item = data.getValue();
                BigDecimal totalPrice = item.getPrice().multiply(new BigDecimal(item.getQuantity()));
                return NumberFormat.getCurrencyInstance().format(totalPrice);
            }, items));

        items.addListener((ListChangeListener<TransactionItem>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    cancelLabel.setVisible(false);
                    paymentPane.setVisible(false);
                    changePane.setVisible(false);
                }
                if (!keepOldSubtotal) {
                    subtotalLabel.setText(String.format(CASH_TEMPLATE, calculateSubtotal()));
                }
            }
        });
        itemTable.setItems(items);
    }

    public BigDecimal calculateSubtotal() {
        return items.stream()
                .map(item -> (item.getPrice().multiply(new BigDecimal(item.getQuantity()))))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    /**
     * Clears all items from the transaction and resets the uncoded product counter.
     */
    private void resetFields() {
        keepOldSubtotal = true;
        items.clear();
        uncodedCount = 0;
        keepOldSubtotal = false;
    }

    public void addItemByBarcode(String barcode){
        try {
            Optional<Product> result = persistence.findProductByBarcode(barcode);
            if (result.isEmpty()) {
                throw new IllegalArgumentException("No product with that barcode exists");
            }
            Product product = result.get();
            int quantity = product.isCarton() ? product.getUnitsPerCarton(): 1;

            if (items.isEmpty()) {
                paymentPane.setVisible(false);
                changePane.setVisible(false);
            }

            TransactionItem newItem = new TransactionItem(items.size(), product.getName(), quantity, product.getDiscountCode(), product.getRetailPrice());
            Optional<TransactionItem> results = items.stream()
                    .filter(item -> item.getProductName().equals(product.getName()))
                    .findAny();
            if (results.isPresent()) {
                int total = results.get().getQuantity() + quantity;
                results.get().setQuantity(total);
                if (total >= product.getUnitsPerCarton() && product.getDrp().compareTo(BigDecimal.ZERO) > 0) {
                    results.get().setPrice(product.getDrp());
                }
            } else {
                items.add(newItem);
                productIndex.add(product);
            }

            paymentPane.setVisible(false);
            changePane.setVisible(false);
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
    public void saveTransaction(PaymentType type, BigDecimal payment) {
        Transaction transaction = new Transaction(items, LocalDateTime.now(), type, payment);
        try {
            persistence.saveTransaction(transaction);
            for (TransactionItem item: items) {
                int refundMultiplier = type == PaymentType.REFUND ? 1: -1;
                Product product = productIndex.stream()
                                .filter(prod -> prod.getName().equals(item.getProductName()))
                                .findAny().orElseThrow();
                product.addStock(refundMultiplier * item.getQuantity());
                persistence.updateProduct(product);
            }

        } catch (SQLException | NoSuchElementException e) {
            Dialog<ButtonType> dialog = new Alert(Alert.AlertType.WARNING);
            dialog.setContentText("Error recording transaction");
            dialog.showAndWait();
        }
    }

    public void cashButtonHandler() {
        if (!items.isEmpty()) {
            if (completeTransaction(PaymentType.CASH)) {
                Print.Kick();
            }
        }
    }

    public void eftposButtonHandler() {
        if (!items.isEmpty()) {
            completeTransaction(PaymentType.EFTPOS);
        }

    }

    public void cashOutButtonHandler() {
        Optional<BigDecimal> cashAmount = showCashInputDialog(BigDecimal.ONE);
        cashAmount.ifPresent(aFloat -> {
            Optional<TransactionItem> cashItem = items.stream()
                    .filter(item -> item.getProductName().equals(TransactionItem.CASH_OUT))
                    .findAny();
            if (cashItem.isPresent()) {
                cashItem.get().setPrice(cashItem.get().getPrice().add(cashAmount.get()));
            } else {
                items.add(TransactionItem.createCashProduct(cashAmount.get()));
            }
        });
    }

    public void cancelButtonHandler() {
        if (!items.isEmpty()) {
            cancelLabel.setVisible(true);
            resetFields();
        }
    }

    public void tillButtonHandler() {
        Print.Kick();
    }

    public void printButtonHandler() {
        Optional<Transaction> transaction = persistence.findLastInsertedTransaction();
        transaction.ifPresent(Print::printSheet);
    }

    public void uncodedButtonHandler() {
        Optional<BigDecimal> cash = showCashInputDialog(new BigDecimal("0.01"));
        if (cash.isPresent()) {
            TransactionItem uncoded = TransactionItem.createUncodedProduct(uncodedCount + 1, cash.get());
            items.add(uncoded);
            uncodedCount++;
        }
    }

    public void deleteButtonHandler() {
        if (!items.isEmpty()) {
            TransactionItem removed = items.remove(items.size() - 1);
            if (items.stream().noneMatch(item -> item.getProductName().equals(removed.getProductName()))) {
                productIndex.removeIf(product -> removed.getProductName().equals(product.getName()));
            }
        }
    }

    public void refundButtonHandler() {
        if (!items.isEmpty()) {
            completeTransaction(PaymentType.REFUND);
        }
    }

    public void addStockButtonHandler() {
        System.out.println("ok");
    }

    /**
     * Event handler for key press/release events on the text input field.
     * @param e the key event that was pressed
     */
    public void textFieldKeyReleaseHandler(KeyEvent e) {
        if (e.getCode() == KeyCode.ENTER) {
            addItemByBarcode(textField.getText());
            textField.setText("");
        } else if (e.getCode() == CASH){
            cashButtonHandler();
        } else if (e.getCode() == EFTPOS) {
            eftposButtonHandler();
        } else if (e.getCode() == CASH_OUT) {
            cashOutButtonHandler();
        } else if (e.getCode() == CANCEL) {
            cancelButtonHandler();
        } else if (e.getCode() == TILL) {
            tillButtonHandler();
        } else if (e.getCode() == PRINT) {
            printButtonHandler();
        } else if (e.getCode() == UNCODED) {
            uncodedButtonHandler();
        } else if (e.getCode() == DELETE) {
            deleteButtonHandler();
        } else if (e.getCode() == REFUND) {
            refundButtonHandler();
        } else if (e.getCode() == ADD_STOCK) {
            addStockButtonHandler();
        }
    }

    /**
     * Concludes the transaction and complete
     * @param type
     * @return true if the transaction was completed successfully; false otherwise
     */
    public boolean completeTransaction(PaymentType type) {
        BigDecimal payment = BigDecimal.ZERO;
        boolean accepted = false;
        if (type == PaymentType.EFTPOS || type == PaymentType.CHEQUE) {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            dialog.getDialogPane().setContentText("Waiting for EFTPOS transaction");
            Optional<ButtonType> buttonType = dialog.showAndWait();
            if (buttonType.isPresent() && ButtonType.OK.equals(buttonType.get())) {
                payment = calculateSubtotal();
                accepted = true;
            }
        } else if (type == PaymentType.REFUND) {
            payment = calculateSubtotal();
            accepted = true;
        } else if (type == PaymentType.CASH) {
            Optional<BigDecimal> cashResponse = showCashInputDialog(calculateSubtotal());
            if (cashResponse.isPresent()) {
                payment = cashResponse.get();
                accepted = true;
            }
        }

        if (accepted) {
            paymentLabel.setText(String.format(CASH_TEMPLATE, payment));
            typeLabel.setText(type.toString());
            paymentPane.setVisible(true);
            if (type != PaymentType.REFUND) {
                changePane.setVisible(true);
                changeLabel.setText(String.format(CASH_TEMPLATE, payment.subtract(calculateSubtotal())));
            }
            saveTransaction(type, payment);
            resetFields();
        }
        return accepted;

    }

    /**
     * Displays a dialog for users to input a cash amount.
     * @param minCash The minimum cash level that is accepted by the dialog.
     * @return The cash amount input by the user, or an empty optional if the user cancels the dialog.
     */
    public Optional<BigDecimal> showCashInputDialog(BigDecimal minCash) {
        Dialog<BigDecimal> dialog;
        Optional<BigDecimal> input = Optional.empty();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dialog/CashInputDialog.fxml"));
            dialog = loader.load();
            CashInputDialogController controller = loader.getController();
            controller.setMinCash(minCash);
            input = dialog.showAndWait();
        } catch (IOException error) {
            error.printStackTrace();
        }
        return input;
    }
}
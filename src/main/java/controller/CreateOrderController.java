package controller;

import data.Persistence;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.IntegerStringConverter;
import model.Order;
import model.Product;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

public class CreateOrderController implements Initializable {

    @FXML
    private Order order;
    @FXML
    private Label supplierLabel;
    @FXML
    private Label orderDateLabel;
    @FXML
    private Label paymentDateLabel;
    @FXML
    private Label deliveryDateLabel;
    @FXML
    private Label totalPriceLabel;
    @FXML
    private TableView<Product> productTable;
    @FXML
    private TableColumn<Product, String> productColumn;
    @FXML
    private TableColumn<Product, String> codeColumn;
    @FXML
    private TableColumn<Product, Integer> reqStockColumn;
    @FXML
    private TableColumn<Product, Integer> currentStockColumn;
    @FXML
    private TableColumn<Product, Integer> qtyColumn;
    @FXML
    private TableColumn<Product, String> priceColumn;
    @FXML
    private TableColumn<Product, String> totalCostColumn;

    private final Persistence persistence;
    private ObservableList<Product> products;
    private final BooleanProperty deleteFlag = new SimpleBooleanProperty(false);

    public CreateOrderController(Persistence persistence, Order config) {
        this.persistence = persistence;
        this.order = config;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        supplierLabel.setText(order.getSupplierCode());
        orderDateLabel.setText(order.getOrderDate().toString());
        deliveryDateLabel.setText(order.getDeliveryDate().toString());
        paymentDateLabel.setText(order.getPaymentDate().toString());

        productColumn.setCellValueFactory((data) -> Bindings.createStringBinding(() -> data.getValue().getName()));
        reqStockColumn.setCellValueFactory((data) -> Bindings.createIntegerBinding(() -> data.getValue().getRequiredStock()).asObject());
        currentStockColumn.setCellValueFactory((data) -> Bindings.createIntegerBinding(() -> data.getValue().getCurrentStock()).asObject());
        priceColumn.setCellValueFactory((data) -> Bindings.createStringBinding(() -> {
            BigDecimal price = data.getValue().getWholesalePrice().setScale(2, RoundingMode.HALF_UP);
            return price.toString();
        }));
        totalCostColumn.setCellValueFactory((data) -> Bindings.createStringBinding(() -> {
            Product product = data.getValue();
            BigDecimal totalCost = product.getWholesalePrice().multiply(new BigDecimal(product.getRequiredCartons()));
            return totalCost.setScale(2, RoundingMode.HALF_UP).toString();
        }));
        qtyColumn.setCellValueFactory((data) -> Bindings.createIntegerBinding(() -> data.getValue().getRequiredCartons()).asObject());
        qtyColumn.setCellFactory((col) -> new TextFieldTableCell<>(new IntegerStringConverter()));
        qtyColumn.setOnEditCommit((event -> event.getRowValue().setRequiredCartons(event.getNewValue())));

        products = FXCollections.observableArrayList(persistence.findProductBySupplier(order.getSupplierCode()));
        productTable.setItems(products);
    }

    public ObservableValue<Boolean> getDeleteFlag() {
        return deleteFlag;
    }

    public void delete() {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this order? your changes will not be saved.");
        Optional<ButtonType> returnType = confirmDialog.showAndWait();
        if (returnType.isPresent() && returnType.get().equals(ButtonType.OK)) {
            deleteFlag.setValue(true);
        }
    }

    /**
     * Saves the order to the database and shows a confirmation message to the user.
     */
    public void saveOrder() {
        try {
            for (Product product: products) {
                if (product.getRequiredCartons() > 0) {
                    order.addItems(product);
                }
            }
            persistence.saveOrder(order);
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Order successfully created.");
            alert.showAndWait();
            deleteFlag.setValue(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

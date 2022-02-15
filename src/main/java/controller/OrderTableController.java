package controller;

import javafx.beans.binding.Bindings;
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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

public class OrderTableController implements Initializable {

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

    private final ObservableList<Product> products;
    private final boolean editable;
    private final boolean showStock;

    public OrderTableController(List<Product> products, Order config, boolean editable, boolean showStock) {
        this.products = FXCollections.observableArrayList(products);
        this.order = config;
        this.editable = editable;
        this.showStock = showStock;
    }

    public OrderTableController(List<Product> products, Order config) {
        this(products, config, false, false);
    }

    public List<Product> getProducts() {
        return Collections.unmodifiableList(products);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        supplierLabel.setText(order.getSupplierCode());
        orderDateLabel.setText(order.getOrderDate().toString());
        deliveryDateLabel.setText(order.getDeliveryDate().toString());
        paymentDateLabel.setText(order.getPaymentDate().toString());

        productColumn.setCellValueFactory((data) -> Bindings.createStringBinding(() -> data.getValue().getName()));
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

        if (editable) {
            qtyColumn.setCellFactory((col) -> new TextFieldTableCell<>(new IntegerStringConverter()));
            qtyColumn.setOnEditCommit((event -> {
                if (event.getNewValue() < 0) {
                    Object a = event.getSource();
                    event.consume();
                } else {
                    event.getRowValue().setRequiredCartons(event.getNewValue());
                    totalPriceLabel.setText(calculateTotal(products).toString());
                }
            }));
        }
        if (showStock) {
            reqStockColumn.setCellValueFactory((data) -> Bindings.createIntegerBinding(() -> data.getValue().getWeeklyNeededStock()).asObject());
            currentStockColumn.setCellValueFactory((data) -> Bindings.createIntegerBinding(() -> data.getValue().getCurrentStock()).asObject());
        } else {
            productTable.getColumns().removeIf((column) -> column.equals(reqStockColumn) || column.equals(currentStockColumn));
        }

        productTable.setItems(products);
        productTable.getSortOrder().add(productColumn);
        totalPriceLabel.setText(calculateTotal(products).toString());
    }

    private BigDecimal calculateTotal(Collection<Product> products) {
        return products.stream()
                .map(product -> product.getWholesalePrice().multiply(new BigDecimal(product.getRequiredCartons())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }
}

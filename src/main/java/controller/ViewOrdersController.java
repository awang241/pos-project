package controller;

import data.Persistence;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import model.Order;
import model.OrderItem;
import model.Product;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class ViewOrdersController implements Initializable {


    private final Persistence persistence;
    private final ObservableList<Order> orders = FXCollections.observableArrayList();
    @FXML
    private TableView<Order> ordersTable;
    @FXML
    private TableColumn<Order, String> supplierColumn;
    @FXML
    private TableColumn<Order, LocalDate> orderDateColumn;
    @FXML
    private TableColumn<Order, LocalDate> paymentDateColumn;
    @FXML
    private TableColumn<Order, LocalDate> deliveryDateColumn;
    @FXML
    private TableColumn<Order, BigDecimal> totalColumn;

    public ViewOrdersController(Persistence persistence) {
        this.persistence = persistence;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        supplierColumn.setCellValueFactory((data) -> Bindings.createStringBinding(() -> data.getValue().getSupplierCode()));
        orderDateColumn.setCellValueFactory((data) -> Bindings.createObjectBinding(() -> data.getValue().getOrderDate()));
        paymentDateColumn.setCellValueFactory((data) -> Bindings.createObjectBinding(() -> data.getValue().getPaymentDate()));
        deliveryDateColumn.setCellValueFactory((data) -> Bindings.createObjectBinding(() -> data.getValue().getDeliveryDate()));
        totalColumn.setCellValueFactory((data) -> Bindings.createObjectBinding(() -> {
            List<OrderItem> items = data.getValue().getItems();
            BigDecimal total = items.stream()
                .map((item) -> item.getPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            return total.setScale(2, RoundingMode.HALF_UP);
        }));
        orders.addAll(persistence.findAllOrders());
        ordersTable.setItems(orders);
    }

    public void tableClickHandler(MouseEvent event) {
        if (event.getButton() == MouseButton.SECONDARY || event.getClickCount() == 2) {
            Order order = ordersTable.getSelectionModel().getSelectedItem();
            Dialog<ButtonType> orderItemDialog = new Dialog<>();
            DialogPane pane = new DialogPane();
            TableView<OrderItem> itemTable = new TableView<>(FXCollections.observableArrayList(order.getItems()));
            TableColumn<OrderItem, String> itemColumn = new TableColumn<>();
            TableColumn<OrderItem, BigDecimal> priceColumn = new TableColumn<>();
            TableColumn<OrderItem, Integer> qtyColumn = new TableColumn<>();
            TableColumn<OrderItem, BigDecimal> totalColumn = new TableColumn<>();

            itemColumn.setText("Item");
            itemColumn.setCellValueFactory((data) -> new SimpleStringProperty(data.getValue().product));
            priceColumn.setCellValueFactory((data) -> new SimpleObjectProperty<>(data.getValue().getPrice().setScale(2, RoundingMode.HALF_UP)));
            priceColumn.setText("Price");
            qtyColumn.setCellValueFactory((data) -> new SimpleIntegerProperty(data.getValue().quantity).asObject());
            qtyColumn.setText("Quantity");
            totalColumn.setCellValueFactory((data) -> {
                OrderItem item = data.getValue();
                BigDecimal total = item.getPrice().multiply(new BigDecimal(item.getQuantity()));
                return new SimpleObjectProperty<>(total.setScale(2, RoundingMode.HALF_UP));
            });
            totalColumn.setText("Total");

            itemTable.getColumns().addAll(itemColumn, priceColumn, qtyColumn, totalColumn);
            itemTable.getSortOrder().add(itemColumn);
            pane.getButtonTypes().add(ButtonType.CLOSE);
            pane.setContent(itemTable);
            orderItemDialog.setDialogPane(pane);
            orderItemDialog.showAndWait();
        }
    }
}

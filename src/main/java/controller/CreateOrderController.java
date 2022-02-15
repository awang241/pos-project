package controller;

import controller.OrderTableController;
import data.Persistence;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.util.converter.IntegerStringConverter;
import model.Order;
import model.OrderItem;
import model.Product;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.*;

public class CreateOrderController implements Initializable {

    @FXML
    private BorderPane orderPane;

    private final Order order;
    private final Persistence persistence;
    private final BooleanProperty deleteFlag = new SimpleBooleanProperty(false);
    private OrderTableController orderTableController;

    public CreateOrderController(Persistence persistence, Order config) {
        this.persistence = persistence;
        this.order = config;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            List<Product> products = new ArrayList<>(persistence.findProductBySupplier(order.getSupplierCode()));
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/OrderTable.fxml"));
            loader.setControllerFactory((controller) -> new OrderTableController(products, order, true, true));
            Node orderTable = loader.load();
            orderTableController = loader.getController();
            orderPane.setCenter(orderTable);
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, e.getLocalizedMessage()).showAndWait();
            e.printStackTrace();
        }
    }

    public ObservableValue<Boolean> getDeleteFlag() {
        return deleteFlag;
    }

    public void delete(Event event) {
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
            List<Product> products = orderTableController.getProducts();
            for (Product product: products) {
                if (product.getRequiredCartons() > 0) {
                    OrderItem item = new OrderItem(product.getRequiredCartons(), -1, product.getWholesalePrice(), product.getName());
                    order.addItem(item);
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

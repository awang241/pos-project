package controller.dialog;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import model.Order;

import java.net.URL;
import java.time.LocalDate;
import java.util.*;

public class CreateOrderDialogController implements Initializable {

    @FXML
    private DatePicker orderDatePicker;
    @FXML
    private DatePicker paymentDatePicker;
    @FXML
    private DatePicker deliveryDatePicker;
    @FXML
    private ComboBox<String> supplierComboBox;
    @FXML
    private DialogPane dialogPane;
    private final SortedSet<String> suppliers;

    public CreateOrderDialogController(Set<String> suppliers) {
        this.suppliers = new TreeSet<>(suppliers);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        orderDatePicker.setValue(LocalDate.now());
        paymentDatePicker.setValue(LocalDate.now());
        deliveryDatePicker.setValue(LocalDate.now());
        supplierComboBox.getItems().add(Order.ALL_SUPPLIERS);
        supplierComboBox.getItems().addAll(suppliers);
        supplierComboBox.setValue(Order.ALL_SUPPLIERS);
    }

    public Order createOrderTemplate(ButtonType buttonType) {
        if (buttonType.equals(ButtonType.CANCEL)) {
            return null;
        } else {
            return new Order(orderDatePicker.getValue(), paymentDatePicker.getValue(), deliveryDatePicker.getValue(), supplierComboBox.getValue());
        }
    }
}

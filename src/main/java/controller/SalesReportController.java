package controller;

import data.Persistence;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.Callback;
import model.Product;

import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class SalesReportController implements Initializable {

    private final Persistence persistence;

    private LocalDate startDate = LocalDate.now().minusDays(30);
    private LocalDate endDate = LocalDate.of(2021, 12, 10);//LocalDate.now();

    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance();
    private static final String LABEL_TEMPLATE = "Displaying sales from %s to %s";

    @FXML
    private TableView<Product> salesTable;
    @FXML
    private TableColumn<Product, String> productColumn;
    @FXML
    private TableColumn<Product, BigDecimal> priceColumn;
    @FXML
    private TableColumn<Product, Integer> numSoldColumn;
    @FXML
    private TableColumn<Product, BigDecimal> totalColumn;
    @FXML
    private TableColumn<Product, BigDecimal> profitColumn;
    @FXML
    private TableColumn<Product, BigDecimal> avgSalesColumn;

    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private Label dateLabel;


    public SalesReportController(Persistence persistence) {
        this.persistence = persistence;
    }

    @Override
    public void initialize(URL URL, ResourceBundle rb) {
        priceColumn.setCellValueFactory((product) -> Bindings.createObjectBinding(() -> product.getValue().getRetailPrice()));
        priceColumn.setCellFactory(currencyFormat());
        productColumn.setCellValueFactory((product) -> Bindings.createStringBinding(() -> product.getValue().getName()));
        numSoldColumn.setCellValueFactory((product) -> Bindings.createIntegerBinding(() -> product.getValue().getCurrentStock()).asObject());
        totalColumn.setCellValueFactory((product) -> Bindings.createObjectBinding(() -> product.getValue().getDrp()));
        totalColumn.setCellFactory(currencyFormat());
        //profitColumn.setCellValueFactory((product) -> Bindings.createStringBinding(() -> product.getValue().getStockLevel()).asObject());

        startDatePicker.setValue(startDate);
        endDatePicker.setValue(endDate);
        loadSales();
    }

    private <S, T extends Number> Callback<TableColumn<S, T>, TableCell<S, T>> currencyFormat() {
        return column -> new TableCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                if (!empty && item != null) {
                    setText(CURRENCY_FORMAT.format(item));
                } else {
                    setText("");
                }
            }
        };
    }

    /**
     * Loads sales from between the given dates and displays them
     */
    public void loadSales() {
        startDate = startDatePicker.getValue();
        endDate = endDatePicker.getValue();


        ObservableList<Product> sales = FXCollections.observableArrayList(persistence.findSalesBetweenDates(startDate.atStartOfDay(), endDate.atStartOfDay()));
        salesTable.setItems(sales);

        dateLabel.setText(String.format(LABEL_TEMPLATE, startDate, endDate));
    }
}

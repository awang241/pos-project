package controller;

import data.Persistence;
import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.Callback;
import model.Sales;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ResourceBundle;

public class GeneralSalesReportController implements Initializable {

    private final Persistence persistence;

    private LocalDate startDate = LocalDate.now().minusMonths(1);
    private LocalDate endDate = LocalDate.now();

    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance();
    private static final String LABEL_TEMPLATE = "Displaying sales from %s to %s";

    @FXML
    private TableView<Sales> salesTable;
    @FXML
    private TableColumn<Sales, String> productColumn;
    @FXML
    private TableColumn<Sales, BigDecimal> priceColumn;
    @FXML
    private TableColumn<Sales, Integer> numSoldColumn;
    @FXML
    private TableColumn<Sales, BigDecimal> totalColumn;
    @FXML
    private TableColumn<Sales, BigDecimal> profitColumn;
    @FXML
    private TableColumn<Sales, BigDecimal> avgSalesColumn;

    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private Label dateLabel;


    public GeneralSalesReportController(Persistence persistence) {
        this.persistence = persistence;
    }

    @Override
    public void initialize(URL URL, ResourceBundle rb) {
        priceColumn.setCellValueFactory((data) -> Bindings.createObjectBinding(() -> data.getValue().getSellPrice()));
        priceColumn.setCellFactory(currencyFormat());
        productColumn.setCellValueFactory((data) -> Bindings.createStringBinding(() -> data.getValue().getProductName()));
        numSoldColumn.setCellValueFactory((data) -> Bindings.createIntegerBinding(() -> data.getValue().getQuantity()).asObject());
        totalColumn.setCellValueFactory((data) -> Bindings.createObjectBinding(() -> data.getValue().getTotalSales()));
        totalColumn.setCellFactory(currencyFormat());
        profitColumn.setCellValueFactory((data) -> new SimpleObjectProperty<>(data.getValue().getTotalProfit()));
        profitColumn.setCellFactory(currencyFormat());

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

        ObservableList<Sales> sales = FXCollections.observableArrayList(persistence.findSalesBetweenDates(startDate.atStartOfDay(), endDate.atStartOfDay()));
        salesTable.setItems(sales);

        dateLabel.setText(String.format(LABEL_TEMPLATE, startDate, endDate));

        avgSalesColumn.setCellValueFactory((data) -> {
            long numDays = ChronoUnit.DAYS.between(startDate, endDate);
            BigDecimal numWeeks = new BigDecimal(numDays).divide(new BigDecimal(7), 2, RoundingMode.HALF_UP);
            BigDecimal avgSales = new BigDecimal(data.getValue().getQuantity()).divide(numWeeks, 2, RoundingMode.HALF_UP);
            return new SimpleObjectProperty<>(avgSales);
        });
    }
}

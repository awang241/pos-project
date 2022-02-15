package controller;

import com.google.common.collect.Table;
import data.Persistence;
import enums.Period;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;


import java.net.URL;
import java.time.LocalDate;
import java.time.Month;
import java.util.Map;
import java.util.ResourceBundle;

public class PeriodicSalesReportController implements Initializable {

    private final static String MONTHLY = "Monthly";
    private final static String WEEKLY = "Weekly";

    private final Persistence persistence;

    private Table<String, Integer, Integer> sales;

    @FXML
    private TableView<String> salesTable;

    @FXML
    private TableColumn<String, String> nameColumn;

    @FXML
    private TableColumn<String, Integer> totalQtyColumn;

    @FXML
    private ComboBox<String> periodCombo;

    @FXML
    private ComboBox<Integer> yearCombo;

    @FXML
    private Label resultsLabel;

    public PeriodicSalesReportController(Persistence persistence) {
        this.persistence = persistence;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        nameColumn.setCellValueFactory((data) -> new SimpleStringProperty(data.getValue()));
        totalQtyColumn.setCellValueFactory((data) -> Bindings.createIntegerBinding(() ->
                sales.row(data.getValue()).values().stream().reduce(0, Integer::sum)
            ).asObject());


        for (int i = 2010; i <= LocalDate.now().getYear(); i++) {
            yearCombo.getItems().add(i);
        }
        yearCombo.setValue(LocalDate.now().getYear());
        periodCombo.getItems().addAll(MONTHLY, WEEKLY);
        periodCombo.setValue(MONTHLY);

        loadSalesReport();
    }

    public void loadSalesReport() {
        Period period;
        if (periodCombo.getValue().equals(MONTHLY)) {
            period = Period.MONTHLY;
        } else {
            period = Period.WEEKLY;
        }
        resultsLabel.setText(String.format("%s report for %d", period, yearCombo.getValue()));

        sales = persistence.findSalesByPeriod(yearCombo.getValue(), period);
        salesTable.setItems(FXCollections.observableArrayList(sales.rowKeySet()));

        salesTable.getColumns().clear();
        salesTable.getColumns().add(nameColumn);
        salesTable.getColumns().add(totalQtyColumn);

        for (int index: sales.columnKeySet()) {
            String columnTitle;
            if (Period.MONTHLY.equals(period)) {
                columnTitle = Month.of(index).toString();
            } else {
                columnTitle = String.format("Week %d", index);
            }
            TableColumn<String, Integer> column = new TableColumn<>(columnTitle);
            column.setMinWidth(120);
            column.setPrefWidth(120);
            column.setCellValueFactory((product) -> Bindings.createIntegerBinding(() -> {
                Map<Integer, Integer> row = sales.row(product.getValue());
                return row.getOrDefault(index, 0);
            }).asObject());
            salesTable.getColumns().add(column);
        }
    }
}

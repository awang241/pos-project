package controller;

import controller.dialog.CreateOrderDialogController;
import data.Persistence;
import enums.Period;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.BorderPane;
import model.Order;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class MainWindowController implements Initializable {

    @FXML
    private BorderPane mainBorderPane;

    private Node posScreen;
    private Node createOrderScreen;
    private Node generalReportScreen;
    private Node periodicReportScreen;

    private Persistence persistence;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            persistence = new Persistence();
            persistence.findSalesByPeriod(2018, Period.MONTHLY);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/POS.fxml"));
            loader.setControllerFactory(param -> new POSController(persistence));
            posScreen = loader.load();

            loader = new FXMLLoader(getClass().getResource("/fxml/SalesReport.fxml"));
            loader.setControllerFactory(param -> new SalesReportController(persistence));
            generalReportScreen = loader.load();

            loader = new FXMLLoader(getClass().getResource("/fxml/PeriodicSalesReport.fxml"));
            loader.setControllerFactory(param -> new PeriodicSalesReportController(persistence));
            periodicReportScreen = loader.load();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void loadPOS() {
        mainBorderPane.setCenter(posScreen);
    }

    public void loadGeneralReport() {
        mainBorderPane.setCenter(generalReportScreen);
    }

    public void loadPeriodicReport() {mainBorderPane.setCenter(periodicReportScreen);}

    public void loadCreateOrder() {
        if (createOrderScreen == null) {
            try {
                Optional<Order> dialogOrder = showCreateOrderDialog();
                if (dialogOrder.isPresent()) {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CreateOrder.fxml"));
                    loader.setControllerFactory(param -> new CreateOrderController(persistence, dialogOrder.get()));
                    createOrderScreen = loader.load();
                    CreateOrderController controller = loader.getController();
                    controller.getDeleteFlag().addListener(((observable, oldValue, newValue) -> {
                        if (newValue.equals(true)) {
                            createOrderScreen = null;
                            mainBorderPane.setCenter(null);
                        }
                    }));
                }

            } catch (IOException e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR, "Error loading create order form.");
                alert.showAndWait();
            }
        }
        mainBorderPane.setCenter(createOrderScreen);
    }

    public void showConfigDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dialog/ConfigDialog.fxml"));
            Dialog<ButtonType> configDialog = new Dialog<>();
            DialogPane configPane = loader.load();

            configDialog.setDialogPane(configPane);
            configDialog.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Optional<Order> showCreateOrderDialog() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dialog/CreateOrderDialog.fxml"));
        loader.setControllerFactory((data) -> new CreateOrderDialogController(persistence.findAllSupplierCodes()));
        DialogPane orderPane = loader.load();

        Dialog<Order> orderDialog = new Dialog<>();
        CreateOrderDialogController controller = loader.getController();
        orderDialog.setResultConverter(controller::createOrderTemplate);
        orderDialog.setDialogPane(orderPane);
        return orderDialog.showAndWait();
    }


}

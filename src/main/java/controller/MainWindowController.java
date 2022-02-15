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
import javafx.util.Callback;
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

            posScreen = loadNode(param -> new POSController(persistence), "/fxml/POS.fxml");
            generalReportScreen = loadNode(param -> new GeneralSalesReportController(persistence), "/fxml/SalesReport.fxml");
            periodicReportScreen = loadNode(param -> new PeriodicSalesReportController(persistence), "/fxml/PeriodicSalesReport.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public Node loadNode(Callback<Class<?>, Object> controllerFactory, String URL) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(URL));
        loader.setControllerFactory(controllerFactory);
        return loader.load();
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
                    mainBorderPane.setCenter(createOrderScreen);
                }

            } catch (IOException e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR, "Error loading create order form.");
                alert.showAndWait();
            }
        }
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

    public void loadViewOrders() {
        try {
            mainBorderPane.setCenter(loadNode((param -> new ViewOrdersController(persistence)), "/fxml/ViewOrders.fxml"));
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, e.getLocalizedMessage()).showAndWait();
            e.printStackTrace();
        }
    }

}

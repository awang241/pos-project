package controller;

import data.GlobalData;
import data.Persistence;
import enums.Period;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class MainWindowController implements Initializable {

    @FXML
    private BorderPane mainBorderPane;

    private Node posScreen;
    private Node ordersScreen;
    private Node generalReportScreen;
    private Node periodicReportScreen;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            Persistence persistence = new Persistence();
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


}

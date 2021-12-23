package controller;

import data.GlobalData;
import data.Persistence;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.net.URL;
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
            Persistence persistence = new Persistence("jdbc:ucanaccess://" + GlobalData.getDbUrl());
            persistence.findLastInsertedTransaction();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/scene.fxml"));
            loader.setControllerFactory(param -> new FXMLController(persistence));
            posScreen = loader.load();

            loader = new FXMLLoader(getClass().getResource("/fxml/salesReport.fxml"));
            loader.setControllerFactory(param -> new SalesReportController(persistence));
            generalReportScreen = loader.load();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void loadPOS() {
        mainBorderPane.setCenter(posScreen);
    }

    public void loadOrders() {

    }

    public void loadGeneralReport() {
        mainBorderPane.setCenter(generalReportScreen);
    }

    public void loadPeriodicReport() {

    }
}

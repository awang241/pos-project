import data.GlobalData;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;

import java.awt.event.ActionEvent;
import java.util.Optional;


public class PosApp extends javafx.application.Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader;
        Scene scene;
        stage.setTitle("POS");

        GlobalData.loadProperties();

        if (GlobalData.shopDataDoesNotExist()) {
            loader = new FXMLLoader(getClass().getResource("fxml/dialog/ConfigDialog.fxml"));
            Dialog<ButtonType> configDialog = new Dialog<>();
            DialogPane configPane = loader.load();

            configDialog.setDialogPane(configPane);
            Optional<ButtonType> buttonType = configDialog.showAndWait();
            if (buttonType.isEmpty() || buttonType.get().getButtonData().equals(ButtonBar.ButtonData.CANCEL_CLOSE)) {
                System.exit(0);
            }
        }

        loader = new FXMLLoader(getClass().getResource("fxml/MainWindow.fxml"));

        scene = new Scene(loader.load());
        //scene.getStylesheets().add(getClass().getResource("css/styles.css").toExternalForm());

        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
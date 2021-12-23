import data.GlobalData;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class PosApp extends javafx.application.Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader;
        Scene scene;
        stage.setTitle("POS");

        if (!GlobalData.shopDataFileExists()) {
            loader = new FXMLLoader(getClass().getResource("fxml/ConfigDialog.fxml"));
            Scene configInput = new Scene(loader.load());
            Stage configStage = new Stage();
            configStage.setScene(configInput);
            configStage.setOnCloseRequest((event) -> System.exit(0));
            configStage.showAndWait();
        }
        GlobalData.loadShopDetails();

        loader = new FXMLLoader(getClass().getResource("fxml/mainWindow.fxml"));

        scene = new Scene(loader.load());
        scene.getStylesheets().add(getClass().getResource("css/styles.css").toExternalForm());

        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
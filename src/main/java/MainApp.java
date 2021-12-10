import data.GlobalData;
import controller.FXMLController;
import data.Persistence;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        GlobalData.getShopDetails();

        Persistence persistence = new Persistence();
        persistence.findLastInsertedTransaction();
        FXMLController controller = new FXMLController(persistence);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/scene.fxml"));
        loader.setController(controller);

        Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(getClass().getResource("css/styles.css").toExternalForm());

        stage.setTitle("POS");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
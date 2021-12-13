package controller.dialog;

import data.GlobalData;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ConfigDialogController implements Initializable {
    @FXML
    private TextField nameField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField gstField;
    @FXML
    private TextField dbField;
    @FXML
    private TextArea addressArea;
    @FXML
    private Button pickDBButton;
    @FXML
    private Button okButton;
    @FXML
    private Label warningLabel;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        nameField.setText(GlobalData.getShopName());
        phoneField.setText(GlobalData.getShopTelNo());
        gstField.setText(GlobalData.getGSTNo());
        addressArea.setText(GlobalData.getShopAddress().replace(", ", "\n"));
    }

    public void selectDBFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Database File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Access Database", "*.mdb"));
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        File file = fileChooser.showOpenDialog(pickDBButton.getScene().getWindow());
        if (file != null) {
            dbField.setText(file.toString());
        }
    }

    public void submit() {
        if (nameField.getText().isBlank()
                || phoneField.getText().isBlank()
                || gstField.getText().isBlank()
                || dbField.getText().isBlank()
                || addressArea.getText().isBlank()) {
            warningLabel.setVisible(true);
        } else {
            if (!GlobalData.shopDataFileExists()) {
                try {
                    String address = addressArea.getText().replace("\n", ", ");
                    GlobalData.createShopDetails(address, phoneField.getText(), gstField.getText(), nameField.getText(), dbField.getText());
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(-1);
                }
            }
            Stage stage = (Stage) okButton.getScene().getWindow();
            stage.close();
        }
    }

}

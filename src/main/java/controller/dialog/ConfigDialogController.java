package controller.dialog;

import data.GlobalData;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class ConfigDialogController implements Initializable {
    @FXML
    private DialogPane dialogPane;
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
    private Label warningLabel;
    @FXML
    private CheckBox showCredentialsBox;
    @FXML
    private GridPane credentialsPane;
    @FXML
    private TextField userField;
    @FXML
    private PasswordField passwordField;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        nameField.setText(GlobalData.getProperty(GlobalData.Key.NAME));
        phoneField.setText(GlobalData.getProperty(GlobalData.Key.TEL_NO));
        gstField.setText(GlobalData.getProperty(GlobalData.Key.GST_NO));
        addressArea.setText(GlobalData.getProperty(GlobalData.Key.ADDRESS));
        dbField.setText(GlobalData.getProperty(GlobalData.Key.DB_FILEPATH));

        String password = GlobalData.getProperty(GlobalData.Key.DB_PASSWORD);
        if (password == null) {
            showCredentialsBox.setSelected(false);
            credentialsPane.setDisable(true);
        } else {
            passwordField.setText(password);
            userField.setText(GlobalData.getProperty(GlobalData.Key.DB_USERNAME));
        }

        dialogPane.lookupButton(ButtonType.OK).addEventFilter(ActionEvent.ACTION, event -> {
            if (!validate()){
                warningLabel.setVisible(true);
                event.consume();
            } else {
                submit();
            }
        });
    }

    public void showCredentialsHandler() {
        credentialsPane.setDisable(!showCredentialsBox.isSelected());
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

    public boolean validate() {
        boolean blankCredentials = (isNotBlankOrNull(userField.getText()) && isNotBlankOrNull(passwordField.getText()));
        boolean validCredentials = !showCredentialsBox.isSelected() || blankCredentials ;
        return validCredentials
            && isNotBlankOrNull(nameField.getText())
            && isNotBlankOrNull(phoneField.getText())
            && isNotBlankOrNull(gstField.getText())
            && isNotBlankOrNull(dbField.getText())
            && isNotBlankOrNull(addressArea.getText());
    }

    public void submit() {
        Map<GlobalData.Key, String> newProperties = new HashMap<>();
        newProperties.put(GlobalData.Key.ADDRESS, addressArea.getText());
        newProperties.put(GlobalData.Key.TEL_NO, phoneField.getText());
        newProperties.put(GlobalData.Key.GST_NO, gstField.getText());
        newProperties.put(GlobalData.Key.NAME, nameField.getText());
        newProperties.put(GlobalData.Key.DB_FILEPATH, dbField.getText());
        if (showCredentialsBox.isSelected()) {
            newProperties.put(GlobalData.Key.DB_USERNAME, userField.getText());
            newProperties.put(GlobalData.Key.DB_PASSWORD, passwordField.getText());
        } else {
            newProperties.put(GlobalData.Key.DB_USERNAME, null);
            newProperties.put(GlobalData.Key.DB_PASSWORD, null);
        }
        try {
            GlobalData.setProperties(newProperties);
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error saving application properties.", ButtonType.OK);
            alert.showAndWait();
        }
    }

    private boolean isNotBlankOrNull(String string) {
        return string != null && !string.isBlank();
    }

}

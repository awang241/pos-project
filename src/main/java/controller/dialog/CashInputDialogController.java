package controller.dialog;

import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.ResourceBundle;

public class CashInputDialogController implements Initializable {

    private static final int MAX_CASH_IN_CENTS = 10000000;
    private static final BigDecimal CENTS_TO_DOLLARS_FACTOR = new BigDecimal(100);
    private int minCashInCents = 0;

    @FXML
    private Dialog<BigDecimal> dialog;

    @FXML
    private TextInputControl cashInput;

    private final IntegerProperty cashAmount = new SimpleIntegerProperty(0);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle){
        cashInput.textProperty().bind(Bindings.createStringBinding(() ->
                String.format("$%d.%02d", cashAmount.get() / 100, cashAmount.get() % 100), cashAmount));

        cashInput.addEventFilter(KeyEvent.ANY, keyEvent -> {
            if (keyEvent.getCode().equals(KeyCode.LEFT) || keyEvent.getCode().equals(KeyCode.RIGHT)) {
                keyEvent.consume();
            }
        });

        dialog.setResultConverter(this::returnCashAmount);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Node acceptButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
        acceptButton.addEventFilter(ActionEvent.ANY, event -> {
            if (cashAmount.get() < minCashInCents || cashAmount.get() > MAX_CASH_IN_CENTS) {
                new Alert(Alert.AlertType.ERROR, "Not enough cash").showAndWait();
                event.consume();
            }
        });
    }

    public void cashInputKeyPressHandler(KeyEvent event) {
        if (event.getCode().isDigitKey() && cashAmount.get() < MAX_CASH_IN_CENTS) {
            int keyVal = Integer.parseInt(event.getText());
            cashAmount.setValue(cashAmount.get() * 10 + keyVal);
        } else if (event.getCode().equals(KeyCode.BACK_SPACE)) {
            cashAmount.setValue(cashAmount.get() / 10);
        }
    }

    public BigDecimal returnCashAmount(ButtonType type) {
        BigDecimal amount = new BigDecimal(cashAmount.get());
        return type.equals(ButtonType.OK) ? amount.divide(CENTS_TO_DOLLARS_FACTOR, 2, RoundingMode.DOWN): null;
    }

    public void setMinCash(BigDecimal min) {
        minCashInCents = min.multiply(CENTS_TO_DOLLARS_FACTOR).intValueExact();
    }
}

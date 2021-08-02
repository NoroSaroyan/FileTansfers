package noro.geekbrains.client;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class RegController {
    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextArea textArea;

    private Controller controller;

    public void setController(Controller controller) {
        this.controller = controller;
    }

    public void setResultTryToReg(String command) {
        if (command.equals(Command.REG_OK)) {
            textArea.appendText("Registered Successfully\n");
        }
        if (command.equals(Command.REG_NO)) {
            textArea.appendText("Username Taken\n");
        }
    }

    public void tryToReg(ActionEvent actionEvent) {
        String login = loginField.getText().trim();
        String password = passwordField.getText().trim();

        if (login.length() * password.length() == 0) {
            return;
        }

        controller.registration(login, password);
    }
}


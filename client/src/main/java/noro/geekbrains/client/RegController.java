package noro.geekbrains.client;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import noro.geekbrains.Command;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class RegController {
    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextArea textArea;

    private String path = "D:\\FileTransfers\\Users\\";

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

        if (createDirectory(login) == true) {
            controller.registration(login, password);
        }
    }

    public boolean createDirectory(String username) {
        try {
            String dirPath = this.path + "\\" + username;
            Files.createDirectories(Paths.get(dirPath));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}


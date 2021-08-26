package noro.geekbrains.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import main.Command;
import main.DbFiles;
import main.Mapper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.URL;
import java.util.*;

public class Controller implements Initializable {

    @FXML
    public TextField loginField;
    @FXML
    public PasswordField passwordField;
    @FXML
    public HBox authPanel;
    @FXML
    ListView<String> files;

    public List<Client> clients = new ArrayList<>();
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private final int PORT = 8189;
    private final String IP_ADDRESS = "localhost";

    private boolean authenticated;
    private String username;
    private Stage stage;
    private Stage regStage;
    private String login;
    private RegController regController;

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
        authPanel.setVisible(!authenticated);
        authPanel.setManaged(!authenticated);
        files.setVisible(authenticated);
        files.setManaged(authenticated);

        if (!authenticated) {
            username = "";
        }
        setTitle(username);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            //stage
            stage = (Stage) loginField.getScene().getWindow();
            stage.setOnCloseRequest(event -> {
                System.out.println("bye");
                if (socket != null && !socket.isClosed()) {
                    try {
                        out.writeUTF(Command.END);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        });
        setAuthenticated(false);
    }

    private void connect() {
        try {
            socket = new Socket(IP_ADDRESS, PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            new Thread(() -> {
                try {
                    // цикл аутентификации
                    while (true) {
                        String str = in.readUTF();

                        if (str.startsWith("/")) {
                            if (str.startsWith(Command.AUTH_OK)) {
                                String[] token = str.split("\\s");
                                username = token[1];
                                setAuthenticated(true);
                                break;
                            }

                            if (str.equals(Command.REG_OK)) {
                                regController.setResultTryToReg(Command.REG_OK);
                            }

                            if (str.equals(Command.REG_NO)) {
                                regController.setResultTryToReg(Command.REG_NO);
                            }
                        }
                    }
                    //цикл работы
                    while (true) {
                        String str = in.readUTF();
                        System.out.println("<<-" + str);
                        if (str.startsWith(Command.INSERT_OK)) {

                        }
                        if (str.startsWith(Command.DBFILES_OK)) {
                            // /dbfilesok
                            // DbFile[{id} = * , name = "dd"]
                            String[] data = str.split(Command.DBFILES_OK, 2);
                            List<DbFiles> dbFiles = Mapper.stringToList(data[1]);
                            Platform.runLater(() -> {
                                files.getItems().clear();
                                System.out.println("files cleared");
                                for (DbFiles dbFile : dbFiles) {
                                    files.getItems().add(dbFile.Name.toString());
                                    System.out.println("added file " + dbFile.toString());
                                }
                            });
                        }
                    }

                } catch (RuntimeException e) {
                    System.out.println(e.getMessage());
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    setAuthenticated(false);
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tryToAuth(ActionEvent actionEvent) {
        try {
            if (socket == null || socket.isClosed()) {
                connect();
            }
            login = loginField.getText().trim();

            out.writeUTF(String.format("%s %s %s", Command.AUTH, loginField.getText().trim(), passwordField.getText().trim()));

        } catch (NullPointerException | ConnectException e) {
            System.out.println("error");
        } catch (IOException e) {
            //e.printStackTrace();
        } finally {
            passwordField.clear();
        }
    }

    private void setTitle(String name) {
        Platform.runLater(() -> {
            if (name.equals("")) {
                stage.setTitle("File Transfer");
            } else {
                stage.setTitle(String.format("File Transfer - [ %s ]", name));
            }
        });
    }

    public void showRegWindow(ActionEvent actionEvent) {
        if (regStage == null) {
            initRegWindow();
        }
        regStage.show();
    }

    private void initRegWindow() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/reg.fxml"));
            Parent root = fxmlLoader.load();

            regController = fxmlLoader.getController();
            regController.setController(this);

            regStage = new Stage();
            regStage.setTitle("Sign Up ");
            regStage.setScene(new Scene(root, 450, 350));
            regStage.initStyle(StageStyle.UTILITY);
            regStage.initModality(Modality.APPLICATION_MODAL);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void registration(String login, String password) {
        if (socket == null || socket.isClosed()) {
            connect();
        }
        try {
            out.writeUTF(String.format("%s %s %s", Command.REG, login, password));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void selectFile(ActionEvent actionEvent) {
        System.out.println("/selectFile button");
        try {
            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(this.stage);
            DbFiles dbFiles = new DbFiles(12342, file.getName(), this.username, file.getAbsolutePath());
            //insertfile command + file , in server split
            if (file != null) {
                String str = Mapper.objectToString(dbFiles);
                out.writeUTF(Command.INSERT_FILE + str);//TODO /insertfile
                System.out.printf("User selected file: %s \n", file.getAbsolutePath());
                byte[] fileContent = new ObjectMapper().writeValueAsBytes(file);// file to byte array , was just file before this
                out.write(fileContent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clientListMouseReleased(MouseEvent mouseEvent) throws IOException {
        System.out.println(files.getSelectionModel().getSelectedItem());
        String msg = String.format("%s", files.getSelectionModel().getSelectedItem());

    }

    public void saveFileToComputer(ActionEvent actionEvent) {
    }
}

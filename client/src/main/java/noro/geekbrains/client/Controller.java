package noro.geekbrains.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.*;
import noro.geekbrains.*;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;

import static noro.geekbrains.FileHandler.saveContentToFile;

public class Controller implements Initializable {

    @FXML
    public TextField loginField;
    @FXML
    public PasswordField passwordField;
    @FXML
    public HBox authPanel;
    @FXML
    ListView<DbFiles> files;
    @FXML
    public Button download;
    @FXML
    public Button selectFileButton;
    @FXML
    public Button delete;
    @FXML
    public Text text;

    private Socket socket;
    private Socket onlyDataSocket;
    private DataInputStream in;
    private DataInputStream onlyDataIn;
    private DataOutputStream out;
    private DataOutputStream onlyDataOut;
    private final int PORT = 8189;
    private final int onlyDataPort = 8188;
    private final String IP_ADDRESS = "localhost";

    private boolean authenticated;
    private String username;
    private Stage stage;
    private Stage regStage;
    private RegController regController;

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
        authPanel.setVisible(!this.authenticated);
        authPanel.setManaged(!this.authenticated);

        text.setVisible(this.authenticated);
        text.setText("text is showing, commands set with setText() less than 1 sec ");

        files.setVisible(this.authenticated);
        files.setManaged(this.authenticated);

        download.setVisible(this.authenticated);

        delete.setVisible(this.authenticated);
        selectFileButton.setVisible(this.authenticated);

        if (!this.authenticated) {
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
            onlyDataSocket = new Socket(IP_ADDRESS, onlyDataPort);

            socket.setSoTimeout(60_000);

            in = new DataInputStream(socket.getInputStream());
            onlyDataIn = new DataInputStream(onlyDataSocket.getInputStream());

            out = new DataOutputStream(socket.getOutputStream());
            onlyDataOut = new DataOutputStream(onlyDataSocket.getOutputStream());

            new Thread(() -> {
                try {
                    // цикл аутентификации
                    while (true) {
                        String str = in.readUTF();
                        if (str.startsWith("/")) {
                            if (str.startsWith(Command.AUTH_OK)) {
                                handleAuthOk(str);
                                this.text.setText(Command.AUTH_OK);
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
                        try {
                            String str = in.readUTF();
                            System.out.println("<<-" + str);
                            if (str.startsWith(Command.INSERT_OK)) {
                                System.out.println(Command.INSERT_OK);
                                this.text.setText(Command.INSERT_OK);
                            }
                            if (str.startsWith(Command.DBFILES_OK)) {
                                handleDbFilesOk(str);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (RuntimeException | IOException e) {
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
            out.writeUTF(String.format("%s %s %s", Command.AUTH, loginField.getText().trim(),
                    passwordField.getText().trim()));

        } catch (NullPointerException | ConnectException e) {
            System.out.println("error");
        } catch (IOException e) {
            e.printStackTrace();
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
        try {
            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(this.stage);
            if (file != null) {
                DbFiles dbFiles = new DbFiles(12342, file.getName(), this.username, file.getAbsolutePath());
                String str = Mapper.objectToString(dbFiles);
                out.writeUTF(Command.INSERT_FILE + str);
                byte[] fileContent = Files.readAllBytes(file.toPath());
                System.out.println("file content size = " + fileContent.length);
                onlyDataOut.writeLong(fileContent.length);
                onlyDataOut.write(fileContent);
                askNewFiles();
                this.text.setText(Command.INSERT_OK);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void askNewFiles() throws IOException {
        this.out.writeUTF(Command.ASK_ALL_FILES);
    }

    public void handleAuthOk(String str) {
        String[] token = str.split("\\s");
        username = token[1];
        setAuthenticated(true);
    }

    public void handleDbFilesOk(String str) {
        String[] data = str.split(Command.DBFILES_OK, 2);
        List<DbFiles> dbFiles = Mapper.stringToList(data[1]);
        Platform.runLater(() -> {
            files.getItems().clear();
            System.out.println("files cleared");
            for (DbFiles dbFile : dbFiles) {
                files.getItems().add(dbFile);
            }
        });
        this.text.setText(Command.DBFILES_OK);
    }


    public void deleteFile(ActionEvent actionEvent) {
        DbFiles file = files.getSelectionModel().getSelectedItem();
        try {
            out.writeUTF(Command.DELETE_FILE + file.Id);
            askNewFiles();
            this.text.setText(Command.DELETE_FILE_OK);
        } catch (Exception e) {
            this.text.setText(Command.DELETE_FILE_NOT_OK);
            e.printStackTrace();
        }

    }

    public void clientListMouseReleased(MouseEvent mouseEvent) throws NullPointerException {
        System.out.println(files.getSelectionModel().getSelectedItem());
    }

    public void saveFileToComputer(ActionEvent actionEvent) {
        DbFiles selectedItem = files.getSelectionModel().getSelectedItem();
        try {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            File file = directoryChooser.showDialog(this.stage);
            String path = file.getAbsolutePath();
            if (selectedItem != null) {
                out.writeUTF(Command.DOWNLOAD_FILE + selectedItem.Id.toString());
                Long size = onlyDataIn.readLong();
                byte[] content = new byte[size.intValue()];
                onlyDataIn.readFully(content);

                String absolutePath = Paths.get(path, selectedItem.Name).toFile().getAbsolutePath();
                if (saveContentToFile(absolutePath, content)) {
                    System.out.println(Command.DOWNLOAD_FILE_OK);
                    this.text.setText(Command.DOWNLOAD_FILE_OK);
                } else {
                    this.text.setText(Command.DOWNLOAD_FILE_DENIED);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

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
import javafx.stage.*;
import main.Command;
import main.DbFiles;
import main.Mapper;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

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
    public TextArea textArea;

    public List<Client> clients = new ArrayList<>();
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
    private String login;
    private RegController regController;

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
        authPanel.setVisible(!authenticated);
        authPanel.setManaged(!authenticated);

        files.setVisible(authenticated);
        files.setManaged(authenticated);
        download.autosize();
        download.setVisible(authenticated);
        delete.setVisible(authenticated);
        selectFileButton.setVisible(authenticated);
        //textArea.setVisible(authenticated);
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
                        //TODO lock and unlock readUTF
                        try {
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
                                        files.getItems().add(dbFile);
                                        System.out.println("added file " + dbFile.toString());
                                    }
                                });
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
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

    //checked
    public void selectFile(ActionEvent actionEvent) {
        System.out.println("/selectFile button");
        try {
            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(this.stage);
            DbFiles dbFiles = new DbFiles(12342, file.getName(), this.username, file.getAbsolutePath());
            //insertfile command + file , in server split
            if (file != null) {
                String str = Mapper.objectToString(dbFiles);
                System.out.println("send insert command ");
                out.writeUTF(Command.INSERT_FILE + str);
                System.out.println("");
                System.out.printf("User selected file: %s \n", file.getAbsolutePath());

                byte[] fileContent = Files.readAllBytes(file.toPath());

                System.out.println("before write");
                System.out.println("file content size = " + fileContent.length);
                onlyDataOut.writeLong(fileContent.length);
                onlyDataOut.write(fileContent);
                System.out.println("after write");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //checked
    public void deleteFile(ActionEvent actionEvent) {
        DbFiles file = files.getSelectionModel().getSelectedItem();
        final int index = files.getSelectionModel().getSelectedIndex();
        try {
            out.writeUTF(Command.DELETE_FILE + file.Id);

//            String response = in.readUTF();
//            if (response.startsWith(Command.DELETE_FILE_OK)) {
//                files.getItems().remove(index);
//            }
//            System.out.println(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clientListMouseReleased(MouseEvent mouseEvent) throws IOException {
        System.out.println(files.getSelectionModel().getSelectedItem());
        String msg = String.format("%s", files.getSelectionModel().getSelectedItem());

    }

    //checked
    public void saveFileToComputer(ActionEvent actionEvent) {
        System.out.println("/downloadfile button");
        DbFiles selectedItem = files.getSelectionModel().getSelectedItem();
        System.out.printf("    selected file: id = %d name = %s \n", selectedItem.Id, selectedItem.Name);
        try {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            File file = directoryChooser.showDialog(this.stage);
            String path = file.getAbsolutePath();
            if (selectedItem != null) {
                out.writeUTF(Command.DOWNLOAD_FILE + selectedItem.Id.toString());

                System.out.println("    user selected: " + selectedItem + "  command -" + Command.DOWNLOAD_FILE);

                Long size = onlyDataIn.readLong();
                System.out.println("    read size " + size);

                byte[] content = new byte[size.intValue()];

                System.out.println("    read content ");
                onlyDataIn.readFully(content);

                System.out.println("    trying to save file.");
                String absolutePath = Paths.get(path, selectedItem.Name).toFile().getAbsolutePath();
                if (saveToFile(absolutePath , content)) {
                    System.out.println(Command.DOWNLOAD_FILE_OK);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean saveToFile(String pathToSave, byte[] content) {
        if (!exist(pathToSave)) {
            if (!createDirectory(pathToSave)) {
                return false;
            }
        }

        try {
            OutputStream out = new FileOutputStream(new File(pathToSave));
            out.write(content);
            out.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean exist(String path) {
        return new File(path).exists();
        //return (path).toFile().exists();
    }

    public static boolean createDirectory(String p) {
        if (!exist(p)) {
            try {
                Path path = Paths.get(p).getParent();
                Files.createDirectories(path);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            return false;
        }
        return false;
    }
}

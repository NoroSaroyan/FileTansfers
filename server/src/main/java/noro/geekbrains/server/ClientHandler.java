package noro.geekbrains.server;

import main.Command;
import main.DbFiles;
import main.Mapper;
import sun.misc.IOUtils;

import java.io.*;
import java.net.Socket;
import java.nio.file.*;
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private Socket onlyDataSocket;

    private DataInputStream in;
    private DataInputStream onlyDataIn;

    private DataOutputStream out;
    private DataOutputStream onlyDataOut;

    private String login;
    private String password;


    public ClientHandler(Server server, Socket socket, Socket onlyDataSocket) {
        try {
            this.server = server;
            this.socket = socket;
            this.onlyDataSocket = onlyDataSocket;

            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            onlyDataIn = new DataInputStream(onlyDataSocket.getInputStream());
            onlyDataOut = new DataOutputStream(onlyDataSocket.getOutputStream());

            server.getExecutorService().execute(() -> {
                try {

                    // цикл аутентификации
                    while (true) {
                        String str = in.readUTF();

                        if (str.equals(Command.END)) {
                            out.writeUTF(Command.END);
                            System.out.println("");
                            throw new RuntimeException("Клиент захотел отключиться");
                        }

                        //если команда аутентификация
                        if (str.startsWith(Command.AUTH)) {
                            String[] token = str.split("\\s", 3);
                            if (token.length < 3) {
                                System.out.println("Error " + Arrays.toString(token));
                                continue;
                            }
                            login = token[1];
                            password = token[2];
                            if (login != null && password != null) {
                                if (server.getAuthService().login(login, password))
                                    if (!server.isLoginAuthenticated(login)) {
                                        sendMsg(Command.AUTH_OK + " " + login);
                                        server.subscribe(this);
                                        List<DbFiles> userFiles = SQLHandler.getUserFiles(login);
                                        sendMsg(Command.DBFILES_OK + Mapper.objectToString(userFiles));
                                        System.out.println("client: " + socket.getRemoteSocketAddress() +
                                                " connected with login: " + login);
                                        break;
                                    } else {
                                        System.out.println("already using");
                                        sendMsg("Already in use");
                                    }
                                else {
                                    sendMsg("Incorrect login/password");
                                }
                            } else {
                                sendMsg("null values");
                                throw new RuntimeException("Null values");
                            }
                        }

                        //если команда регистрация
                        if (str.startsWith(Command.REG)) {
                            String[] token = str.split("\\s", 3);
                            if (token.length < 3) {
                                System.out.println("error " + Arrays.toString(token));
                                continue;
                            }
                            boolean regSuccess = server.getAuthService()
                                    .registration(token[1], token[2]);
                            if (regSuccess) {
                                sendMsg(Command.REG_OK);
                                System.out.println("Reg ok");
                                System.out.println(Arrays.toString(token));
                            } else {
                                sendMsg(Command.REG_NO);
                            }
                        }

                    }
                    //цикл работы
                    while (true) {
                        try {
                            String str = in.readUTF();
                            if (str.startsWith("/")) {
                                if (str.equals(Command.END)) {
                                    out.writeUTF(Command.END);
                                    break;
                                }
                                if (str.startsWith(Command.ASK_ALL_FILES)) {
                                    List<DbFiles> userFiles = SQLHandler.getUserFiles(login);
                                    sendMsg(Command.DBFILES_OK + Mapper.objectToString(userFiles));
                                }
                                if (str.startsWith(Command.DOWNLOAD_FILE)) {
                                    String[] data = str.split(Command.DOWNLOAD_FILE, 2);
                                    System.out.println("download file " + data[1]);
                                    int i = Integer.parseInt(data[1]);
                                    DbFiles dbFile = SQLHandler.getFilesById(i);
                                    System.out.println("    got file from db " + dbFile.toString());
                                    byte[] fileContent = Files.readAllBytes(Paths.get(dbFile.Path));
                                    System.out.println("    write file content length" + fileContent.length);
                                    onlyDataOut.writeLong(fileContent.length);
                                    System.out.println("    write file content");
                                    onlyDataOut.write(fileContent);
                                } else {
                                    System.out.println("download file else broadcast msg");
                                    server.broadcastMsg(this, str);
                                }
                                if (str.startsWith(Command.DELETE_FILE)) {
                                    String[] data = str.split(Command.DELETE_FILE, 2);
                                    String fileId = data[1];
                                    System.out.println("deleted file id =" + fileId);
                                    DbFiles dbFile = SQLHandler.getFilesById(Integer.parseInt(fileId));
                                    System.out.println("got file from db " + dbFile.toString());
                                    //TODO check dbFile null
                                    if (dbFile != null) {
                                        File file = new File(dbFile.Path);
                                        boolean isDeleted = false;

                                        try {
                                            isDeleted = Files.deleteIfExists(file.toPath());
                                        } catch (IOException ioException) {
                                            ioException.printStackTrace();
                                        }

                                        if (isDeleted) {
                                            SQLHandler.deleteFile(Integer.parseInt(fileId));
                                            out.writeUTF(Command.DELETE_FILE_OK + fileId);
                                        } else {
                                            out.writeUTF(Command.DELETE_FILE_NOT_OK + "id:" + fileId);
                                        }
                                    }
                                }
                                if (str.startsWith(Command.INSERT_FILE)) {
                                    String[] data = str.split(Command.INSERT_FILE, 2);
                                    DbFiles file = Mapper.stringToObject(data[1]);
                                    if (file != null) {
                                        Long size = onlyDataIn.readLong();
                                        byte[] bytes = new byte[size.intValue()];
                                        onlyDataIn.readFully(bytes);
                                        String path = "D:\\FileTransfers\\Users\\" + this.login + "\\" + file.Name;
                                        if (saveContentToFile(bytes, path)) {
                                            SQLHandler.insertFile(file.Name, file.Username, path);
                                            sendMsg(Command.INSERT_OK);
                                        } else {
                                            out.writeUTF(Command.INSERT_FAILED);

                                        }
                                    } else {
                                        server.broadcastMsg(this, str);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (RuntimeException e) {
                    System.out.println(e.getMessage());
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    server.unsubscribe(this);
                    System.out.println("Client disconnected: " + login);
                    try {
                        socket.close();
                        onlyDataSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

        } catch (
                IOException e) {
            e.printStackTrace();
        }

    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getLogin() {
        return login;
    }


    //Saves users file to servers hard drive
    public static boolean saveContentToFile(byte[] arr, String path) throws Exception {
        if (!exist(path)) {
            if (!createDirectory(path)) {
                return false;
            }
        }
        try {
            OutputStream out = new FileOutputStream(new File(path));
            out.write(arr);
            out.close();
            return true;
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

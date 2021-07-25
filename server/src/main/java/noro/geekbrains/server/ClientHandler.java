package noro.geekbrains.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private String login;

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            server.getExecutorService().execute(() -> {
                try {
                    // установка сокет тайм аут
//                    socket.setSoTimeout(5000);

                    // цикл аутентификации
                    while (true) {
                        String str = in.readUTF();

                        //если команда отключиться
                        if (str.equals(Command.END)) {
                            out.writeUTF(Command.END);
                            throw new RuntimeException("Клиент захотел отключиться");
                        }

                        //если команда аутентификация
                        if (str.startsWith(Command.AUTH)) {
                            String[] token = str.split("\\s", 3);
                            if (token.length < 3) {
                                continue;
                            }
                            login = token[1];
                            if (login != null) {
                                if (!server.isLoginAuthenticated(login)) {
                                    sendMsg(Command.AUTH_OK + " " + login);
                                    server.subscribe(this);
                                    System.out.println("client: " + socket.getRemoteSocketAddress() +
                                            " connected with login: " + login);
                                    break;
                                } else {
                                    sendMsg("Данная учетная запись уже используется");
                                }
                            } else {
                                sendMsg("Неверный логин / пароль");
                            }
                        }

                        //если команда регистрация
                        if (str.startsWith(Command.REG)) {
                            String[] token = str.split("\\s", 4);
                            if (token.length < 4) {
                                continue;
                            }
                            boolean regSuccess = server.getAuthService()
                                    .registration(token[1], token[2]);
                            if (regSuccess) {
                                sendMsg(Command.REG_OK);
                            } else {
                                sendMsg(Command.REG_NO);
                            }
                        }

                    }
                    //цикл работы
                    while (true) {
                        String str = in.readUTF();

                        if (str.startsWith("/")) {
                            if (str.equals(Command.END)) {
                                out.writeUTF(Command.END);
                                break;
                            }

                            if (str.startsWith(Command.PRIVATE_MSG)) {
                                String[] token = str.split("\\s", 3);
                                if (token.length < 3) {
                                    continue;
                                }
                                server.privateMsg(this, token[1], token[2]);
                            }
                            if (str.startsWith("/chnick ")) {
                                String[] token = str.split("\\s+", 2);
                                if (token.length < 2) {
                                    continue;
                                }
                                if (token[1].contains(" ")) {
                                    sendMsg("Ник не может содержать пробелов!");
                                    continue;
                                }
                            }
                        } else {
                            server.broadcastMsg(this, str);
                        }
                    }
                } catch (RuntimeException e) {
                    System.out.println(e.getMessage());
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    server.unsubscribe(this);
                    System.out.println("Client disconnected: " + login);
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

        } catch (IOException e) {
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
}

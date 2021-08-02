package noro.geekbrains.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final int PORT = 8189;
    private ServerSocket server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private List<ClientHandler> clients;
    private AuthService authService;
    private ExecutorService executorService;

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public Server() {
        clients = new CopyOnWriteArrayList<>();
        executorService = Executors.newCachedThreadPool();
        authService = new SimpleAuthService();
        //==============//
//        if (!SQLHandler.connect()) {
//            throw new RuntimeException("Не удалось подключиться к БД");
//        }
//        authService = new DBAuthServise();
        //==============//


        try {
            server = new ServerSocket(PORT);
            System.out.println("Server started");

            while (true) {
                socket = server.accept();
                System.out.println("Client connected");
                System.out.println("client: " + socket.getRemoteSocketAddress());
                new ClientHandler(this, socket);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // SQLHandler.disconnect();
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void broadcastMsg(ClientHandler sender, String msg) {
        String message = String.format("[ %s ]: %s", sender.getLogin(), msg);
        for (ClientHandler c : clients) {
            c.sendMsg(message);
        }
    }

    public void privateMsg(ClientHandler sender, String receiver, String msg) {
        String message = String.format("[ %s ] to [ %s ]: %s", sender.getLogin(), receiver, msg);
        for (ClientHandler c : clients) {
            if (c.getLogin().equals(receiver)) {
                c.sendMsg(message);
                if (!c.equals(sender)) {
                    sender.sendMsg(message);
                }
                return;
            }
        }
        sender.sendMsg("not found user: " + receiver);
    }

    public void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
        broadcastClientlist();
    }

    public void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        broadcastClientlist();
    }

    public AuthService getAuthService() {
        return authService;
    }

    public boolean isLoginAuthenticated(String login) {
        for (ClientHandler c : clients) {
            if (c.getLogin().equals(login)) {
                return true;
            }
        }
        return false;
    }

    public void broadcastClientlist() {
        StringBuilder sb = new StringBuilder(Command.CLIENT_LIST);

        for (ClientHandler c : clients) {
            sb.append(" ").append(c.getLogin());
        }

        String msg = sb.toString();

        for (ClientHandler c : clients) {
            c.sendMsg(msg);
        }
    }
}

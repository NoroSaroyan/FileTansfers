package noro.geekbrains.server;

import main.Command;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final int PORT = 8189;
    private final int ONLYDATAPORT = 8188;

    private ServerSocket server;
    private ServerSocket onlyDataServer;
    private Socket onlyDataSocket;
    private Socket socket;

    private DataInputStream onlyDataIn;
    private DataInputStream in;

    private DataOutputStream onlyDataOut;
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
        if (!SQLHandler.connect()) {
            throw new RuntimeException("could not connect to database");
        }
        authService = new DatabaseAuthService();

        try {
            server = new ServerSocket(PORT);
            onlyDataServer = new ServerSocket(ONLYDATAPORT);
            System.out.println("Server started");
            System.out.println("Data Server Started");

            while (true) {
                socket = server.accept();
                onlyDataSocket = onlyDataServer.accept();
                socket.setSoTimeout(100_000);
                onlyDataSocket.setSoTimeout(100_000);
                System.out.println("Client connected");
                System.out.println("client: " + socket.getRemoteSocketAddress());
                new ClientHandler(this, socket, onlyDataSocket);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
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
    }

    public void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
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
}

package noro.geekbrains.client;

import noro.geekbrains.server.Server;

import java.io.*;
import java.nio.file.Files;
import java.util.List;


public class Client {
    public String Username;
    public String Password;

    public Client(String username, String password) {
        this.Username = username;
        this.Password = password;
    }

    public void add(Client client) throws Exception {
        if (client != null) {
            Server.users.add(client);
        } else {
            throw new IOException("client is not valid");
        }
    }

    public void Save() {
        try {
            FileWriter fw = new FileWriter("~/.FILE_TRANSFER");
            File homeFile = new File("~/.FILE_TRANSFER");
            if (homeFile.createNewFile()) {
                fw.write(this.Username);
                fw.write(":");
                fw.write(this.Password);
                fw.close();
            } else {
                System.out.println("File" + homeFile.getPath() + "already exists");
            }
        } catch (IOException e) {
            System.out.println("Error occurred");
            e.printStackTrace();
        }
    }

    public static Client login() throws Exception {
        File file = new File("~/.FILE_TRANSFER");
        List<String> lines = Files.readAllLines(file.toPath());
        if (lines.size() < 1) {
            throw new Exception();
        }

        String[] split = lines.get(0).split(":");
        if (split.length != 2) {
            throw new Exception();
        }
        Client cl = new Client(split[0], split[1]);
        int index = Server.users.indexOf(cl);
        return  Server.users.get(index);
    }

    public static Client login(String username, String password) {
        Client client = new Client(username, password);
        int index = Server.users.indexOf(client);
        return Server.users.get(index);
    }


}

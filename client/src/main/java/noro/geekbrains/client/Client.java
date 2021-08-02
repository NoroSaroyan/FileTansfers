package noro.geekbrains.client;

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
            //todo
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

    /*
    auto login for client.
     */
    public static Client autoLogin() throws Exception {

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
        // TODO: 02.08.2021
        return cl;
    }

    public static Client login(String username, String password) throws Exception {
        try {
            //client sends command /login
            //server answers (true or false) by completing query in sqlhandler.
            Client client = new Client(username, password);
            //todo

        } catch (RuntimeException e) {
            e.getMessage();
        }
        throw new IOException();
    }

}

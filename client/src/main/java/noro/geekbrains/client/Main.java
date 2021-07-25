package noro.geekbrains.client;

import java.util.Scanner;

public class Main {
    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        Client client = new Client("Norik", "11111N");

    }

    public static void commands(String arg) throws Exception {
        if (arg == null) {
            System.out.println("Provide a command");
            arg = sc.next();
            commands(arg);
        }
        if (arg.equals("--help")) {
            help();
        }
    }
    public static void help() {
        System.out.println("-u <username>");
        System.out.println("-p <password>");
    }
}

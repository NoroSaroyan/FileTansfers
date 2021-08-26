package noro.geekbrains.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import main.DbFiles;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Main {

    public static void main(String[] args) throws Exception {
        Server server = new Server();
        /*byte[] str = "test".getBytes();
        String path = "C:\\1\\34\\f.txt";
        String path1 = "D:\\FileTransfers\\Users\\kkkl\\dkg.txt";
        saveContentToFile(str, path1);
       */
    }

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

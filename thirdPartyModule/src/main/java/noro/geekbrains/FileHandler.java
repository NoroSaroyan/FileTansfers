package noro.geekbrains;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileHandler {
    public static boolean saveContentToFile(String pathToSave, byte[] content) {
        if (!exist(pathToSave)) {
            if (!createDirectory(pathToSave)) {
                return false;
            }
        }

        try {
            OutputStream out = new FileOutputStream(pathToSave);
            out.write(content);
            out.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean exist(String path) {
        return new File(path).exists();
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

package noro.geekbrains.server;

import noro.geekbrains.DbFiles;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SQLHandler {
    private static Connection connection;
    private static PreparedStatement psRegistration;
    private static PreparedStatement psLogin;
    private static PreparedStatement psInsertFile;
    private static PreparedStatement psGetUserFiles;
    private static PreparedStatement psGetFileById;
    private static PreparedStatement psDeleteFile;

    public static boolean connect() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://root:16012004@localhost:3306/FileTransfer");
            prepareAllStatements();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void prepareAllStatements() throws SQLException {
        psRegistration = connection.prepareStatement("INSERT INTO clients(name, password) VALUES (? ,? );");
        psLogin = connection.prepareStatement("Select name from clients where name = ? AND password = ?;");
        psInsertFile = connection.prepareStatement("Insert INTO files(name , username , path) VALUES (?,?,?);");
        psGetUserFiles = connection.prepareStatement("select * from files where username = ?;");
        psGetFileById = connection.prepareStatement("Select * from files where id = ?;");
        psDeleteFile = connection.prepareStatement("delete from files where id = ?;");
    }

    public static List<DbFiles> getUserFiles(String username) {
        List<DbFiles> files = new ArrayList<>();
        ResultSet rs = null;
        try {
            psGetUserFiles.setString(1, username);
            rs = psGetUserFiles.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String path = rs.getString("path");
                files.add(new DbFiles(id, name, username, path));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }
        return files;
    }

    public static DbFiles getFilesById(int id) {
        ResultSet rs;
        try {
            psGetFileById.setInt(1, id);
            rs = psGetFileById.executeQuery();
            if (rs.next()) {
                String username = rs.getString("username");
                String name = rs.getString("name");
                String path = rs.getString("path");
                rs.close();
                return new DbFiles(id, name, username, path);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean insertFile(String name, String username, String path) {
        try {
            psInsertFile.setString(1, name);
            psInsertFile.setString(2, username);
            psInsertFile.setString(3, path);
            psInsertFile.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public static boolean registration(String login, String password) {
        try {
            psRegistration.setString(1, login);
            psRegistration.setString(2, password);
            psRegistration.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteFile(int id) {
        try {
            psDeleteFile.setInt(1, id);
            psDeleteFile.execute();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean login(String username, String password) {
        try {
            psLogin.setString(1, username);
            psLogin.setString(2, password);
            return psLogin.execute();
        } catch (SQLException throwable) {
            return false;
        }
    }

    public static void disconnect() {
        try {
            psRegistration.close();
            psLogin.close();
            psDeleteFile.close();
            psLogin.close();
            psGetUserFiles.close();
            psGetFileById.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}

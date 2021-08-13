package noro.geekbrains.server;

import java.sql.*;

public class SQLHandler {
    private static Connection connection;
    private static PreparedStatement psGetUserName;
    private static PreparedStatement psRegistration;
    private static PreparedStatement psChangeUserName;
    private static PreparedStatement psChangePassword;
    private static PreparedStatement psLogin;
    private static PreparedStatement psInsertFile;
    private static PreparedStatement psGetFile;
    private static PreparedStatement psGetFileByPath;
    private static PreparedStatement psGetFiles;

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
        psGetUserName = connection.prepareStatement("SELECT name FROM clients WHERE name = ? AND password = ?;");
        psRegistration = connection.prepareStatement("INSERT INTO clients(name, password) VALUES (? ,? );");
        psChangeUserName = connection.prepareStatement("UPDATE clients SET name = ? WHERE name = ? AND password = ? ;");
        psChangePassword = connection.prepareStatement("Update clients SET password = ? WHERE name = ? AND Password = ?;");
        psLogin = connection.prepareStatement("Select name from clients where name = ? AND password = ?;");
        psInsertFile = connection.prepareStatement("Insert INTO files(id , name , username , path) VALUES (?,?,?,?);");
        psGetFile = connection.prepareStatement("Select name from files where name = ? AND username = ?;");
        psGetFileByPath = connection.prepareStatement("Select name from files where path = ?;");
        psGetFiles = connection.prepareStatement("Select * from files where username = ?;");

    }

    //String id here , int id in db , will it work ?
    public static boolean insertFile(String name, String username, String path, String id) {
        try {
            psInsertFile.setString(1, id);
            psInsertFile.setString(2, name);
            psInsertFile.setString(3, username);
            psInsertFile.setString(4, path);
            psInsertFile.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public static boolean getAllFiles(String username) {
        try {
            psGetFiles.setString(1, username);
            psGetFiles.execute();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public static boolean getFile(String path) {
        try {
            psGetFileByPath.setString(1, path);
            psGetFileByPath.execute();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public static boolean getFile(String filename, String username) {
        try {
            psGetFile.setString(1, filename);
            psGetFile.setString(2, username);
            psGetFile.execute();
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

    public static boolean changeUserName(String oldUsername, String password, String newUserName) {
        try {

            psChangeUserName.setString(1, newUserName);
            psChangeUserName.setString(2, oldUsername);
            psChangePassword.setString(3, password);
            psChangeUserName.executeUpdate();

            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public static boolean changePassword(String username, String oldPassword, String newPassword) {
        try {
            //psChangePassword = connection.prepareStatement
            //("Update clients SET password = ? WHERE username = ? AND password = ?;");
            psChangePassword.setString(1, newPassword);
            psChangeUserName.setString(2, username);
            psChangePassword.setString(3, oldPassword);
            psChangeUserName.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public static boolean login(String username, String password) {
        try {
            psLogin.setString(1, username);
            psLogin.setString(2, password);
            return psLogin.execute();
        } catch (SQLException throwables) {
            return false;
        }
    }

    public static void disconnect() {
        try {
            psRegistration.close();
            psChangeUserName.close();
            psChangeUserName.close();
            psChangePassword.close();
            psLogin.close();
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

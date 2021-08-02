package noro.geekbrains.server;

import java.sql.*;

public class SQLHandler {
    private static Connection connection;
    private static PreparedStatement psGetUserName;
    private static PreparedStatement psRegistration;
    private static PreparedStatement psChangeUserName;
    private static PreparedStatement psChangePassword;
    private static PreparedStatement psLogin;

    public static boolean connect() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/filetransfer");
            prepareAllStatements();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void prepareAllStatements() throws SQLException {
        psGetUserName = connection.prepareStatement("SELECT username FROM clients WHERE username = ? AND password = ?;");
        psRegistration = connection.prepareStatement("INSERT INTO clients(username, password) VALUES (? ,? );");
        psChangeUserName = connection.prepareStatement("UPDATE clients SET username = ? WHERE username = ? AND password = ? ;");
        psChangePassword = connection.prepareStatement("Update clients SET password = ? WHERE username = ? AND Password = ?;");
        psLogin = connection.prepareStatement("Select username from clients where username = ? AND password = ?;");
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

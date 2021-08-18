package noro.geekbrains.server;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SQLHandler {
    private static Connection connection;
    private static PreparedStatement psRegistration;
    private static PreparedStatement psChangeUserName;
    private static PreparedStatement psChangePassword;
    private static PreparedStatement psLogin;
    private static PreparedStatement psInsertFile;
    private static PreparedStatement psGetUserFiles;
    private static PreparedStatement psGetFileById;


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
        psChangeUserName = connection.prepareStatement("UPDATE clients SET name = ? WHERE name = ? AND password = ? ;");
        psChangePassword = connection.prepareStatement("Update clients SET password = ? WHERE name = ? AND Password = ?;");
        psLogin = connection.prepareStatement("Select name from clients where name = ? AND password = ?;");
        psInsertFile = connection.prepareStatement("Insert INTO files(name , username , path) VALUES (?,?,?);");
        psGetUserFiles = connection.prepareStatement("select * from files where username = ?;");
        psGetFileById = connection.prepareStatement("Select * from files where id = ?;");
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

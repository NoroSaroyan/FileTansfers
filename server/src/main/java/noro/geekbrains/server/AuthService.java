package noro.geekbrains.server;

public interface AuthService {
    boolean registration(String login, String password);

    boolean login(String login, String password);

}

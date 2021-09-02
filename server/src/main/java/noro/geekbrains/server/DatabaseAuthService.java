package noro.geekbrains.server;

public class DatabaseAuthService implements AuthService {

    public DatabaseAuthService() {

    }

    @Override
    public boolean registration(String login, String password) {
        return SQLHandler.registration(login, password);
    }

    @Override
    public boolean login(String login, String password) {
        return SQLHandler.login(login, password);

    }
}

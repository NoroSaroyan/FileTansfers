package noro.geekbrains.server;

import java.util.ArrayList;
import java.util.List;

public class SimpleAuthService implements AuthService {
    private class UserData {
        String login;
        String password;

        public UserData(String login, String password) {
            this.login = login;
            this.password = password;
        }
    }

    private List<UserData> users;

    public SimpleAuthService() {
        users = new ArrayList<>();
        users.add(new UserData("qwe", "qwe"));
        users.add(new UserData("asd", "asd"));
        users.add(new UserData("zxc", "zxc"));

        for (int i = 1; i < 10; i++) {
            users.add(new UserData("login" + i, "pass" + i));
        }
    }

    @Override
    public boolean registration(String login, String password) {
        for (UserData user : users) {
            if (user.login.equals(login)) {
                return false;
            }
        }

        users.add(new UserData(login, password));
        return true;
    }

}


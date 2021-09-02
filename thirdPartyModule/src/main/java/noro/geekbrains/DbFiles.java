package noro.geekbrains;

import java.io.Serializable;

public class DbFiles implements Serializable {

    public Integer Id;
    public String Name;
    public String Username;
    public String Path;

    public DbFiles() {
    }

    public DbFiles(Integer id, String name, String username, String path) {
        Id = id;
        this.Name = name;
        this.Username = username;
        this.Path = path;
    }

    @Override
    public String toString() {
        return this.Name;
    }
}

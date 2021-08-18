package noro.geekbrains.server;

public class DbFiles {
    public Integer Id;
    public String Name;
    public String Username;
    public String Path;

    public DbFiles(Integer id, String name, String username, String path) {
        Id = id;
        this.Name = name;
        this.Username = username;
        this.Path = path;
    }
    @Override
    public String toString() {
        return "DbFiles{" +
                "Id=" + Id +
                ", Name='" + Name + '\'' +
                ", Username='" + Username + '\'' +
                ", Path='" + Path + '\'' +
                '}';
    }
}

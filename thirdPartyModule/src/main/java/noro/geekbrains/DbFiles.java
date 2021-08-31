package noro.geekbrains;

import java.io.Serializable;

public class DbFiles implements Serializable {
    //@JsonProperty
    public Integer Id;
    //@JsonProperty
    public String Name;
    //@JsonProperty
    public String Username;
    // @JsonProperty
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
//        return "noro.geekbrains.DbFiles{" +
//                "Id=" + Id +
//                ", Name='" + Name + '\'' +
//                ", Username='" + Username + '\'' +
//                ", Path='" + Path + '\'' +
//                '}';
        return this.Name;
    }
}

package Zookeeper;

import java.io.Serializable;

//注意：一定要实现序列化接口  implements Serializable
public class User implements Serializable {

    private Integer id;
    private String name;

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
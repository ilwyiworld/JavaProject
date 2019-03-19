package com.znv.model;

/**
 * Created by Administrator on 2017/7/21.
 * 如果客户端向服务器端post Object对象，则需要在Object对象上加入注解：@XmlRootElement
 * 如果post的是一个Object集合，则会报错。
 * 解决方法是自己包装一个对象，加入@XmlRootElement注解，将list当作此对象中的一个普通属性来post。
 */
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class StudentForm {
    private List<Student> list;

    public List<Student> getList() {
        return list;
    }

    public void setList(List<Student> list) {
        this.list = list;
    }

}

package com.yiworld.register;

import com.yiworld.framework.URL;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;

public class RemoteRegister {
    private static Map<String, List<URL>> REGISTER = new HashMap<>();

    public static void register(String interfaceName, URL url) {
        List<URL> list= Collections.singletonList(url);
        REGISTER.put(interfaceName, list);
        saveFile();
    }

    public static URL random(String interfaceName) {
        Map<String, List<URL>> file=getFile();
        List<URL> list=file.get(interfaceName);
        Random random=new Random();
        int n=random.nextInt(list.size());
        return  list.get(n);
    }

    public static void saveFile(){
        try{
            FileOutputStream fileOutputStream=new FileOutputStream("/temp.txt");
            ObjectOutputStream oom=new ObjectOutputStream(fileOutputStream);
            oom.writeObject(REGISTER);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static Map<String,List<URL>> getFile(){
        try{
            FileInputStream fileInputStream=new FileInputStream("/temp.txt");
            ObjectInputStream oim=new ObjectInputStream(fileInputStream);
            return (Map<String, List<URL>>) oim.readObject();
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
}

package com.yiworld.protocol.http;

import com.yiworld.framework.Invocation;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpClient {
    public String send(String hostname, Integer port, Invocation invocation) {
        try {
            URL url = new URL("http", hostname, port, "/");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            OutputStream outputStream = connection.getOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(outputStream);

            oos.writeObject(invocation);
            oos.flush();
            oos.close();

            InputStream inputStream = connection.getInputStream();
            String result = IOUtils.toString(inputStream);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

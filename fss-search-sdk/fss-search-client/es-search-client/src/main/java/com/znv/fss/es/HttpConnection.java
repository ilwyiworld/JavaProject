package com.znv.fss.es;

import com.alibaba.fastjson.JSONObject;
import com.znv.fss.common.errorcode.FssErrorCodeEnum;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by User on 2017/8/3.
 */
public class HttpConnection {
    private HttpURLConnection connection;

    protected static final Logger LOGGER = LogManager.getLogger(HttpConnection.class);

    public HttpConnection() {

    }

    public JSONObject esHttpConnect(String esurl) {
        try {
            // 创建连接
            URL url = new URL(esurl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.connect();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            LOGGER.error("can't get the result，please check the connection!" + e);
            JSONObject errorCode = new JSONObject(true);
            errorCode.put("errorCode", FssErrorCodeEnum.ES_HTTP_FAILED_CONNECTION.getCode());
            return errorCode;
        } catch (Exception e) {
            LOGGER.error("Exception: \n" + e);
        }
        LOGGER.info("http connect success!!!");
        return null;
    }

    // 发送post请求
    public void esHttpPost(JSONObject obj) {
        // POST请求
        try {
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            out.write(obj.toString().getBytes("UTF-8"));// 这样可以处理中文乱码问题
            out.flush();
            out.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            LOGGER.error("can't get the result，please check your parameters" + e);
        } catch (Exception e) {
            LOGGER.error("Exception: \n" + e);
        }
    }

    // 发送post请求
    public void esHttpPost(String obj) {
        // POST请求
        try {
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            out.write(obj.getBytes("UTF-8"));// 这样可以处理中文乱码问题
            out.flush();
            out.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            LOGGER.error("can't get the result，please check your parameters" + e);
        } catch (Exception e) {
            LOGGER.error("Exception: \n" + e);
        }
    }

    // 读取结果响应
    public StringBuffer esHttpGet() {
        StringBuffer sb = new StringBuffer("");
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String lines;
            while ((lines = reader.readLine()) != null) {
                lines = new String(lines.getBytes(), "UTF-8");
                sb.append(lines);
            }
            reader.close();

        } catch (IOException e) {
            sb.append(FssErrorCodeEnum.ES_GET_EXCEPTION.getCode());
            // TODO Auto-generated catch block
            LOGGER.error("can't get the result，please check the connection or parameters" + e);
        } catch (Exception e) {
            sb.append(FssErrorCodeEnum.ES_GET_EXCEPTION.getCode());
            LOGGER.error("Exception: \n" + e);
        }
        return sb;
    }

    // 断开连接
    public void esHttpClose() {
        try {
            if (connection != null) {
                connection.disconnect();
            }
        } catch (Exception e) {
            LOGGER.error("Http failed to close！" + e);
        }
    }
}

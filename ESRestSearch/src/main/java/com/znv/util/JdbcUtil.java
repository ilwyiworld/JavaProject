package com.znv.util;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JdbcUtil {

    private static final Logger LOGGER = LogManager.getLogger(JdbcUtil.class.getName());

    private static ComboPooledDataSource ds = null;
    private static Connection conn = null;
    private static PreparedStatement ps = null;
    private static ResultSet rs = null;

    //在静态代码块中创建数据库连接池
    static{
        try{
            ds = new ComboPooledDataSource("ZNVR");//使用C3P0的命名配置来创建数据源
        }catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }
    
    /**
    * @Method: getConnection
    * @Description: 从数据源中获取数据库连接
    * @return Connection
    * @throws SQLException
    */ 
    public static Connection getConnection() throws SQLException{
        //从数据源中获取数据库连接
        return ds.getConnection();
    }

    public static List<Object> query(String sql) {
        if(sql.equals("") || sql == null){
            return null;
        }
        List<Object> list = new ArrayList<Object>();
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnNum = rsmd.getColumnCount();
            // 将数据封装到list中
            while (rs.next()) {
                Object[] objects = new Object[columnNum];
                for (int i = 0; i < objects.length; i++) {
                    objects[i] = rs.getObject(i + 1);
                }
                list.add(objects);
            }
        } catch (Exception e) {
            LOGGER.error("查询Mysql出错",e);
        } finally{
            release(conn,ps,rs);
        }
        return list;
    }

    /**
    * @Description: 释放资源，
    * 释放的资源包括Connection数据库连接对象，负责执行SQL命令的Statement对象，存储查询结果的ResultSet对象
    */ 
    public static void release(Connection conn,Statement st,ResultSet rs){
        if(rs!=null){
            try{
                //关闭存储查询结果的ResultSet对象
                rs.close();
            }catch (Exception e) {
                e.printStackTrace();
            }
            rs = null;
        }
        if(st!=null){
            try{
                //关闭负责执行SQL命令的Statement对象
                st.close();
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(conn!=null){
            try{
                //将Connection连接对象还给数据库连接池
                conn.close();
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
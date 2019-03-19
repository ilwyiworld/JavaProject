package com.znv.fss.phoenix;
import com.znv.fss.utils.ConnectionPool;

import java.sql.*;

/**
 * Created by ct on 2017/5/24.
 */
public class ConnecttionPoolTest {
    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        // TODO Auto-generated method stub
        try {
            // 创建数据库连接库对象
            ConnectionPool connPool = new ConnectionPool("org.apache.phoenix.jdbc.PhoenixDriver",
                "jdbc:phoenix:10.45.157.96:2181:/hbase-unsecure", "", "");
            // 新建数据库连接库
            connPool.createPool();
            // SQL测试语句
            String sql = "Select * from fss_blacklist_smart limit 10";
            // 设定程序运行起始时间
            long start = System.currentTimeMillis();
            // 循环测试100次数据库连接
            for (int i = 0; i < 100; i++) {
                Connection conn = connPool.getConnection(); // 从连接库中获取一个可用的连接
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                while (rs.next()) {
                    // String name = rs.getString("PERSONNAME");
                    // System.out.println("查询结果" + name);
                }
                rs.close();
                stmt.close();
                connPool.returnConnection(conn);// 连接使用完后释放连接到连接池
            }
            System.out.println("经过100次的循环调用，使用连接池花费的时间:" + (System.currentTimeMillis() - start) + "ms");
            // connPool.refreshConnections();//刷新数据库连接池中所有连接，即不管连接是否正在运行，都把所有连接都释放并放回到连接池。注意：这个耗时比较大。
            connPool.closeConnectionPool();// 关闭数据库连接池。注意：这个耗时比较大。
            // 设定程序运行起始时间
            start = System.currentTimeMillis();
            // 导入驱动
            Class.forName("org.apache.phoenix.jdbc.PhoenixDriver");
            Connection conn = DriverManager.getConnection("jdbc:phoenix:10.45.157.96:2181:/hbase-unsecure", "", "");
            for (int i = 0; i < 100; i++) {
                // 创建连接
                // Connection conn = DriverManager.getConnection(
                // "jdbc:phoenix:10.45.157.96:2181:/hbase-unsecure", "", "");
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                while (rs.next()) {
                    // String name = rs.getString("PERSONNAME");
                    // System.out.println("查询结果" + name);
                }
                rs.close();
                stmt.close();
                conn.close();// 关闭连接
            }
            System.out.println("经过100次的循环调用，不使用连接池花费的时间:" + (System.currentTimeMillis() - start) + "ms");
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}

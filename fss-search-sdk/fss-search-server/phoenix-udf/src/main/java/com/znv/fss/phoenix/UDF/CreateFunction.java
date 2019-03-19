package com.znv.fss.phoenix.UDF;

import com.znv.fss.common.utils.PropertiesUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import sun.misc.BASE64Decoder;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

//import java.sql.*;

/**
 * Created by Administrator on 2017/2/24.
 */
public class CreateFunction {
    private static final Log LOG = LogFactory.getLog(CreateFunction.class);

    public static void main(String[] args) {
        // String ipAddr = "littlev.server01";
        /*
         * String ipAddr = "lv09.dct-znv.com"; InetAddress a; try { ipAddr = InetAddress.getLocalHost().getHostName(); }
         * catch (UnknownHostException e) { e.printStackTrace(); } String url = "jdbc:phoenix:" + ipAddr +
         * ":2181:/hbase-unsecure";
         */
        // String hdfsurl = "hdfs://lv102.dct-znv.com:8020/user/fss_V113/development";//
        String hdfsurl = args[0];
        Properties confProp = null;
        try {
            confProp = PropertiesUtil.loadFromHdfs(hdfsurl + "/config/fss.properties");
        } catch (IOException e) {
            LOG.error(e);
            return;
        }
        String phoenixurl = PropertiesUtil.get(confProp, "pheonix.jdbc.url");
        try {
            Class.forName("org.apache.phoenix.jdbc.PhoenixDriver");
        } catch (ClassNotFoundException e) {
            LOG.error(e);
            return;
        }
        Connection conn = null;
        try {
            Properties props = new Properties();
            props.setProperty("phoenix.functions.allowUserDefinedFunctions", "true");
            if (PropertiesUtil.get(confProp, "phoenix.schema.isNamespaceMappingEnabled").equals("true")) {
                props.setProperty("phoenix.schema.isNamespaceMappingEnabled", "true");
            }
            conn = DriverManager.getConnection(phoenixurl, props);
            conn.setAutoCommit(true);
            try {
                dropFun(conn);
                createFun(conn);
                testComp(conn);
                System.out.println("create function .");
            } catch (Exception e) {
                LOG.error(e);
            }
        } catch (SQLException e) {
            LOG.error(e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {
                    LOG.error(e);
                }
            }
        }
    }

    static void createFun(Connection conn) {
        // String createFun = "CREATE FUNCTION FeatureComp(VARBINARY,VARCHAR) returns INTEGER as
        // 'com.phoenix.UDF.FeatureCompFunction' using jar
        // 'hdfs://littlev.server01:8020/apps/hbase/data/lib/PhoenixUDF.jar'";
        // String createFun = "CREATE FUNCTION FeatureComp(VARBINARY,VARBINARY) returns INTEGER  as 'com.znv.fss.phoenix.UDF.FeatureCompFunction'";
        String createFun = "CREATE FUNCTION FeatureComp(VARBINARY,VARBINARY) returns FLOAT  as 'com.znv.fss.phoenix.UDF.FeatureCompFunction'";
        // String createFun = "CREATE FUNCTION FeatureComp(VARBINARY,VARBINARY) returns FLOAT as
        // hdfs://znv.bfw04:8020/apps/hbase/data/lib/PhoenixUDF.jar
        // 'com.phoenix.UDF.FeatureCompFunction' using jar 'hdfs://znv.bfw02:8020/apps/hbase/data/lib/PhoenixUDF.jar'";
        Statement statMent = null;
        try {
            statMent = conn.createStatement();
            statMent.execute(createFun);
        } catch (SQLException e) {
            LOG.error(e);
        } finally {
            if (statMent != null) {
                try {
                    statMent.close();
                } catch (Exception e) {
                    LOG.error(e);
                }
            }
        }
    }

    static void dropFun(Connection conn) {
        String createFun = "DROP  FUNCTION IF EXISTS FeatureComp";
        Statement statMent = null;
        try {
            statMent = conn.createStatement();
            statMent.execute(createFun);
        } catch (SQLException e) {
            LOG.error(e);
        } finally {
            if (statMent != null) {
                try {
                    statMent.close();
                } catch (Exception e) {
                    LOG.error(e);
                }
            }
        }
    }

    static void testComp(Connection conn) throws Exception {
        String feature2 = "7qpXQpFlAACAAAAAjM0QvpuPOr1BVwy9jX4svslmoLw/3mu9eVoLvuDXMLsnQrU9a+JFPOshtr2jfYe9EEpUPndMSj7LSqU85Da7PBRNv70/Vsi6BXZ+PVS8Q71JzlQ8LAO7vSHuMr1b+eA97Qq5PUTnAL4r+qG9JhSAvTHxjbzS+Ja6+cxnPeQTSj1WJ1W9KnCEvOVUhL1fLQu9BQR7PF3zzT1qz8O9WNtVvZ5nnr0UHSY9bAdevV3uwD1xxsY8sw+ZvN07Xz3eq1q+3ORYO9FstLsj2TI8s4TCvGQeBr61PAo+NrUZvkfVIb3ukYI8xPDuPTSL9jpBotG9cNa7PBET8T2guyE83EsIPv33GDs0ZuW7w06QPeMwgb09I9A9QGuUvJLycT0DZp+9t+B8vJDj9j2cYUE9H5sqvssTgT0r6Re+tByhve4j2r02f8A7PUZJvrZh+r3oeCi9eyPiO1fYArwGgzU+ykcqvQBG8r3sSbO8MhYyu3YarT0uPpi9tjhlPpae5L1zN3Q6n1l1PRpAUT13TwQ+rkqwuxpgjb2OPzs9W6W3POnDnbzI9/q92PSrPAcPJr7V4F08Xfx2vbUIqr0RQcY6oTbvO+Jbz701duK9XA8zvqZvg71p3L28q6cWPNO3rL2bOiY+h4b0vWcqhLwljxC9Mn8jvamzTL0ZqPQ9wqjVvZb58r0=";
        ResultSet rs = null;
        BASE64Decoder deoder = new BASE64Decoder();
        byte[] bf = deoder.decodeBuffer(feature2);
        // String selectStr = "SELECT * from {SELECT FEATURECOMP(FEATURE,'"+ feature5 +"') FROM FSS_BLACKLIST where"+"
        // FEATURECOMP(FEATURE,'"+ feature5 +"') > 0.0 LIMIT 30}";
        String selectStr1 = "select personId,personName,age,sex,imageName,imageData,feature,fcPid,startTime,endTime,controlLevel,flag,FeatureComp(feature,?) as sim from FSS_BLACKLIST  where FeatureComp(feature,?) > 89 order by FeatureComp(feature,?) desc limit 10";
        String selectStr = "SELECT FeatureComp(FEATURE,?) as sim FROM FSS_DEVELOP_410.FSS_PERSONLIST_V1_1_3_20170727 where FeatureComp(FEATURE,?) > 80 order by FeatureComp(FEATURE,?) desc limit 10";
        // FeatureComp
        String selectStr2 = "SELECT COUNT(FEATURECOMP(FEATURE,?)) FROM FSS_BLACKLIST_TEST_0117";
        String selectStr3 = "SELECT COUNT (FEATURE) FROM FSS_BLACKLIST_TEST_0117";
        // long start = System.currentTimeMillis();
        try (PreparedStatement stat = conn.prepareStatement(selectStr)) {
            stat.setObject(1, bf);
            stat.setObject(2, bf);
            stat.setObject(3, bf);
            long start = System.currentTimeMillis();
            rs = stat.executeQuery();
            // rs = conn.createStatement().executeQuery(selectStr1);
            while (rs.next()) {
                System.out.println(rs.getString("sim"));
            }
            long end = System.currentTimeMillis();
            System.out.println("cost:" + (end - start));
        } catch (Exception e) {
            LOG.error(e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                    LOG.error(e);
                }
            }
        }
    }
}

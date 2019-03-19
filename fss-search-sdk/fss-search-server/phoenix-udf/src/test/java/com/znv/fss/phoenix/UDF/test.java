package com.znv.fss.phoenix.UDF;

import com.znv.fss.common.utils.PropertiesUtil;
import sun.misc.BASE64Decoder;

import java.io.IOException;
import java.net.InetAddress;
import java.sql.*;
import java.util.Properties;

/**
 * Created by Administrator on 2016/12/27.
 */
class test {
    public static void main(String[] args) {
        // String ipAddr = "littlev.server01";
        String ipAddr = "lv101.dct-znv.com";
        InetAddress a;
        // try {
        // ipAddr = InetAddress.getLocalHost().getHostName();
        // } catch (UnknownHostException e) {
        // e.printStackTrace();
        // }
        String hdfsurl = "hdfs://lv102.dct-znv.com:8020/user/fss_V113/development";// args[0];
        Properties confProp = null;
        try {
            confProp = PropertiesUtil.loadFromHdfs(hdfsurl + "/config/fss.properties");
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        String phoenixurl = PropertiesUtil.get(confProp, "pheonix.jdbc.url");
        try {
            Class.forName("org.apache.phoenix.jdbc.PhoenixDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }
        try {
            Properties props = new Properties();
            props.setProperty("phoenix.functions.allowUserDefinedFunctions", "true");
            if (PropertiesUtil.get(confProp, "phoenix.schema.isNamespaceMappingEnabled").equals("true")) {
                props.setProperty("phoenix.schema.isNamespaceMappingEnabled", "true");
            }
            Connection conn = DriverManager.getConnection(phoenixurl, props);
            conn.setAutoCommit(true);
            try {
                // dropFun(conn);
                // createFun(conn);
                // testReverse(conn);
                testComp(conn);
                // System.out.println("create function .");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static void createFun(Connection conn) {
        // String createFun = "CREATE FUNCTION FeatureComp(VARBINARY,VARCHAR) returns INTEGER as
        // 'com.phoenix.UDF.FeatureCompFunction' using jar
        // 'hdfs://littlev.server01:8020/apps/hbase/data/lib/PhoenixUDF.jar'";
        String createFun = "CREATE FUNCTION FeatureComp(VARBINARY,VARBINARY) returns INTEGER  as 'com.phoenix.UDF.FeatureCompFunction'";
        // String createFun = "CREATE FUNCTION FeatureComp(VARBINARY,VARBINARY) returns FLOAT as
        // hdfs://znv.bfw04:8020/apps/hbase/data/lib/PhoenixUDF.jar
        // 'com.phoenix.UDF.FeatureCompFunction' using jar 'hdfs://znv.bfw02:8020/apps/hbase/data/lib/PhoenixUDF.jar'";
        Statement statMent = null;
        try {
            statMent = conn.createStatement();
            statMent.execute(createFun);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static void dropFun(Connection conn) {
        String createFun = "DROP  FUNCTION IF EXISTS FeatureComp";
        Statement statMent = null;
        try {
            statMent = conn.createStatement();
            statMent.execute(createFun);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static void testComp(Connection conn) throws Exception {
        String feature1 = "7qpXQpFlAACAAAAADWVQPlQ6VLyzahA7d62VOlz90L2O0Ce+L+sOva1ru710cIw8vgCoOqtLs72B+AW9T5VmPVvETr2vb0q9a5zGPc4Igj12VjE+efTuvHTTqT3MLVI8ezG6vFyTmzzEsqK9RnmcvHLFFj1ZW4k9j0GMvT3hZb4FFNG7NZkCPmNpTj1XCaU9mFwHPkgztL3shg0+W4+NPIIklL3SaRi+s1qzvYUlPzym1xO9zMYwvtkU+r1H8hk+1WFMvIjAMDzzSOc9lEzqPbjUlrsUUrI9z5t+vcAgvL2DgAW995ZDveVYlj2knvW9PgzjvOMVHz33hfy7nSExPlO1cL3PTqU8y04KvhgJET10v4U8eGKGPbMfPb7eUKW8C/KhPfL0jz0878o9Lw0LPOC2nD13XFi9GGMtPNM9Xz3m+4i9WJDKPc9NML3SU0u9kHycPYV+AjwRYqK8KoxfPTGJaz5r2ts9Q5f+vGlNM77B5Ag9L2SMvekv3j2KrKk8Kt62vRwa/Lyn3sI93ihevMnefb5vTfo9S+/BvRT6bzwIoa89k7bvvMHWp7tGnIe81by4Og3xHTvW2HS9N2RRPYGR/L1H5qK9Pus3PhmLpTp5tlQ8uYz6PfIRvb0h1C6+J61LvKMkXD0DZGk8qmMVPexC2Ty56YI9fB+BvY6aNTwGx8m911+euXI28r0=";
        String feature2 = "VU4AAAAAAAAMAgAAqN//vWfwcT1bLn89DOS5PAZGIb6lNni7TtrqPOmyWr6I/s+9ASLAOv3Pmj0DuS68y/+2vViBpb1yDQ2+i8gOPiJrBT2lb+w9wnmCvH3khr1pj149d9HuPXoIubxSsgg+HwQCPrg8dz01/Dm8K0b3PPaxir1AaJA9LpDZvQyp3D32R/m7wKpYvbbpUD3zmBy7Xo8uPUQo6DyodTy9hj7xOZza9T0qFO480aWNvAJQ+rtu7u88LcWOPSO1CL5uh1s9bLHMvZHV3Lw/0FC983NhveW3Cb67kYC9AOcSvn9ArzyZJCy9oD7mPSX9k71CrF49AvxzvpjLsDvXKAM+9T+iPeOLW7wnGhY9ed1OvWyCYT1kMaM9MhoOvoI3iLxZpMW9x2jJPZEIsLyI+u+9kv0hvdPIyb0WpJA9U1czPBAoBL0FGMi8pMWxvKH0ij3XoT68BqOcvT3fO735HtC9N48avjQo3L3+bDw+3gfrvVZg4LwrPyS+6K7FPGlMXz0i1xa9OR13vQrx7bzW3wg9FAyHvSShybxq/CG8ghl1PFMzrT2FonY9CdmqPfDCmz226ls9m80YPuh2or27z0Q8bTiMPWcMMr6C3gq9NYYrPrrdm71uUS++ovO2O7HZ0jzPc4Q9QyIJvjyjnLyKWBY8fLVfvpu2WrzOaGK9CcI/vpJUEr4=";
        String feature3 = "VU4AAAAAAAAMAgAA+foVvRj2zr3bnri97ISIvO5BEr6gnUg+W28IvRPm7j1FOhY+Wc4QvlwE8LwAITA+SHXhPASrxb1DmSQ+Vo2CvfB8Tb3cdts6QNOgvb9zhj0oOJM9LWQdvuyin70zxbM8MvOFvWIuFL2mYR89hzNOvdp0Lz05wk697M7yuy3PSTsry0K9Umi/PfncRj06JZO9tHUPvszfnz3MzOO97lXoPQHlGT66/Yc9DDcQPdZ1JbzHrR29BODWPN6i5r3TepM+NijFPQSr0rzufuE9Or7tvfnjTb2aHP26WU6QvbaE6700nlq9+BzCPWnPtb2ok9u9kWj4vf2v/DzF8Dc9xCqRPi1RxTzuq229JYmdvamPjj3o3Mu79rSbvcxMIj5P24m9HLwCPlIgVj4/XKo8n8fUPQNueL1fTSa9w0PMPOjD1by2hcW9o7alPezVqr3EhJc92d9DPZB7Pb65VQG9hQf0vNZlCL32paQ9xQcAPnNYvD2LB0i95ZoMvRdr5zvXU1k+5wCFPBnwHT6hatg8lC1bvCyNVj21/hm9ZSfeu8Enw7z2IVy9fF/APBmDID0syRK9XTWBPDbnPz1H7PU8O4X1PMFaczwoNv48gWfTvBFAuzxsL0M8xjfavOQUAD3dLhO93Mb3vJFWCL3eEt69O8yPPa9XSzoBuNM9VSfuvCqwKTo=";
        String feature4 = "VU4AAAAAAAAMAgAAlFvePXtOEDqdUJk7AAQ0PcRgaL2D5V89o8H3vWZOhb0L7R0+DEqovaFwkbzSqGM98/ZOuaaIyr02loo9FmepPWxxBb2Khoc955vIPV2Qyr3UuSK7fHRjPkUHwr18HAs+rB2HPVlgCb1rbCQ9W8q/PVJF5L1oyW4+aHHNPXJLhTtSGVc8Zq1lvoI6Lz34geW82UecvX7OUzwgjEc9kSKpvdzbGDxxH/i8YYMTvs6CiLwAC429XNHaPUqXmr2dgxM9kEoAPsATyr0C6DM9xDVzPQQ0lr1R1DO9Ra2avCh68T3BZH2+CmpBPexdET555cs9gNlyPbdAAD5q8j++YztyPI8VTb3cHYq9ZIb9vVPZVD3LTUY+dBonvXRfgL39Ewq+Iw8FPkag1zwAMJy89o+UPeTqiz0fsSm+5HARPV/h27wBViA7f0SbPdKPHT6lB9q9tNH8PdRtrLwVV3k9VIWEvaY3ZbwNjQI+blXoPKe297vU3oa6WbcMvaBjATzWd5c9bl+vvSbjyDw5hJC7ophXvZacyLybovC6ZaSlvBv/2zwkDac8Zu9sPQ05lLxV7TQ8RzEePu1mCT0azB09YRtiPSfmyrtsp1q8sTyDPdsVPj2R6tS9QVQMPdiVVL0Zjcg9Knv7vWSuDT6hzUG+XCkIvrERj7wvpMO8NdLmO58OFjw=";
        String feature5 = "VU4AAAAAAAAMAgAA1lP+vSDwBT1s7Ii9RKIlPVbLGb0n/0G+gQlKvmKsBL0SZ0G9P1d0Pa42272E"
            + "ZAa9GVWtPLY49rzxc4A8fzz1vKAYGD2h2rU90OXTPGBbJL5HUaC9dACzvEnGsr2gDyI917McPtFi"
            + "/r00voA9aeA2vd7yEbxG50U8ewUDvUd9vr0eT+29fmOmO9gfrTyC4xm+ViT3Pc5RjD1XUzW8biBw"
            + "vSZG4D2jna89nQsqPhVKOr5lrT27IN78O6r+KD7hWww9MGu8PbuYhr0VEkq9ba2QvCIVqjy6EG89"
            + "NhnAPSEs/D1YRSE+ihBJvTrNP7xU3sm9w5v0vTaiRT6mG3O9kostu1InyD26tgW+WIeJPci7Cr72"
            + "MAe+/5EcvX1aEz2GVI49IwLhOx850z3vEwM9K2NBvmGkjbxvBhM+JYuNvfj7Pb4jFQW8j90pPb2D"
            + "FbvWbIa818aWvSBHMz4/+nC8B/mgOw9nhLzjuSG+LJv8PfF24L2QUPQ97ghjvLRMrz3hu9C8JKV8"
            + "vWDtvb2qCkO9LsfevA0sbDkBtWu7xTeovXfhAj7Sl/S8dbgOvYIVK73ivp+9Sdv/Ox2i5D39yIg9"
            + "ZR4RvpuT4z3lakm99T+jPAlyn72yPA29DCCYvQxlAj1wPpy8zacZPX/eAb41lPS9z0HNvUskrD2U" + "JwO9/mZjPQ+dqj0=";
        ResultSet rs;
        BASE64Decoder deoder = new BASE64Decoder();
        byte[] bf = deoder.decodeBuffer(feature2);
        // String selectStr = "SELECT * from {SELECT FEATURECOMP(FEATURE,'"+ feature5 +"') FROM FSS_BLACKLIST where"+"
        // FEATURECOMP(FEATURE,'"+ feature5 +"') > 0.0 LIMIT 30}";
        String selectStr1 = "select personId,personName,age,sex,imageName,imageData,feature,fcPid,startTime,endTime,controlLevel,flag,FeatureComp(feature,?) as sim from FSS_BLACKLIST  where FeatureComp(feature,?) > 89 order by FeatureComp(feature,?) desc limit 10";
        String selectStr = "SELECT FeatureComp(FEATURE,?) as sim FROM FSS_DEVELOP.FSS_PERSONLIST_V1_1_3_20170727 where FeatureComp(FEATURE,?) > 89 order by FeatureComp(FEATURE,?) desc limit 10";
        String selectStr2 = "SELECT COUNT(FEATURECOMP(FEATURE,?)) FROM FSS_BLACKLIST_TEST_0117";
        String selectStr3 = "SELECT COUNT (FEATURE) FROM FSS_BLACKLIST_TEST_0117";
        // long start = System.currentTimeMillis();
        PreparedStatement stat = conn.prepareStatement(selectStr);
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
    }

    static void testReverse(Connection conn) throws Exception {

        String ddl = "CREATE TABLE IF NOT EXISTS REVERSE_TEST (pk VARCHAR NOT NULL PRIMARY KEY)";
        conn.createStatement().execute(ddl);
        conn.setAutoCommit(false);
        String dml = "UPSERT INTO REVERSE_TEST VALUES('abc')";
        conn.createStatement().execute(dml);
        Thread.sleep(10000);
        conn.commit();

        ResultSet rs;
        rs = conn.createStatement().executeQuery("SELECT FeatureComp(pk) FROM REVERSE_TEST");
        rs.next();
        System.out.println(rs.getString(1));

        rs = conn.createStatement().executeQuery("SELECT pk FROM REVERSE_TEST WHERE pk=FeatureComp('cba')");
        rs.next();
        System.out.println(rs.getString(1));
    }

}

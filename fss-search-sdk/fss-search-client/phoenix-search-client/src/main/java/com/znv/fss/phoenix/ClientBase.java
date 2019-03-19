package com.znv.fss.phoenix;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.znv.fss.common.errorcode.FssErrorCodeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by ct on 2017-01-16.
 */
public class ClientBase {

    private static final Logger L = LoggerFactory.getLogger(ClientBase.class);

    /**
     * 执行无需返回结果的sql语句
     * @param sql sql语句
     * @param phoenixConn phoenix连接
     * @throws Exception 异常
     */
    public void executeSql(String sql, Connection phoenixConn) throws Exception {
        if (phoenixConn == null) {
            throw new Exception("phoenixConn is null");
        }
        Statement stat = phoenixConn.createStatement();
        try {
            stat.execute(sql);
            phoenixConn.commit();
        } catch (Exception e) {
            L.error("executeSql {} error: {}", sql, e);
            throw e;
        }finally {
            // 释放资源
            try {
                stat.close();
            } catch (Exception e) {
                L.info(" executeSql exception when close PreparedStatement !");
                //  e.printStackTrace();
                L.debug("exception:", e); // lq-modify 2018-05-28
            }
        }

    }

    /**
     * 添加数据
     * @param data 数据信息
     * @param tableName 表名
     * @param phoenixConn phoenix连接
     * @return JSONObject
     */
    public static JSONObject insert(JSONObject data, String tableName, Connection phoenixConn) {
        JSONObject result = new JSONObject();
        result.put("errorCode", FssErrorCodeEnum.SUCCESS.getCode());
        if (phoenixConn == null) {
            result.put("errorCode", FssErrorCodeEnum.PHOENIX_CONN_NULL.getCode());
            L.error("insert error, phoenixConn is null");
            return result;
        }

        StringBuilder insertSql = new StringBuilder();
        insertSql.append("UPSERT INTO ").append(tableName).append("(");
        for (String keys : data.keySet()) {
            insertSql.append(keys + ",");
        }
        if (insertSql.charAt(insertSql.length() - 1) == ',') {
            insertSql.deleteCharAt(insertSql.length() - 1);
        }
        insertSql.append(") VALUES(");
        for (String keys : data.keySet()) {
            insertSql.append("?,");
        }
        if (insertSql.charAt(insertSql.length() - 1) == ',') {
            insertSql.deleteCharAt(insertSql.length() - 1);
        }
        insertSql.append(")");
        // L.info("insertSql {}", insertSql);

        int i = 1;
        PreparedStatement stat = null;
        try {
            stat = phoenixConn.prepareStatement(insertSql.toString());
            for (String key : data.keySet()) {
                stat.setObject(i, data.get(key));
                i++;
            }
            stat.executeUpdate();
            phoenixConn.commit();
        } catch (SQLException e) {
            result.put("errorCode", FssErrorCodeEnum.PHOENIX_SYS_ERROR.getCode());
            // e.printStackTrace();
            L.error(" Insert exception when close PreparedStatement !",e);
        }finally {
            // 释放资源
            try {
                if (stat != null){
                    stat.close();
                }
            } catch (SQLException e) {
                L.error(" Insert exception when release resource !",e);
                // e.printStackTrace();
            }
        }

        return result;
    }

    /**
     * 添加批量数据
     * @param dataList 数据信息
     * @param tableName 表名
     * @param phoenixConn phoenix连接
     */
    public static JSONObject batchInsert(JSONArray dataList, String tableName, Connection phoenixConn) {
        JSONObject result = new JSONObject();
        result.put("errorCode", FssErrorCodeEnum.SUCCESS.getCode());
        // 增加批量输入结果集,默认全部为error
        JSONArray insertResult = new JSONArray(dataList.size());
        for (int idx = 0; idx < dataList.size(); idx++) {
            JSONObject obj = new JSONObject(dataList.size());
            obj.put("id", idx);
            obj.put("errorCode", "error");
            insertResult.add(idx, obj);
        }
        PreparedStatement stat = null;

        try {
            phoenixConn.setAutoCommit(false);
            if (phoenixConn == null) {
                L.error("batch insert error, phoenixConn is null");
                result.put("errorCode", FssErrorCodeEnum.PHOENIX_CONN_NULL.getCode());
                return result;
            }
            // 预编译SQL语句，只编译一次
            JSONObject firstData = dataList.getJSONObject(0);
            StringBuilder insertSql = new StringBuilder();
            insertSql.append("UPSERT INTO ").append(tableName).append("(");
            for (String keys : firstData.keySet()) {
                insertSql.append(keys + ",");
            }
            if (insertSql.charAt(insertSql.length() - 1) == ',') {
                insertSql.deleteCharAt(insertSql.length() - 1);
            }
            insertSql.append(") VALUES(");
            for (String keys : firstData.keySet()) {
                insertSql.append("?,");
            }
            if (insertSql.charAt(insertSql.length() - 1) == ',') {
                insertSql.deleteCharAt(insertSql.length() - 1);
            }
            insertSql.append(")");
            // L.info("insertSql {}", insertSql);
            stat = phoenixConn.prepareStatement(insertSql.toString());

            // 插入所有数据
            int len = dataList.size();
            int count = 0;
            int batchSize = 50;
            int lastKey = 0;
            for (int idx = 0; idx < len; idx++) {
                count++;
                JSONObject data = dataList.getJSONObject(idx);
                int i = 1;
                Iterator<Map.Entry<String, Object>> iterator = data.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, Object> entry = iterator.next();
                    stat.setObject(i, entry.getValue());
                    i++;
                }
                stat.addBatch();

                if (count % batchSize == 0) { // 1000条数据提交一次
                    stat.executeBatch();
                    phoenixConn.commit();

                    // 写入成功的数据的errorCode刷写为success
                    lastKey = count;
                    int page = count / batchSize;
                    int startKey = batchSize * (page - 1);
                    int endKey = count;
                    for (int j = startKey; j < endKey; j++) {
                        insertResult.remove(j);
                        JSONObject obj = new JSONObject();
                        obj.put("id", j);
                        obj.put("errorCode", "success");
                        insertResult.add(j, obj);
                    }
                }
            }
            stat.executeBatch();
            phoenixConn.commit();
            // 写入成功的数据的errorCode刷写为success
            if (lastKey != len /* (len - 1) */) {
                for (int j = lastKey; j < len; j++) {
                    insertResult.remove(j);
                    JSONObject obj = new JSONObject();
                    obj.put("id", j);
                    obj.put("errorCode", "success");
                    insertResult.add(j, obj);
                }
            }
            result.put("data", insertResult);

            // System.out.println(String.format("批量写入 %d 条数据！", dataList.size()));
            phoenixConn.setAutoCommit(true);
        } catch (SQLException e) {
            result.put("errorCode", FssErrorCodeEnum.PHOENIX_SYS_ERROR.getCode());
            // e.printStackTrace();
            L.error("batchInsert exception ",e);
        }finally {
            // 释放资源
            try {
                if (stat != null){
                    stat.close();
                }
            } catch (SQLException e) {
                L.error(" batchInsert exception when close PreparedStatement !",e);
                // e.printStackTrace();
            }
        }

        // L.info("批量写入 {} 数据！", dataList.size());
        return result;
    }

    /**
     * 查询数据
     * @param sql 人的唯一标识
     * @return JSONObject
     * @throws Exception 异常,所有异常全部抛出，但是如果释放资源时出现异常则打印异常
     */
    public JSONObject query(String sql, Connection phoenixConn) throws Exception {
        if (phoenixConn == null) {
            throw new Exception("phoenixConn is null");
        }

        JSONObject result = new JSONObject();
        List<JSONObject> objList = new ArrayList<JSONObject>();
        PreparedStatement stat = null;
        ResultSet rs = null;
        try {
            stat = phoenixConn.prepareStatement(sql);
            rs = stat.executeQuery();
            while (rs.next()) {
                JSONObject record = new JSONObject();
                ResultSetMetaData rsMetaData = rs.getMetaData();
                int columnCount = rsMetaData.getColumnCount();
                for (int column = 0; column < columnCount; column++) {
                    String field = rsMetaData.getColumnLabel(column + 1);
                    record.put(field.toLowerCase(), rs.getObject(field));
                }
                objList.add(record);
            }
        } catch (SQLException e) {
            L.error("query error", e);
            throw e;
        }finally {
            // 释放资源
            try {
                if (stat != null){
                    stat.close();
                }
            } catch (SQLException e) {
                L.error(" query exception when close PreparedStatement !",e);
                // e.printStackTrace();
            }

            try {
                if (rs != null){
                    rs.close();
                }
            } catch (SQLException e) {
                L.error(" query exception when close PreparedStatement !",e);
                // e.printStackTrace();
            }
        }

        result.put("data", objList);
        return result;
    }
}

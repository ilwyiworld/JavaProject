package com.znv.fss.phoenix;

import com.alibaba.fastjson.JSONObject;
import com.znv.fss.common.VConstants;
import com.znv.fss.common.errorcode.FssErrorCodeEnum;
import com.znv.fss.conf.ConfigManager;
import com.znv.kafka.ProducerBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by Administrator on 2017/7/25.
 */
public final class LibConfigClient extends ClientBase {
    private static final Logger L = LoggerFactory.getLogger(LibConfigClient.class);
    private static ProducerBase producer = new ProducerBase();
    private String tableName = ConfigManager.getTableName(VConstants.FSS_LIB_CONFIG_V113_TABLE_NAME);
    private static final String LIBID = "lib_id";
    private static final String LIBNAME = "lib_name";

    // 单例
    private static LibConfigClient libConfigClient = null;

    static {
        init();
    }

    /**
     * 构造函数
     */
    private LibConfigClient() {
    }

    /**
     * @param phoenixConn
     * @return
     */
    public JSONObject getLibConfigAll(Connection phoenixConn) {
        JSONObject result = new JSONObject();
        result.put("errorCode", FssErrorCodeEnum.SUCCESS.getCode());
        String sql = String.format(" select * from %s ", tableName);
        if (phoenixConn == null) {
            result.put("errorCode", FssErrorCodeEnum.PHOENIX_CONN_NULL.getCode());
            L.error("insert error, phoenixConn is null");
        } else {
            try {
                JSONObject resultObj = super.query(sql, phoenixConn);
                result.put("data", resultObj);
            } catch (Exception e) {
                result.put("errorCode", FssErrorCodeEnum.PHOENIX_INVALID_SQL.getCode());
            }
        }

        return result;
    }

    /**
     * @param data libid必填，libname必填
     * @param phoenixConn
     * @return
     */
    public JSONObject insert(JSONObject data, Connection phoenixConn) {
        JSONObject result = new JSONObject();
        boolean isExist = false;
        if (phoenixConn == null) {
            result.put("errorCode", FssErrorCodeEnum.PHOENIX_CONN_NULL.getCode());
            L.error("insert error, phoenixConn is null");
        } else {
            // 不再做任何判断，有新的操作直接把数据写入到表中
            try {
                super.insert(data, tableName, phoenixConn);
            } catch (Exception e) {
                result.put("errorCode", FssErrorCodeEnum.PHOENIX_INVALID_SQL.getCode());
                L.error("error occur when insert data {}", e);
            }
            result.put("errorCode", FssErrorCodeEnum.SUCCESS.getCode());

        }

        return result;
    }

    /**
     * @param data
     * @param phoenixConn
     * @return
     */
    public JSONObject delete(JSONObject data, Connection phoenixConn) {
        JSONObject result = new JSONObject();
        result.put("errorCode", FssErrorCodeEnum.SUCCESS.getCode());
        if (phoenixConn == null) {
            result.put("errorCode", FssErrorCodeEnum.PHOENIX_CONN_NULL.getCode());
            L.error("insert error, phoenixConn is null");
        } else if (data.containsKey(LIBID) && null != data.getInteger(LIBID)) {
            int libId = data.getInteger(LIBID);
            String sql = String.format(" delete from %s where %s = %d", tableName, LIBID, libId);
            try {
                super.executeSql(sql, phoenixConn);
            } catch (Exception e) {
                result.put("errorCode", FssErrorCodeEnum.PHOENIX_INVALID_SQL.getCode());
                L.error("error occur when insert data {}", e);
            }
        }

        return result;
    }

    /**
     * @param libId
     * @param phoenixConn
     * @return result类型{"errorCode":"success","data":"jsonobject"}
     */
    public JSONObject queryLibName(int libId, Connection phoenixConn) {
        JSONObject result = new JSONObject();
        result.put("errorCode", FssErrorCodeEnum.SUCCESS.getCode());
        StringBuilder sql = new StringBuilder();
        // 组查询语句
        sql.append("select ").append(LIBID).append(",").append(LIBNAME).append(" from ").append(tableName)
            .append(" where ").append(LIBID).append(" = ").append(libId);
        // List<JSONObject> resList = new ArrayList<JSONObject>();
        ResultSet rs = null;
        try (PreparedStatement stat = phoenixConn.prepareStatement(sql.toString())) {
            rs = stat.executeQuery();
            while (rs.next()) {
                // JSONObject obj = new JSONObject();
                result.put(LIBID, rs.getInt(LIBID));
                result.put(LIBNAME, rs.getString(LIBNAME));
                // resList.add(obj);
            }

            // result.put("data", resList);

        } catch (SQLException e) {
            result.put("errorCode", FssErrorCodeEnum.PHOENIX_INVALID_SQL.getCode());
            L.error("error occur when querying data by lib_id ", e);
        }finally {
            if (rs != null){
                try {
                    rs.close();
                }catch (Exception e){
                    L.error("error occur when querying data by lib_id release resource ", e);
                }
            }
        }
        return result;

    }

    /**
     * @param query
     * @param phoenixConn
     * @return
     */
    public JSONObject query(JSONObject query, Connection phoenixConn) {
        JSONObject result = new JSONObject();
        result.put("errorCode", FssErrorCodeEnum.SUCCESS.getCode());
        if (phoenixConn == null) {
            result.put("errorCode", FssErrorCodeEnum.PHOENIX_CONN_NULL.getCode());
            L.error("error occur when querying libconfig data, phoenixConn is null");
        } else if (query.containsKey("count") && query.containsKey("total_page") && query.containsKey("page_no")
            && query.containsKey("page_size") && null != query.getInteger("count")
            && null != query.getInteger("total_page") && null != query.getInteger("page_no")
            && null != query.getInteger("page_size")) {
            // 判断必传的字段是否都存在

            List<JSONObject> resList = new ArrayList<>();
            long startTime = System.currentTimeMillis();
            // 首次查询先查询总条数和总页数，翻页查询不需要再次查询总条数和总页数
            int count = query.getInteger("count");
            int totalPage = query.getInteger("total_page");

            // 每页显示的条数
            if (query.containsKey("page_size") && null != query.getInteger("page_size")) {
                int pageSize = query.getInteger("page_size");
                // 当传入的每页行数为0时默认每页10条
                if (pageSize <= 0) {
                    pageSize = 10;
                }
                query.put("page_size", pageSize);
            }

            try {
                if (-1 == count && -1 == totalPage) {
                    // 首次查询先计算符合条件的总记录数
                    JSONObject total = organizeQuerySql(query, phoenixConn, 1);
                    count = total.getIntValue("count");
                    totalPage = total.getIntValue("total_page");
                    // L.info("query {},count: {},total page: {}", tableName, count, totalPage);
                }

                if (0 == count) {
                    // 查询结果为空
                    result.put("data", resList);
                    result.put("time", System.currentTimeMillis() - startTime);
                    result.put("count", count);
                    result.put("total_page", totalPage);
                    result.put("id", VConstants.QUERY_FSS_LIB_CONFIG_V113);
                } else {
                    // 查询结果中有值
                    result = organizeQuerySql(query, phoenixConn, 0);
                    result.put("time", System.currentTimeMillis() - startTime);
                    result.put("count", count);
                    result.put("total_page", totalPage);
                    result.put("id", VConstants.QUERY_FSS_LIB_CONFIG_V113);
                }

            } catch (Exception e) {
                result.put("errorCode", FssErrorCodeEnum.PHOENIX_SYS_ERROR.getCode());
                L.error("error occur when querying {} data", tableName, e);
            }
        } else {
            result.put("errorCode", FssErrorCodeEnum.PHOENIX_INVALID_PARAM.getCode());
        }

        return result;
    }

    /**
     * @param query
     * @param phoenixConn
     * @param flag
     * @return
     */
    private JSONObject organizeQuerySql(JSONObject query, Connection phoenixConn, int flag) {
        // flag，0-组装数据查询sql，1-组装数据统计sql
        JSONObject result = new JSONObject();
        result.put("errorCode", FssErrorCodeEnum.SUCCESS.getCode());
        List<JSONObject> resList = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        int pageNo = query.getIntValue("page_no");
        int pageSize = query.getIntValue("page_size");

        // 单值查询条件
        JSONObject queryTerm = null;
        if (query.containsKey("query_term")) {
            queryTerm = query.getJSONObject("query_term");
        }
        // 多值查询条件
        JSONObject queryMulti = null;
        if (query.containsKey("query_multi")) {
            queryMulti = query.getJSONObject("query_multi");
        }

        // flag，0-组装数据查询sql，1-组装数据统计sql
        if (flag == 0) {
            sql.append("select * from ").append(tableName).append(" WHERE");
        } else {
            sql.append("select count(1) from ").append(tableName).append(" WHERE");
        }

        // 单值查询条件
        boolean hasQueryTerm = false;
        if (null != queryTerm && !queryTerm.isEmpty()) {
            hasQueryTerm = true;
            for (String key : queryTerm.keySet()) {
                sql.append(" ").append(key + " = ? AND");
            }
            sql.delete(sql.lastIndexOf("AND"), sql.length());
        }

        // 多值查询条件
        boolean hasMulti = false;
        if (null != queryMulti && !queryMulti.isEmpty()) {
            hasMulti = true;
            if (hasQueryTerm) {
                sql.append(" AND ");
            }
            for (String key : queryMulti.keySet()) {
                Object value = queryMulti.get(key);
                if (value instanceof ArrayList) { //
                    sql.append(" ").append(key).append(" IN (");
                    for (Object ele : (ArrayList) value) {
                        sql.append("?,");
                    }
                    sql.delete(sql.lastIndexOf(","), sql.length());
                    sql.append(")");
                }
                sql.append(" AND ");
            }
            sql.delete(sql.lastIndexOf("AND"), sql.length());
        }

        // 设置查询偏移量 from ,size
        if (0 == flag) {
            sql.append(" order by lib_id");
            sql.append(" limit ").append(pageSize);
            sql.append(" offset ").append((pageNo - 1) * pageSize);

        }

        int i = 1;
        ResultSet rs = null;
        try (PreparedStatement stat = phoenixConn.prepareStatement(sql.toString())) {
            // 模糊查询的内容已经拼到sql语句中了
            // 单值查询条件
            if (hasQueryTerm) {
                for (String key : queryTerm.keySet()) {
                    stat.setObject(i, queryTerm.get(key));
                    i++;
                }
            }
            // 多值查询条件
            if (hasMulti) {
                for (String key : queryMulti.keySet()) {
                    Object value = queryMulti.get(key);
                    if (value instanceof ArrayList) {
                        for (Object ele : (ArrayList) value) {
                            stat.setObject(i, ele);
                            i++;
                        }
                    }
                }
            }

            rs = stat.executeQuery();
            if (flag == 0) { // 翻页查询
                while (rs.next()) {
                    JSONObject record = new JSONObject();
                    ResultSetMetaData rsMetaData = rs.getMetaData();
                    int columnCount = rsMetaData.getColumnCount();
                    for (int column = 0; column < columnCount; column++) {
                        String field = rsMetaData.getColumnLabel(column + 1);
                        record.put(field.toLowerCase(), rs.getObject(field));
                    }
                    resList.add(record);
                }
                result.put("data", resList);
            } else { // 首次查询
                while (rs.next()) {
                    int count = rs.getInt(1);
                    result.put("count", count);
                    result.put("total_page", count % pageSize == 0 ? count / pageSize : count / pageSize + 1);
                }
            }
        } catch (Exception e) {
            result.put("errorCode", FssErrorCodeEnum.PHOENIX_INVALID_SQL.getCode());
            L.error("query libconfig error {}", e);
        }finally {
            if (rs != null){
                try {
                    rs.close();
                }catch (Exception e){
                    L.error("query libconfig release resource error ", e);
                }
            }
        }

        return result;
    }

    /**
     * @return 获取单例
     */
    // public static synchronized LibConfigClient getInstance() {
    // if (null == libConfigClient) {
    // libConfigClient = new LibConfigClient();
    // }
    // return libConfigClient;
    // }

    private static class LibConfigSingletonHolder {
        private static LibConfigClient instance = new LibConfigClient();
    }

    public static LibConfigClient getInstance() {
        return LibConfigSingletonHolder.instance;
    }

    /**
     * 初始化kafka
     */
    private static void init() {
        try {
            Properties props = ConfigManager.getProducerProps();
            producer.initWithConfig(props);
            producer.setMsgTypeParam(ConfigManager.getString(VConstants.NOTIFY_TOPIC_MSGTYPE),
                ConfigManager.getString(VConstants.ZOOKEEPER_ADDR), ConfigManager.getInt(VConstants.NOTIFY_PARTITION_NUM),
                ConfigManager.getInt(VConstants.NOTIFY_REPLICATION_NUM));
            // L.info("finished init kafka...");
        } catch (Exception e) {
            L.error("init kafka... error {}", e);
        }
    }

    /**
     * @return 获取表名
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * 发送到kafka
     */
    // 流处理不需要lib_name字段，不需要发通知
    private void sendToKafka(int primaryId) {
        JSONObject notifyMsg = new JSONObject();
        notifyMsg.put("msg_type", ConfigManager.getString(VConstants.NOTIFY_TOPIC_MSGTYPE));
        notifyMsg.put("table_name", tableName);
        notifyMsg.put("primary_id", primaryId);
        notifyMsg.put("reference_id", null);
        // boolean ret = producer.sendData(notifyMsg);
        // L.info("send to kafka return {}", ret);
    }

    public void close() {
        if (producer != null) {
            producer.close();
        }
    }

}

package com.znv.fss.phoenix;

import com.alibaba.fastjson.JSONObject;
import com.znv.fss.common.VConstants;
import com.znv.fss.common.errorcode.FssErrorCodeEnum;
import com.znv.fss.common.utils.FeatureCompUtil;
import com.znv.fss.conf.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static com.znv.fss.common.VConstants.GET_ALARM_EXPORT_DATA_V113;
import static com.znv.fss.common.VConstants.QUERY_FSS_ALARM_V113;


/**
 * Created by Administrator on 2017/7/31.
 */
public final class AlarmSearchClient extends ClientBase {

    // 单例
    // private static AlarmSearchClient alarmSearchClient = null;

    private static final Logger L = LoggerFactory.getLogger(AlarmSearchClient.class);

    private String tableName = ConfigManager.getTableName(VConstants.FSS_AIALRM_V113_TABLE_NAME);
    private static final String OPTIME = "op_time";
    private static final String ALARMTYPE = "alarm_type";
    private static final String LIBID = "lib_id"; // int
    private static final String PERSONID = "person_id";
    private static final String CONFIMTIME = "confirm_time";
    // private static final String CONFIRMBY = "confirm_by";
    private static final int SIMTHRESHOLD = 50;
    private static final int EXPORTSIZE = 10000;

    /**
     * 构造函数
     */
    private AlarmSearchClient() {

    }

    /**
     * 获取单例
     */
    // public static synchronized AlarmSearchClient getInstance() {
    // return new AlarmSearchClient();
    // }

    private static class AlarmSingletonHolder {
        private static AlarmSearchClient instance = new AlarmSearchClient();
    }

    public static AlarmSearchClient getInstance() {
        return AlarmSingletonHolder.instance;
    }

    public JSONObject insert(JSONObject insertData, Connection phoenixConn) {
        JSONObject result = new JSONObject();
        if (null == phoenixConn) {
            result.put("errorCode", FssErrorCodeEnum.PHOENIX_CONN_NULL.getCode());
            L.error("insert error, phoenixConn is null");
        } else if (insertData.containsKey(ALARMTYPE) && null != insertData.getInteger(ALARMTYPE)
            && insertData.containsKey(OPTIME) && !insertData.getString(OPTIME).isEmpty()
            && insertData.containsKey(PERSONID) && !insertData.getString(PERSONID).isEmpty()
            && insertData.containsKey(LIBID) && null != insertData.getInteger(LIBID)) {
            // 获取person_id
            String personIdStr = insertData.getString(PERSONID);
            insertData.put(PERSONID, personIdStr);

            // 修改确认时间
            long currentTime = System.currentTimeMillis();
            Date timeDate = new Date(currentTime);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String confirmTime = sdf.format(timeDate);
            insertData.put(CONFIMTIME, confirmTime);

            result = super.insert(insertData, tableName, phoenixConn);

        } else {
            result.put("errorCode", FssErrorCodeEnum.PHOENIX_INVALID_PARAM.getCode());
        }

        return result;
    }

    public JSONObject query(JSONObject searchData, Connection phoenixConn) throws Exception {
        if (phoenixConn == null) {
            throw new Exception("phoenixConn is null");
        }

        JSONObject result = new JSONObject();
        result.put("errorCode", FssErrorCodeEnum.SUCCESS.getCode());
        List<JSONObject> objList = new ArrayList<>();

        long t1 = System.currentTimeMillis();

        // 首次查询先查询总条数和总页数，翻页查询不需要再次查询总条数和总页数
        int count = searchData.getInteger("count");
        int totalPage = searchData.getInteger("total_page");

        // 每页显示的条数
        if (searchData.containsKey("page_size") && null != searchData.getInteger("page_size")) {
            int pageSize = searchData.getInteger("page_size");
            // 当传入的每页行数为0时默认每页10条
            if (pageSize <= 0) {
                pageSize = 10;
            }
            searchData.put("page_size", pageSize);
        }

        if (count == -1 && totalPage == -1) {
            JSONObject total = getCountAndPage(searchData, phoenixConn);
            count = total.getIntValue("count");
            totalPage = total.getIntValue("total_page");
            // L.info("query alarm ,count: {},total page: {}", count, totalPage);
        }

        // 没有符合条件的记录，直接返回
        if (count == 0) {
            result.put("data", objList);
            result.put("time", System.currentTimeMillis() - t1);
            result.put("count", count);
            result.put("total_page", totalPage);
            result.put("id", QUERY_FSS_ALARM_V113);
        } else {
            StringBuilder sql = new StringBuilder();

            int pageNo = searchData.getIntValue("page_no");
            int pageSize = searchData.getIntValue("page_size");

            JSONObject queryTerm = null;
            if (searchData.containsKey("query_term")) {
                queryTerm = searchData.getJSONObject("query_term");
            }

            // 按图片查询需要用到udf，做特殊处理
            int sim = -1;
            String feature = "";
            boolean isSearchFeature = false;
            if (queryTerm != null && queryTerm.containsKey("feature") && !queryTerm.getString("feature").isEmpty()) {
                isSearchFeature = true;
                feature = queryTerm.getString("feature");
                queryTerm.remove("feature");

                // 添加对相似度阈值的判断
                sim = SIMTHRESHOLD;
                if (null != queryTerm.getInteger("sim") && queryTerm.getIntValue("sim") > SIMTHRESHOLD) {
                    sim = queryTerm.getIntValue("sim");
                }

                queryTerm.remove("sim");
            }

            // [lq-add] 2018-05-21 sensetime points read from hdfs,Normalize/reversalNormalize do in SDK
            FeatureCompUtil fc = new FeatureCompUtil();
            fc.setFeaturePoints(PhoenixClient.getPoints());
            float simThresholdFloat = fc.reversalNormalize(sim / 100.00F);

            // 多值查询条件
            JSONObject queryMulti = null;
            if (searchData.containsKey("query_multi")) {
                queryMulti = searchData.getJSONObject("query_multi");
            }

            // 范围条件
            JSONObject queryRange = null;
            String orderType = "";
            String orderField = "";
            if (searchData.containsKey("query_range")) {
                queryRange = searchData.getJSONObject("query_range");
                orderField = queryRange.getString("order_field");
                orderType = queryRange.getString("order_type");
            }

            // sql.append("select * from ").append(tableName).append(" WHERE");

            // 优化方案
            sql.append("select ");
            // 只返回告警表中需要的字段
            for (String key : schema.keySet()) {
                // 去掉key中的列族信息
                if (key.contains(".")) {
                    key = key.split("\\.")[1];
                }
                sql.append(key)./* append(" as ").append(key). */append(",");
            }

            if (isSearchFeature) {
                // 查询结果添加查询图片与实时图片间的相似度
                sql.append("FeatureComp(rt_feature,?) as sim");
            } else {
                // 去掉多余的逗号
                int idx1 = sql.lastIndexOf(",");
                if (idx1 != -1) {
                    sql.delete(idx1, sql.length());
                }
            }

            sql.append(" from ").append(tableName).append(" WHERE");

            boolean isAddAnd = false;
            // 单值条件
            if (queryTerm != null) {
                boolean isDeleteAnd = false;
                for (Object key : queryTerm.keySet()) {
                    sql.append(" ").append(key.toString() + " = ? AND ");
                    isAddAnd = true;
                    isDeleteAnd = true;
                }
                if (isDeleteAnd) {
                    int index = sql.lastIndexOf("AND");
                    if (index != -1) {
                        sql.delete(index, sql.length());
                    }
                }
            }

            // 多值条件
            if (queryMulti != null) {
                if (isAddAnd && queryMulti.size() != 0) {
                    sql.append(" AND ");
                }
                boolean isDeleteAnd = false;
                for (Object key : queryMulti.keySet()) {
                    Object value = queryMulti.get(key);
                    sql.append(" ").append(key).append(" IN (");
                    if (value instanceof ArrayList) {
                        for (Object ele : (ArrayList) value) {
                            sql.append("?,");
                        }
                        int index = sql.lastIndexOf(",");
                        if (index != -1) {
                            sql.delete(index, sql.length());
                        }
                        sql.append(")");
                    }
                    sql.append(" AND ");

                    isAddAnd = true;
                    isDeleteAnd = true;
                }
                if (isDeleteAnd) {
                    int index = sql.lastIndexOf("AND");
                    if (index != -1) {
                        sql.delete(index, sql.length());
                    }
                }
            }
            if (queryRange.size() != 0) {
                if (isAddAnd) {
                    sql.append(" AND ");
                }
                sql.append(" op_time BETWEEN '").append(queryRange.getString("start_time")).append("' AND '")
                    .append(queryRange.getString("end_time")).append("'");
                isAddAnd = true;
            }

            // 按图片查询,按照比对相似度排序，默认相似度倒排序
            if (isSearchFeature /* !feature.equals("") */) {
                if (isAddAnd) {
                    sql.append(" AND ");
                }
                // sql.append(" FeatureComp(rt_feature,?) >= ").append(sim).append(" ");
                sql.append(" FeatureComp(rt_feature,?) >= ").append(simThresholdFloat).append(" "); // [lq-modify-2018-05-21]

                // 支持按sim 和 告警时间排序
                if ("0".equals(orderField)) {
                    sql.append(" order by FeatureComp(rt_feature,?) ");
                } else {
                    sql.append(" order by op_time ");
                }

            } else if ("0".equals(orderField)) {
                // 不按图片查询，按照时间排序
                sql.append(" order by similarity ");
            } else /* if ("1".equals(orderField)) */ {
                sql.append(" order by op_time ");
            }
            if ("0".equals(orderType)) {
                sql.append(" desc ");
            } else /* if ("1".equals(orderType)) */ {
                sql.append(" asc ");
            }

            sql.append(" limit ").append(pageSize).append(" offset ").append((pageNo - 1) * pageSize);

            // L.info("searchData: {}", sql.toString());
            // System.out.println(sql.toString());

            int i = 1;
            ResultSet rs = null;
            try (PreparedStatement stat = phoenixConn.prepareStatement(sql.toString())) {
                // 按图片查询（返回字段）
                if (isSearchFeature) {
                    byte[] byteFeature = Base64.getDecoder().decode(feature);
                    stat.setObject(i, byteFeature);
                    i++;
                    // stat.setObject(i, byteFeature);
                }
                // 单值条件
                if (queryTerm != null) {
                    for (Object key : queryTerm.keySet()) {
                        stat.setObject(i, queryTerm.get(key));
                        i++;
                    }
                }

                // 多值条件
                if (queryMulti != null) {
                    for (Object key : queryMulti.keySet()) {
                        Object value = queryMulti.get(key);
                        if (value instanceof ArrayList) {
                            for (Object ele : (ArrayList) value) {
                                stat.setObject(i, ele);
                                i++;
                            }
                        }
                    }
                }

                // 按图片查询（查询条件）
                if (isSearchFeature /* !feature.equals("") */) {
                    byte[] byteFeature = Base64.getDecoder().decode(feature);
                    stat.setObject(i, byteFeature);
                    i++;

                    if ("0".equals(orderField)) {
                        stat.setObject(i, byteFeature);
                        i++;
                    }
                }

                rs = stat.executeQuery();

                while (rs.next()) {
                    JSONObject record = new JSONObject();
                    ResultSetMetaData rsMetaData = rs.getMetaData();
                    int columnCount = rsMetaData.getColumnCount();

                    // String personId = "";
                    for (int column = 0; column < columnCount; column++) {
                        String field = rsMetaData.getColumnLabel(column + 1).toLowerCase();
                        record.put(field, rs.getObject(field));
                    }

                    // [lq-add] 2018-05-21 sensetime points read from hdfs,Normalize/reversalNormalize do in SDK
                    if (record.containsKey("sim") && null != record.get("sim")) {
                        float simFlloat = fc.Normalize(Float.parseFloat(record.get("sim").toString()));
                        record.put("sim", simFlloat);
                    }

                    objList.add(record);
                }
                rs.close(); // lq-add
            } catch (Exception e) {
                L.error("query history error,{}", e.getMessage());
                // throw e;
            }finally {
                if (rs != null){
                    try {
                        rs.close();
                    }catch (Exception e){
                        L.error("alarmSearchClient query release resource error: ",e);
                    }
                }
            }

            // if (!objList.isEmpty()) {
            // JSONObject queryInfo = new JSONObject();
            // queryInfo.put("id", VConstants.QUERY_FSS_PERSONLIST_V113);
            // queryInfo.put("page_no", 1);
            // queryInfo.put("page_size", 1);
            // queryInfo.put("total_page", -1);
            // queryInfo.put("count", -1);
            // JSONObject qTerm = new JSONObject();
            // JSONObject qRange = new JSONObject();
            // for (JSONObject object : objList) {
            // qTerm.put("person_id", object.getString("person_id"));
            // qTerm.put(LIBID, object.getInteger(LIBID));
            // qRange.put("start_time", "1970-01-01");
            // qRange.put("end_time", "2030-12-31");
            // // qRange.put("order","1");
            // queryInfo.put("query_term", qTerm);
            // queryInfo.put("query_range_modify", qRange);
            // JSONObject personObj = PersonListClient.getInstance().query(queryInfo, phoenixConn, true);
            // if ("success".equals(FssErrorCodeEnum.getExplanationByCode(personObj.getInteger("errorCode")))) {
            // List<JSONObject> dataList = (List<JSONObject>) personObj.get("data");
            // if (!dataList.isEmpty()) {
            // object.putAll(dataList.get(0));
            // }
            // }
            // }
            // }

            result.put("data", objList);
            result.put("time", System.currentTimeMillis() - t1);
            result.put("total_page", totalPage);
            result.put("count", count);
            result.put("id", QUERY_FSS_ALARM_V113);
        }

        return result;
    }

    private JSONObject getCountAndPage(JSONObject searchData, Connection phoenixConn) throws Exception {
        StringBuilder sql = new StringBuilder();
        JSONObject result = new JSONObject();
        int pageSize = 0;

        // 每页显示的条数
        if (searchData.containsKey("page_size") && null != searchData.getInteger("page_size")) {
            pageSize = searchData.getInteger("page_size");
            // 当传入的每页行数为0时默认每页10条
            if (pageSize <= 0) {
                pageSize = 10;
            }
        }

        // 单值查询条件
        JSONObject queryTerm = null;
        if (searchData.containsKey("query_term")) {
            queryTerm = searchData.getJSONObject("query_term");
        }

        // 按图片查询需要用到udf，做特殊处理
        int sim = -1;
        String feature = "";
        if (queryTerm != null && queryTerm.containsKey("feature")) {
            feature = queryTerm.getString("feature");
            // queryTerm.remove("feature");

            sim = SIMTHRESHOLD;
            if (null != queryTerm.getInteger("sim") && queryTerm.getIntValue("sim") > SIMTHRESHOLD) {
                sim = queryTerm.getIntValue("sim");
            }
            // queryTerm.remove("sim");
        }

        // [lq-add] 2018-05-21 sensetime points read from hdfs,Normalize/reversalNormalize do in SDK
        FeatureCompUtil fc = new FeatureCompUtil();
        fc.setFeaturePoints(PhoenixClient.getPoints());
        float simThresholdFloat = fc.reversalNormalize(sim / 100.00F);

        // 多值查询条件
        JSONObject queryMulti = null;
        if (searchData.containsKey("query_multi")) {
            queryMulti = searchData.getJSONObject("query_multi");
        }

        JSONObject queryRange = null;
        if (searchData.containsKey("query_range")) {
            queryRange = searchData.getJSONObject("query_range");
        }

        sql.append("select count(1) from ").append(tableName).append(" WHERE");

        boolean isAddAnd = false;
        // 单值条件
        if (queryTerm != null) {
            boolean isDeleteAnd = false;
            for (Object key : queryTerm.keySet()) {
                if (!key.toString().equals("sim") && !key.toString().equals("feature")) {
                    sql.append(" ").append(key.toString() + " = ? AND");
                    isAddAnd = true;
                    isDeleteAnd = true;
                }
            }
            if (isDeleteAnd) {
                int index = sql.lastIndexOf("AND");
                if (index != -1) {
                    sql.delete(index, sql.length());
                }
            }
        }

        // 多值条件
        if (queryMulti != null) {
            if (isAddAnd && queryMulti.size() != 0) {
                sql.append(" AND ");
            }
            boolean isDeleteAnd = false;
            for (Object key : queryMulti.keySet()) {
                Object value = queryMulti.get(key);
                sql.append(" ").append(key).append(" IN (");
                if (value instanceof ArrayList) {
                    for (Object ele : (ArrayList) value) {
                        sql.append("?,");
                    }
                    int index = sql.lastIndexOf(",");
                    if (index != -1) {
                        sql.delete(index, sql.length());
                    }
                    sql.append(")");
                }
                sql.append(" AND ");
                isAddAnd = true;
                isDeleteAnd = true;
            }
            if (isDeleteAnd) {
                int index = sql.lastIndexOf("AND");
                if (index != -1) {
                    sql.delete(index, sql.length());
                }
            }
        }

        if (queryRange.size() != 0) {
            if (isAddAnd) {
                sql.append(" AND ");
            }
            sql.append(" op_time BETWEEN '").append(queryRange.getString("start_time")).append("' AND '")
                .append(queryRange.getString("end_time")).append("'");
            isAddAnd = true;
        }

        // 按图片查询
        if (!feature.equals("")) {
            if (isAddAnd) {
                sql.append(" AND ");
            }
            // sql.append(" FeatureComp(rt_feature,?) >= ").append(sim).append(" ");
            sql.append(" FeatureComp(rt_feature,?) >= ").append(simThresholdFloat).append(" "); // [lq-modify-2018-05-21]
        }

        L.info("query: {}", sql.toString());

        int i = 1;
        ResultSet rs = null;
        try (PreparedStatement stat = phoenixConn.prepareStatement(sql.toString())) {
            // 单值条件
            if (queryTerm != null) {
                for (Object key : queryTerm.keySet()) {
                    // 按图片查询做特殊处理
                    if (!key.toString().equals("sim") && !key.toString().equals("feature")) {
                        stat.setObject(i, queryTerm.get(key));
                        i++;
                    }
                }
            }

            // 多值条件
            if (queryMulti != null) {
                for (Object key : queryMulti.keySet()) {
                    Object value = queryMulti.get(key);
                    if (value instanceof ArrayList) {
                        for (Object ele : (ArrayList) value) {
                            stat.setObject(i, ele);
                            i++;
                        }
                    }
                }
            }

            // 按图片查询
            if (!feature.equals("")) {
                stat.setObject(i, Base64.getDecoder().decode(feature));
            }

            rs = stat.executeQuery();
            while (rs.next()) {
                int count = rs.getInt(1);
                result.put("count", count);
                result.put("total_page", count % pageSize == 0 ? count / pageSize : count / pageSize + 1);
            }
        }catch (Exception e){
            L.error("alarmSearchClient getCountAndPage error:", e);

        }finally {
            if (rs != null){
                try {
                    rs.close();
                }catch (Exception e){
                    L.error("alarmSearchClient getCountAndPage release resource error:",e);
                }
            }
        }

        return result;
    }

    public JSONObject getAlarmPicture(JSONObject queryData, Connection phoenixConn) {
        JSONObject result = new JSONObject();
        result.put("errorCode", FssErrorCodeEnum.SUCCESS.getCode());
        if (phoenixConn == null) {
            result.put("errorCode", FssErrorCodeEnum.PHOENIX_CONN_NULL.getCode());
            L.error(String.format("error occur when getAlarmPicture %s data, phoenixConn is null", tableName));
        } else if (queryData.containsKey(ALARMTYPE) && null != queryData.getInteger(ALARMTYPE)
            && queryData.containsKey(LIBID) && null != queryData.getInteger(LIBID) && queryData.containsKey(OPTIME)
            && !queryData.getString(OPTIME).isEmpty() && queryData.containsKey(PERSONID)
            && !queryData.getString(PERSONID).isEmpty()) {
            int alarmType = queryData.getInteger(ALARMTYPE);
            int libId = queryData.getInteger(LIBID);
            String opTime = queryData.getString(OPTIME);
            String personId = queryData.getString(PERSONID);
            String sql = String.format(" select rt_image_data3 from %s where ", tableName);
            sql = sql + String.format(" %s = %d and %s = %d", ALARMTYPE, alarmType, LIBID, libId);
            sql = sql + String.format(" and %s = '%s' and %s = '%s'", OPTIME, opTime, PERSONID, personId);

            // System.out.println("get alarm picture sql :" + sql);

            ResultSet rs = null;
            try (PreparedStatement stat = phoenixConn.prepareStatement(sql)) {
                rs = stat.executeQuery();
                while (rs.next()) {
                    ResultSetMetaData rsMetaData = rs.getMetaData();
                    int columnCount = rsMetaData.getColumnCount();
                    for (int column = 0; column < columnCount; column++) {
                        String field = rsMetaData.getColumnLabel(column + 1);
                        result.put(field.toLowerCase(), rs.getObject(field));
                    }
                }
            } catch (SQLException e) {
                result.put("errorCode", FssErrorCodeEnum.PHOENIX_INVALID_SQL.getCode());
                L.error("error occur when get alarm picture", e);
            }finally {
                if (rs != null){
                    try {
                        rs.close();
                    }catch (Exception e){
                        L.error(" alarmSearchClient getAlarmPicture release resource error:",e);
                    }
                }
            }

        } else {
            result.put("errorCode", FssErrorCodeEnum.PHOENIX_INVALID_PARAM.getCode());
            L.error(" rowkey not only when get alarm picture");

        }
        return result;

    }

    public JSONObject getAlarmExportData(JSONObject searchData, Connection phoenixConn) throws Exception {
        if (phoenixConn == null) {
            throw new Exception("phoenixConn is null");
        }

        JSONObject result = new JSONObject();
        result.put("errorCode", FssErrorCodeEnum.SUCCESS.getCode());
        List<JSONObject> objList = new ArrayList<>();

        long t1 = System.currentTimeMillis();
        int pageSize = EXPORTSIZE;

        StringBuilder sql = new StringBuilder();

        JSONObject queryTerm = null;
        if (searchData.containsKey("query_term")) {
            queryTerm = searchData.getJSONObject("query_term");
        }

        // 按图片查询需要用到udf，做特殊处理
        int sim = -1;
        String feature = "";
        boolean isSearchFeature = false;
        if (queryTerm != null && queryTerm.containsKey("feature") && !queryTerm.getString("feature").isEmpty()) {
            isSearchFeature = true;
            feature = queryTerm.getString("feature");
            queryTerm.remove("feature");

            // 添加对相似度阈值的判断
            sim = SIMTHRESHOLD;
            if (null != queryTerm.getInteger("sim") && queryTerm.getIntValue("sim") > SIMTHRESHOLD) {
                sim = queryTerm.getIntValue("sim");
            }

            queryTerm.remove("sim");
        }

        // [lq-add] 2018-05-21 sensetime points read from hdfs,Normalize/reversalNormalize do in SDK
        FeatureCompUtil fc = new FeatureCompUtil();
        fc.setFeaturePoints(PhoenixClient.getPoints());
        float simThresholdFloat = fc.reversalNormalize(sim / 100.00F);

        // 多值查询条件
        JSONObject queryMulti = null;
        if (searchData.containsKey("query_multi")) {
            queryMulti = searchData.getJSONObject("query_multi");
        }

        // 范围条件
        JSONObject queryRange = null;
        String orderType = "";
        String orderField = "";
        if (searchData.containsKey("query_range")) {
            queryRange = searchData.getJSONObject("query_range");
            orderField = queryRange.getString("order_field");
            orderType = queryRange.getString("order_type");
        }

        // 优化方案
        sql.append("select ");
        // 只返回告警表中需要的字段
        for (String key : exportSchema.keySet()) {
            // 去掉key中的列族信息
            if (key.contains(".")) {
                key = key.split("\\.")[1];
            }
            sql.append(key)./* append(" as ").append(key). */append(",");
        }

        if (isSearchFeature) {
            // 查询结果添加查询图片与实时图片间的相似度
            sql.append("FeatureComp(rt_feature,?) as sim");
        } else {
            // 去掉多余的逗号
            int idx1 = sql.lastIndexOf(",");
            if (idx1 != -1) {
                sql.delete(idx1, sql.length());
            }
        }

        sql.append(" from ").append(tableName).append(" WHERE");

        boolean isAddAnd = false;
        // 单值条件
        if (queryTerm != null) {
            boolean isDeleteAnd = false;
            for (Object key : queryTerm.keySet()) {
                sql.append(" ").append(key.toString() + " = ? AND ");
                isAddAnd = true;
                isDeleteAnd = true;
            }
            if (isDeleteAnd) {
                int index = sql.lastIndexOf("AND");
                if (index != -1) {
                    sql.delete(index, sql.length());
                }
            }
        }

        // 多值条件
        if (queryMulti != null) {
            if (isAddAnd && queryMulti.size() != 0) {
                sql.append(" AND ");
            }
            boolean isDeleteAnd = false;
            for (Object key : queryMulti.keySet()) {
                Object value = queryMulti.get(key);
                sql.append(" ").append(key).append(" IN (");
                if (value instanceof ArrayList) {
                    for (Object ele : (ArrayList) value) {
                        sql.append("?,");
                    }
                    int index = sql.lastIndexOf(",");
                    if (index != -1) {
                        sql.delete(index, sql.length());
                    }
                    sql.append(")");
                }
                sql.append(" AND ");

                isAddAnd = true;
                isDeleteAnd = true;
            }
            if (isDeleteAnd) {
                int index = sql.lastIndexOf("AND");
                if (index != -1) {
                    sql.delete(index, sql.length());
                }
            }
        }
        if (queryRange.size() != 0) {
            if (isAddAnd) {
                sql.append(" AND ");
            }
            sql.append(" op_time BETWEEN '").append(queryRange.getString("start_time")).append("' AND '")
                .append(queryRange.getString("end_time")).append("'");
            isAddAnd = true;
        }

        // 按图片查询,按照比对相似度排序，默认相似度倒排序
        if (isSearchFeature /* !feature.equals("") */) {
            if (isAddAnd) {
                sql.append(" AND ");
            }
            // sql.append(" FeatureComp(rt_feature,?) >= ").append(sim).append(" ");
            sql.append(" FeatureComp(rt_feature,?) >= ").append(simThresholdFloat).append(" "); // [lq-modify-2018-05-21]

            // 支持按sim 和 告警时间排序
            if ("0".equals(orderField)) {
                sql.append(" order by FeatureComp(rt_feature,?) ");
            } else {
                sql.append(" order by op_time ");
            }

        } else if ("0".equals(orderField)) {
            // 不按图片查询，按照时间排序
            sql.append(" order by similarity ");
        } else /* if ("1".equals(orderField)) */ {
            sql.append(" order by op_time ");
        }
        if ("0".equals(orderType)) {
            sql.append(" desc ");
        } else /* if ("1".equals(orderType)) */ {
            sql.append(" asc ");
        }

        sql.append(" limit ").append(pageSize);

        // L.info("searchData: {}", sql.toString());
        // System.out.println(sql.toString());

        int i = 1;
        ResultSet rs = null;
        try (PreparedStatement stat = phoenixConn.prepareStatement(sql.toString())) {
            // 按图片查询（返回字段）
            if (isSearchFeature) {
                byte[] byteFeature = Base64.getDecoder().decode(feature);
                stat.setObject(i, byteFeature);
                i++;
                // stat.setObject(i, byteFeature);
            }
            // 单值条件
            if (queryTerm != null) {
                for (Object key : queryTerm.keySet()) {
                    stat.setObject(i, queryTerm.get(key));
                    i++;
                }
            }

            // 多值条件
            if (queryMulti != null) {
                for (Object key : queryMulti.keySet()) {
                    Object value = queryMulti.get(key);
                    if (value instanceof ArrayList) {
                        for (Object ele : (ArrayList) value) {
                            stat.setObject(i, ele);
                            i++;
                        }
                    }
                }
            }

            // 按图片查询（查询条件）
            if (isSearchFeature /* !feature.equals("") */) {
                byte[] byteFeature = Base64.getDecoder().decode(feature);
                stat.setObject(i, byteFeature);
                i++;

                if ("0".equals(orderField)) {
                    stat.setObject(i, byteFeature);
                    i++;
                }
            }

           rs = stat.executeQuery();

            while (rs.next()) {
                JSONObject record = new JSONObject();
                ResultSetMetaData rsMetaData = rs.getMetaData();
                int columnCount = rsMetaData.getColumnCount();

                // String personId = "";
                for (int column = 0; column < columnCount; column++) {
                    String field = rsMetaData.getColumnLabel(column + 1).toLowerCase();
                    record.put(field, rs.getObject(field));
                }

                // [lq-add] 2018-05-21 sensetime points read from hdfs,Normalize/reversalNormalize do in SDK
                if (record.containsKey("sim") && null != record.get("sim")) {
                    float simFlloat = fc.Normalize(Float.parseFloat(record.get("sim").toString()));
                    record.put("sim", simFlloat);
                }

                objList.add(record);
            }
        } catch (Exception e) {
            L.error("query history error,{}", e.getMessage());
            // throw e;
        }finally {
            if (rs != null){
                try {
                    rs.close();
                }catch (Exception e){
                    L.error("alarmSearchClient getAlarmExportData release resource error:",e);
                }
            }
        }

        result.put("data", objList);
        result.put("time", System.currentTimeMillis() - t1);
        result.put("id", GET_ALARM_EXPORT_DATA_V113);
        return result;
    }

    private static HashMap<String, String> exportSchema = new HashMap();

    {
        exportSchema.put("alarm_type", "unsigned_int not null");
        exportSchema.put("op_time", "VARCHAR not null");
        exportSchema.put("lib_id", "unsigned_int not null");
        exportSchema.put("person_id", "VARCHAR not null");
        // exportSchema.put("attr.enter_time", "VARCHAR");
        exportSchema.put("attr.person_name", "VARCHAR");
        exportSchema.put("attr.camera_id", "VARCHAR");
        exportSchema.put("attr.camera_name", "VARCHAR");
        exportSchema.put("attr.similarity", "unsigned_float");
        exportSchema.put("attr.control_event_id", "VARCHAR");
        exportSchema.put("attr.alarm_duration", "unsigned_long"); // 老人模块需要
        exportSchema.put("attr.big_picture_uuid", "VARCHAR"); // 大图UUID
        exportSchema.put("attr.img_url", "VARCHAR"); // 小图UUID
    }

    // 表结构 注释掉部分查询不需要返回的字段
    private static HashMap<String, String> schema = new HashMap();

    {
        schema.put("alarm_type", "unsigned_int not null");
        schema.put("op_time", "VARCHAR not null");
        schema.put("lib_id", "unsigned_int not null");
        schema.put("person_id", "VARCHAR not null");
        schema.put("attr.enter_time", "VARCHAR");
        schema.put("attr.person_name", "VARCHAR");
        schema.put("attr.camera_id", "VARCHAR");
        schema.put("attr.camera_name", "VARCHAR");
        schema.put("attr.similarity", "unsigned_float");
        schema.put("attr.control_event_id", "VARCHAR");
        schema.put("attr.alarm_duration", "unsigned_long"); // 老人模块需要
        schema.put("attr.big_picture_uuid", "VARCHAR"); // 大图UUID
        schema.put("attr.img_url","VARCHAR"); // 小图UUID
        // schema.put("pics.rt_image_data","VARBINARY");
    }

    /**
     * 获取表结构
     */
    public HashMap<String, String> getAlarmSchema() {
        return schema;
    }
}

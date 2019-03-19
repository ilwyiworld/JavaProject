package com.znv.fss.phoenix;

import com.alibaba.fastjson.JSONObject;
import com.znv.fss.common.VConstants;
import com.znv.fss.common.errorcode.FssErrorCodeEnum;
import com.znv.fss.conf.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2017/7/31.
 */
public final class HistorySearchClient {

    // 单例
    private static HistorySearchClient historySearchClient = null;

    private static final Logger L = LoggerFactory.getLogger(HistorySearchClient.class);

    private String tableName = ConfigManager.getTableName(VConstants.FSS_HISTORY_V113_TABLE_NAME);

    private static final int SIMTHRESHOLD = 50;

    // private static final String FRAMEIDX = "frame_index";
    private static final String TASKIDX = "task_idx";
    private static final String TRACKIDX = "track_idx";
    private static final String RTIMAGEDATA = "rt_image_data";
    private static final String ENTERTIME = "enter_time";
    private static final String CAMERAID = "camera_id";

    /**
     * 构造函数
     */
    private HistorySearchClient() {

    }

    /**
     * 获取单例
     */
    // public static synchronized HistorySearchClient getInstance() {
    // return new HistorySearchClient();
    // }

    private static class HistorySingletonHolder {
        private static HistorySearchClient instance = new HistorySearchClient();
    }

    public static HistorySearchClient getInstance() {
        return HistorySingletonHolder.instance;
    }

    /**
     * @param searchData
     * @param phoenixConn
     * @return
     * @throws Exception
     */
    public JSONObject query(JSONObject searchData,
                            Connection phoenixConn) throws Exception {
        if (phoenixConn == null) {
            throw new Exception("phoenixConn is null");
        }

        JSONObject result = new JSONObject();
        result.put("errorCode", FssErrorCodeEnum.SUCCESS.getCode());
        List<JSONObject> objList = new ArrayList<>();
        HashMap<String, JSONObject> alarm = new HashMap<>();
        JSONObject queryPersonId = new JSONObject();
        List<Object> personIdList = new ArrayList<>();

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
            // L.info("query history ,count: {},total page: {}", count, totalPage);
            // System.out.printf("query history :count = %d, total page = %d\n", count, totalPage);
        }

        // 没有符合条件的记录，直接返回
        if (count == 0) {
            result.put("data", objList);
            result.put("time", System.currentTimeMillis() - t1);
            result.put("count", count);
            result.put("total_page", totalPage);
            result.put("id", VConstants.QUERY_FSS_HISTORY_V113);
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
            String order = "";
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

                if (queryTerm.containsKey("order")) {
                    order = queryTerm.getString("order");
                    queryTerm.remove("order");
                }
            }

            // 多值查询条件
            JSONObject queryMulti = null;
            if (searchData.containsKey("query_multi")) {
                queryMulti = searchData.getJSONObject("query_multi");
            }

            // 范围条件
            JSONObject queryRange = null;
            if (searchData.containsKey("query_range")) {
                queryRange = searchData.getJSONObject("query_range");
            }

            // sql.append("select * from ").append(tableName).append(" WHERE");

            sql.append("select ");
            // 只返回历史表中需要的字段
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
                    sql.append(" ").append(key.toString() + " = ? AND");
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
                sql.append(" enter_time BETWEEN '").append(queryRange.getString("start_time")).append("' AND '")
                    .append(queryRange.getString("end_time")).append("'");
                isAddAnd = true;
            }

            // 按图片查询,若order=1:按照比对相似度排序,order=0:时间倒序排序
            if (isSearchFeature /* !feature.equals("") */) {
                if (isAddAnd) {
                    sql.append(" AND ");
                }
                sql.append(" FeatureComp(rt_feature,?) >= ").append(sim).append(" ");
            }
            if ("1".equals(order)) {
                sql.append(" order by FeatureComp(rt_feature,?) ");
            } else {
                sql.append(" order by enter_time "); // 不按图片查询，按照进入时间排序
            }

            sql.append(" desc limit ").append(pageSize).append(" offset ").append((pageNo - 1) * pageSize);

            // L.info("searchData: {}", sql.toString());
            // System.out.println(sql.toString());

            int i = 1;
            ResultSet rs = null;
            try (PreparedStatement stat = phoenixConn.prepareStatement(sql.toString())) {
                // 按图片查询（查询结果）
                if (isSearchFeature) {
                    byte[] byteFeature = Base64.getDecoder().decode(feature);
                    stat.setObject(i, byteFeature);
                    i++;
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
                    if ("1".equals(order)) {
                        stat.setObject(i, byteFeature);
                        i++;
                    }
                }

                rs = stat.executeQuery();

                while (rs.next()) {
                    JSONObject record = new JSONObject();
                    ResultSetMetaData rsMetaData = rs.getMetaData();
                    int columnCount = rsMetaData.getColumnCount();

                    for (int column = 0; column < columnCount; column++) {
                        String field = rsMetaData.getColumnLabel(column + 1).toLowerCase();
                        record.put(field, rs.getObject(field));
                    }
                    objList.add(record);
                }
            } catch (Exception e) {
                L.error("query history error,{}", e);
                throw e;
            } finally {
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (Exception e) {
                        L.error("query history release resource error", e);
                    }
                }
            }

            // 获取名单库相关信息
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
            // qTerm.put("lib_id", object.getInteger("lib_id"));
            // qRange.put("start_time", "1970-01-01");
            // qRange.put("end_time", "2030-12-31");
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
            result.put("id", VConstants.QUERY_FSS_HISTORY_V113);
        }
        return result;
    }

    /**
     * @param searchData
     * @param phoenixConn
     * @return
     * @throws Exception
     */
    private JSONObject getCountAndPage(JSONObject searchData,
                                       Connection phoenixConn) throws Exception {
        StringBuilder sql = new StringBuilder();
        JSONObject result = new JSONObject();

        // 每页显示的条数
        int pageSize = searchData.getIntValue("page_size");

        // 单值查询条件
        JSONObject queryTerm = null;
        if (searchData.containsKey("query_term")) {
            queryTerm = searchData.getJSONObject("query_term");
        }

        // 按图片查询需要用到udf，做特殊处理
        int sim = -1;
        String feature = "";
        String order = "";
        if (queryTerm != null && queryTerm.containsKey("feature")) {
            feature = queryTerm.getString("feature");
            // queryTerm.remove("feature");

            sim = SIMTHRESHOLD;
            if (null != queryTerm.getInteger("sim") && queryTerm.getIntValue("sim") > SIMTHRESHOLD) {
                sim = queryTerm.getIntValue("sim");
            }
            // queryTerm.remove("sim");

            if (queryTerm.containsKey("order")) {
                order = queryTerm.getString("order");
            }
        }

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
                if (!key.toString().equals("sim") && !key.toString().equals("feature")
                    && !key.toString().equals("order")) {
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
            sql.append(" enter_time BETWEEN '").append(queryRange.getString("start_time")).append("' AND '")
                .append(queryRange.getString("end_time")).append("'");
            isAddAnd = true;
        }

        // 按图片查询
        if (!feature.equals("")) {
            if (isAddAnd) {
                sql.append(" AND ");
            }
            sql.append(" FeatureComp(rt_feature,?) >= ").append(sim).append(" ");
        }

        // L.info("query: {}", sql.toString());

        int i = 1;
        ResultSet rs = null;
        try (PreparedStatement stat = phoenixConn.prepareStatement(sql.toString())) {
            // 单值条件
            if (queryTerm != null) {
                for (Object key : queryTerm.keySet()) {
                    // 按图片查询做特殊处理
                    if (!key.toString().equals("sim") && !key.toString().equals("feature")
                        && !key.toString().equals("order")) {
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
        } catch (SQLException e) {
            result.put("errorCode", FssErrorCodeEnum.PHOENIX_INVALID_SQL.getCode());
            L.error("error occur when getCountAndPage", e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                    L.error("error occur when getCountAndPage release resource ", e);
                }
            }
        }
        return result;
    }

    /**
     * @param queryData
     * @param phoenixConn
     * @return
     */
    public JSONObject getHistoryPicture(JSONObject queryData, Connection phoenixConn) {
        JSONObject result = new JSONObject();
        result.put("errorCode", FssErrorCodeEnum.SUCCESS.getCode());
        if (phoenixConn == null) {
            result.put("errorCode", FssErrorCodeEnum.PHOENIX_CONN_NULL.getCode());
            L.error(String.format("error occur when getHistoryPicture %s data, phoenixConn is null", tableName));
        } else if (queryData.containsKey("uuid") && !queryData.getString("uuid").isEmpty()) {
            String sql = String.format(" select rt_image_data3 from %s where", tableName);
            String historyUuid = queryData.getString("uuid");
            String enterTime = queryData.getString("enter_time");
            sql = sql + String.format(" enter_time = '%s' and uuid = '%s'", enterTime, historyUuid);

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
                L.error("error occur when get history picture", e);
            } finally {
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (Exception e) {
                        L.error("error occur when get history picture release resource ", e);
                    }
                }
            }

        } else {
            result.put("errorCode", FssErrorCodeEnum.PHOENIX_INVALID_PARAM.getCode());
            L.error(" rowkey not only when get history picture");

        }
        return result;

    }

    /**
     * @param queryData，超级检索，时间按照between过滤，时间加1秒
     * @param phoenixConn
     * @return
     */
    public JSONObject getHistoryPictureForSuperSearch(JSONObject queryData, Connection phoenixConn) {
        JSONObject result = new JSONObject();
        String superSearchTableName = "FSS_DEVELOP_410.FSS_HISTORY_V113_10000W"; // 92集群
        result.put("errorCode", FssErrorCodeEnum.SUCCESS.getCode());
        if (phoenixConn == null) {
            result.put("errorCode", FssErrorCodeEnum.PHOENIX_CONN_NULL.getCode());
            L.error(String.format("error occur when getHistoryPicture %s data, phoenixConn is null", superSearchTableName));
        } else if (queryData.containsKey("uuid") && !queryData.getString("uuid").isEmpty()) {
            String sql = String.format(" select rt_image_data3 from %s where", superSearchTableName);
            String historyUuid = queryData.getString("uuid");
            try {

                String enterTimeStart = queryData.getString("enter_time");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date searchTime = sdf.parse(enterTimeStart);
                long searchTimeLong = searchTime.getTime() + 1000l; // 结束时间加一秒
                Date endTime = new Date(searchTimeLong);
                String enterTimeEnd = sdf.format(endTime);

                sql += String.format(" uuid = '%s'", historyUuid);
                sql += String.format(" and enter_time between '%s' and '%s'", enterTimeStart, enterTimeEnd);

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
                    L.error("error occur when get history picture for super search", e);
                } finally {
                    if (rs != null) {
                        try {
                            rs.close();
                        } catch (Exception e) {
                            L.error("error occur when get history picture for super search release resource ", e);
                        }
                    }
                }
            } catch (ParseException e) {
                result.put("errorCode", FssErrorCodeEnum.PHOENIX_INVALID_PARAM.getCode());
                L.error("error occur when get history picture for super search when rewrite enter_time", e);
            }

        } else {
            result.put("errorCode", FssErrorCodeEnum.PHOENIX_INVALID_PARAM.getCode());
            L.error(" rowkey not only when get history picture for super search");

        }
        return result;
    }

    /**
     * @param queryData
     * @param phoenixConn
     * @return
     */
    // 通过 camera_id（string) + enter_time (string) + task_idx(string) + track_idx(string) 获取图片
    public JSONObject getHistoryPictureByIndex(JSONObject queryData, Connection phoenixConn) {
        JSONObject result = new JSONObject();
        result.put("errorCode", FssErrorCodeEnum.SUCCESS.getCode());
        if (phoenixConn == null) {
            result.put("errorCode", FssErrorCodeEnum.PHOENIX_CONN_NULL.getCode());
            L.error(String.format("error occur when getHistoryPicture %s data, phoenixConn is null", tableName));
        } else if (queryData.containsKey(CAMERAID) && !queryData.getString(CAMERAID).isEmpty()
            && queryData.containsKey(ENTERTIME) && !queryData.getString(ENTERTIME).isEmpty()
            && queryData.containsKey(TASKIDX) && !queryData.getString(TASKIDX).isEmpty()
            && queryData.containsKey(TRACKIDX) && !queryData.getString(TRACKIDX).isEmpty()) {
            // int frameIdx = queryData.getInteger(FRAMEIDX);
            String cameraId = queryData.getString(CAMERAID);
            String enterTime = queryData.getString(ENTERTIME);
            String taskIdx = queryData.getString(TASKIDX);
            String trackIdx = queryData.getString(TRACKIDX);

            StringBuffer strBuffer = new StringBuffer();
            strBuffer.append("select ").append(RTIMAGEDATA).append(" from ").append(tableName).append(" where ")
                .append(ENTERTIME).append("='").append(enterTime).append("' and ").append(CAMERAID).append(" ='")
                .append(cameraId).append("' and ").append(TASKIDX).append("='").append(taskIdx).append("' and ")
                .append(TRACKIDX).append(" ='").append(trackIdx).append("'");

            // System.out.println("getHistoryPictureByIndex sql : " +strBuffer.toString());

            ResultSet rs = null;
            try (PreparedStatement stat = phoenixConn.prepareStatement(strBuffer.toString())) {
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
                L.error("error occur when get history picture by index ", e);
            } finally {
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (Exception e) {
                        L.error("error occur when get history picture by index release resource  ", e);
                    }
                }
            }

        } else {
            result.put("errorCode", FssErrorCodeEnum.PHOENIX_INVALID_PARAM.getCode());
            L.error(" rowkey not only when get history picture by index ");

        }
        return result;
    }

    // 表结构 注释掉部分查询不需要返回的字段
    private static HashMap<String, String> schema = new HashMap();
    {
        schema.put("uuid", "VARCHAR not null");
        schema.put("enter_time", "VARCHAR not null");
        schema.put("attr.person_id", "VARCHAR");
        schema.put("attr.person_name", "VARCHAR");
        schema.put("attr.camera_id", "VARCHAR");
        schema.put("attr.camera_name", "VARCHAR");
        schema.put("attr.similarity", "unsigned_float");
        schema.put("attr.lib_id", "unsigned_int");
        schema.put("attr.is_alarm", "VARCHAR");
        schema.put("attr.control_event_id", "VARCHAR");
        schema.put("attr.big_picture_uuid", "VARCHAR"); // 大图UUID
        schema.put("attr.left_pos", "int"); // 人脸框左上角横坐标
        schema.put("attr.top", "int"); // 人脸框左上角纵坐标
        schema.put("attr.right_pos", "int"); // 人脸框右下角横坐标
        schema.put("attr.bottom", "int"); // 人脸框右下角纵坐标
        schema.put("attr.img_width", "unsigned_int"); // 人脸图像宽度
        schema.put("attr.img_height", "unsigned_int"); // 人脸图像高度
        schema.put("attr.img_url","VARCHAR"); // 小图UUID
        // schema.put("pic.rt_image_data","VARBINARY");
    }

    /**
     * 获取表结构
     */
    public HashMap<String, String> getSchema() {
        return schema;
    }

}

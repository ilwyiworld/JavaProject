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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * Created by Administrator on 2017/7/25.
 */
public final class CameraLibClient extends ClientBase {
    private static final Logger L = LoggerFactory.getLogger(CameraLibClient.class);
    private String tableName = ConfigManager.getTableName(VConstants.FSS_CAMERA_LIB_V113_TABLE_NAME);
    private int cameraMaxNum = ConfigManager.getInt(VConstants.FSS_CAMERA_MAX_NUM);
    private static final String CAMERAID = "camera_id";
    private static final String LIBID = "lib_id";
    private static final String CAMERANAME = "camera_name";
    private static final String LIBNAME = "lib_name";
    private static final String CONLST = "control_start_time";
    private static final String CONLET = "control_end_time";
    private static CameraLibClient cameraLibClient = null;
    private static ProducerBase producer = new ProducerBase();
    private static final String ORIGINALCAMERA = "original_camera_id";

    static {
        init();
    }

    /**
     * 构造函数
     */
    private CameraLibClient() {
    }

    // public static synchronized CameraLibClient getInstance() {
    // return new CameraLibClient();
    // }

    /**
     * 获取单例
     */
    private static class CameraLibSingletonHolder {
        private static CameraLibClient instance = new CameraLibClient();
    }

    public static CameraLibClient getInstance() {
        return CameraLibSingletonHolder.instance;
    }

    public JSONObject deleteCameraLib(JSONObject data, Connection phoenixConn) {
        JSONObject result = new JSONObject();
        int libId = -1;
        if (phoenixConn == null) {
            result.put("errorCode", FssErrorCodeEnum.PHOENIX_CONN_NULL.getCode());
            L.error("insert error, phoenixConn is null");
        } else if (data.containsKey(LIBID) && null != data.getInteger(LIBID)) {
            // 按照lib_id删除布控表信息
            libId = data.getInteger(LIBID);
            String sql = String.format(" delete from %s where %s = %d", tableName, LIBID, libId);
            try {
                super.executeSql(sql, phoenixConn);
                result.put("errorCode", FssErrorCodeEnum.SUCCESS.getCode());
            } catch (Exception e) {
                result.put("errorCode", FssErrorCodeEnum.PHOENIX_INVALID_SQL.getCode());
            }

            // 发生真正的删除操作需要通知kafka
            if ("success".equals(FssErrorCodeEnum.getExplanationByCode(result.getInteger("errorCode")))) {
                sendToKafka(libId);
            }
        }

        return result;
    }

    /**
     * @param phoenixConn
     * @return
     */
    public JSONObject getCameraLibAll(Connection phoenixConn) {
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
     * @param data        cameraid、cameraname、libid、libname必填，CST、CET
     * @param phoenixConn
     * @return
     */
    // 首先需要判断整条信息是否发生修改，存在不做任何修改
    // 任何字段发生修改，则刷写数据
    public JSONObject insert(JSONObject data, Connection phoenixConn) {
        JSONObject result = new JSONObject();
        result.put("errorCode", FssErrorCodeEnum.SUCCESS.getCode());
        if (phoenixConn == null) {
            result.put("errorCode", FssErrorCodeEnum.PHOENIX_CONN_NULL.getCode());
            L.error("insert error, phoenixConn is null");
        } else {
            String insertCameraId = "";
            int insertLibId = 0;
            // JSONObject configRes = new JSONObject();
            // if (data.containsKey(CAMERAID) && data.containsKey(LIBID) && data.containsKey(CAMERANAME)
            // && data.containsKey(LIBNAME) && null != data.getInteger(CAMERAID) && null != data.getInteger(LIBID)
            // && null != data.getString(CAMERAID) && !"".equals(data.getString(CAMERANAME))
            // && null != data.getString(LIBNAME) && !"".equals(data.getString(LIBNAME))) {

            // 布控表与配置表分开操作
            if (data.containsKey(CAMERAID) && data.containsKey(CAMERANAME) && !data.getString(CAMERAID).isEmpty()
                    && !data.getString(CAMERANAME).isEmpty()) {
                // 判断输入的CAMERAID,LIBID,CAMERANAME是否正确
                StringBuilder sql = new StringBuilder();
                insertCameraId = data.getString(CAMERAID);
                insertLibId = data.getIntValue(LIBID);
                sql.append("select distinct lib_id  from ").append(tableName).append(" where ").append(CAMERAID)
                        .append(" = '").append(insertCameraId).append("'");
                List<Integer> resList = new ArrayList<Integer>();
                ResultSet rs = null;
                try (PreparedStatement stat = phoenixConn.prepareStatement(sql.toString())) {
                    rs = stat.executeQuery();
                    while (rs.next()) {
                        resList.add(rs.getInt(LIBID));
                    }
                    // 单个摄像头下最多配5个子库
                    // 当布控表中的已经配置了5个子库时需要判断新增加的子库是否已经存在
                    // 如果存在则修改，不存在则提示alarm
                    if (resList.size() > cameraMaxNum) {
                        result.put("errorCode", FssErrorCodeEnum.PHOENIX_TOO_MANY_LIBS.getCode());
                        L.info("phoenix cameralib too many cameras,error occur when insert {}", tableName);
                    } else if (resList.size() == cameraMaxNum && !resList.contains(insertLibId)) {
                        result.put("errorCode", FssErrorCodeEnum.PHOENIX_TOO_MANY_LIBS.getCode());
                        L.info("phoenix cameralib too many cameras,error occur when insert {}", tableName);
                    } else {
                        // 数据插入到布控表
                        super.insert(organizeCameraInsertData(data), tableName, phoenixConn);
                        // 数据插入到配置表
                        // configRes = LibConfigClient.getInstance().insert(organizeConfigInsetData(data), phoenixConn);
                    }
                } catch (SQLException e) {
                    result.put("errorCode", FssErrorCodeEnum.PHOENIX_INVALID_SQL.getCode());
                    L.error("error occur when insert data for {} {}", tableName, e);
                }finally {
                    if (rs != null){
                        try {
                            rs.close();
                        }catch (Exception e){
                            L.error("error occur when insert data release resource ", e);
                        }
                    }
                }
            } else {
                result.put("errorCode", FssErrorCodeEnum.PHOENIX_INVALID_PARAM.getCode());
            }

            // 发生“增”“改”操作，则通知kafka
            if ("success".equals(FssErrorCodeEnum.getExplanationByCode(result.getInteger("errorCode")))) {
                sendToKafka(insertCameraId, insertLibId);
            }
            // 布控表与配置表分开操作
            // if ("success".equals(FssErrorCodeEnum.getExplanationByCode(result.getInteger("errorCode")))
            // && "success".equals(FssErrorCodeEnum.getExplanationByCode(configRes.getInteger("errorCode")))) {
            // sendToKafka(insertCameraId, insertLibId);
            // }

        }

        return result;

    }

    public JSONObject upsert(JSONObject data, Connection phoenixConn) {
        JSONObject result = new JSONObject();

        // 先插入新的数据，再判断是否修改了camera_id
        if (data.containsKey("data") && !data.getJSONObject("data").isEmpty()) {
            JSONObject deleteData = data.getJSONObject("data");
            result = insert(deleteData, phoenixConn);

            if (data.containsKey(ORIGINALCAMERA) && !data.getString(ORIGINALCAMERA).isEmpty()) {
                String oldCameraId = data.getString(ORIGINALCAMERA);
                String newCameraId = deleteData.getString(CAMERAID);
                if (!newCameraId.equals(oldCameraId)) {
                    // 修改了camera_id，则先删除原camera_id对应的记录
                    deleteData.put(CAMERAID, oldCameraId);
                    result = delete(deleteData, phoenixConn);
                }
            }

        } else {
            result.put("errorCode", FssErrorCodeEnum.PHOENIX_INVALID_PARAM.getCode());
        }
        return result;
    }

    /**
     * @param data        单条删除，data中字段：cameraid,libid,必填
     * @param phoenixConn
     * @return
     */
    public JSONObject delete(JSONObject data, Connection phoenixConn) {
        JSONObject result = new JSONObject();
        result.put("errorCode", FssErrorCodeEnum.SUCCESS.getCode());
        String sql = "";
        String deleteCameraId = "";
        int deleteLibId = 0;

        if (data.containsKey(CAMERAID) && data.containsKey(LIBID) && !data.getString(CAMERAID).isEmpty()
                && null != data.getInteger(LIBID)) {
            // 判断输入的主键是否正确
            deleteCameraId = data.getString(CAMERAID);
            deleteLibId = data.getIntValue(LIBID);
            sql = String.format("delete from %s where ( %s = '%s' ) and ( %s = %d )", tableName, CAMERAID,
                    deleteCameraId, LIBID, deleteLibId);

            try {
                super.executeSql(sql, phoenixConn);

            } catch (Exception e) {
                result.put("errorCode", FssErrorCodeEnum.PHOENIX_INVALID_SQL.getCode());
                L.error("error occur when delete data from {} ", tableName, e);
            }

            // 发生“删”操作则通知kafka
            if ("success".equals(FssErrorCodeEnum.getExplanationByCode(result.getInteger("errorCode")))) {
                sendToKafka(deleteCameraId, deleteLibId);
            }
        } else {
            result.put("errorCode", FssErrorCodeEnum.PHOENIX_INVALID_PARAM.getCode());
        }

        return result;

    }

    /**
     * @param query       查询条件中不能有libname
     * @param phoenixConn
     * @return
     */
    public JSONObject query(JSONObject query, Connection phoenixConn) {
        JSONObject result = new JSONObject();
        result.put("errorCode", FssErrorCodeEnum.SUCCESS.getCode());
        if (phoenixConn == null) {
            result.put("errorCode", FssErrorCodeEnum.PHOENIX_CONN_NULL.getCode());
            L.error("error occur when querying cameralib data, phoenixConn is null");
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
                    result.put("id", VConstants.QUERY_FSS_CAMERA_LIB_V113);
                } else {
                    // 查询结果中有值
                    result = organizeQuerySql(query, phoenixConn, 0);
                    result.put("time", System.currentTimeMillis() - startTime);
                    result.put("count", count);
                    result.put("total_page", totalPage);
                    result.put("id", VConstants.QUERY_FSS_CAMERA_LIB_V113);
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
    /*
     * 组查询条件，并查询配置表拼完整的查询结果记录
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
        // like查询条件，字符串模糊匹配
        JSONObject queryLike = null;
        if (query.containsKey("query_like")) {
            queryLike = query.getJSONObject("query_like");
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

        // like查询条件 模糊匹配
        if (queryLike != null && !queryLike.isEmpty()) {
            if (hasQueryTerm || hasMulti) {
                sql.append(" AND ");
            }
            for (String key : queryLike.keySet()) {
                sql.append(" ").append(key).append(" like '%").append(queryLike.getString(key)).append("%' AND");
            }
            sql.delete(sql.lastIndexOf("AND"), sql.length());
        }

        // 设置查询偏移量 from ,size
        if (0 == flag) {
            sql.append(" limit ").append(pageSize);
            sql.append(" offset ").append((pageNo - 1) * pageSize);

        }
        // L.info("query: {}", sql.toString());

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
                    // 获取lib_id去配置表查询lib_name
                    int libId = record.getIntValue(LIBID);
                    // // 查询名单库的同时可能发生修改配置表操作，导致lib_name发生变化，所以每次获取的结果都要查询lib_name
                    // String libName = LibConfigClient.getInstance().queryLibName(libId,
                    // phoenixConn).getString(LIBNAME);
                    // record.put(LIBNAME, libName);
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
            L.error("query cameralib error ", e);
        }finally {
            if (rs != null){
                try {
                    rs.close();
                }catch (Exception e){
                    L.error("query cameralib release resource error ", e);
                }

            }
        }

        return result;

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
            L.error("init kafka... error ", e);
        }
    }

    /**
     * 发送到kafka
     */
    private void sendToKafka(int primaryId) {
        JSONObject notifyMsg = new JSONObject();
        notifyMsg.put("msg_type", ConfigManager.getString(VConstants.NOTIFY_TOPIC_MSGTYPE));
        notifyMsg.put("table_name", tableName);
        notifyMsg.put("primary_id", primaryId);
        notifyMsg.put("reference_id", null);
        long currentTime = System.currentTimeMillis();
        Date timeDate = new Date(currentTime);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timeStr = sdf.format(timeDate);
        notifyMsg.put("send_time", timeStr);
        boolean ret = producer.sendData(notifyMsg);
        L.info("send to kafka return {}", ret);
        System.out.println("CameraLibClient-ret:" + ret+",send_time:"+timeStr);
    }

    private void sendToKafka(String primaryId, int referenceId) {
        JSONObject notifyMsg = new JSONObject();
        notifyMsg.put("msg_type", ConfigManager.getString(VConstants.NOTIFY_TOPIC_MSGTYPE));
        notifyMsg.put("table_name", tableName);
        notifyMsg.put("primary_id", primaryId);
        notifyMsg.put("reference_id", referenceId);
        long currentTime = System.currentTimeMillis();
        Date timeDate = new Date(currentTime);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timeStr = sdf.format(timeDate);
        notifyMsg.put("send_time", timeStr);
        boolean ret = producer.sendData(notifyMsg);
        L.info("send to kafka return {}", ret);
        System.out.println("CameraLibClient-ret:" + ret+",send_time:"+timeStr);
    }

    private JSONObject organizeCameraInsertData(JSONObject input) {
        JSONObject output = new JSONObject();
        output.put(CAMERAID, input.getString(CAMERAID));
        output.put(CAMERANAME, input.getString(CAMERANAME));
        output.put(LIBID, input.getIntValue(LIBID));
        output.put(CONLST, input.getString(CONLST));
        output.put(CONLET, input.getString(CONLET));
        return output;
    }

    private JSONObject organizeConfigInsetData(JSONObject input) {
        JSONObject output = new JSONObject();
        output.put(LIBID, input.getIntValue(LIBID));
        output.put(LIBNAME, input.getString(LIBNAME));
        return output;
    }

    // 判断数组中是否存在指定元素
    public static boolean contain(List<Integer> inputList, int targetValue) {
        boolean isContains = false;
        for (int value : inputList) {
            if (targetValue == value) {
                isContains = true;
            }
        }
        return isContains;
    }

    public void close() {
        if (producer != null) {
            producer.close();
        }
    }

}

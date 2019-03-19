package com.znv.fss.phoenix;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.znv.fss.common.utils.PropertiesUtil;
import com.znv.fss.common.VConstants;
import com.znv.fss.common.errorcode.FssErrorCodeEnum;
import com.znv.fss.conf.ConfigManager;
import com.znv.fss.utils.ConnectionPool;
import com.znv.kafka.ProducerBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ct on 2016-12-22.
 */
public class PhoenixClient {

    private static final Logger L = LoggerFactory.getLogger(PhoenixClient.class);

    // PHOENIX url
    // private String url = "";

    // 连接池
    private ConnectionPool connPool = null;
    private static ProducerBase producer = new ProducerBase();
    private static Map<String, float[]> points = new ConcurrentHashMap<String, float[]>(2); // [lq-add] 2018-05-21

    /**
     * @param url 构造方法
     * @param hdfsFilPath
     * @throws Exception 初始化异常全部抛出
     */
    public PhoenixClient(String url, String hdfsFilPath) throws Exception {
        // 加载配置文件
        ConfigManager cm = new ConfigManager();
        cm.init(hdfsFilPath);
        cm.producerInit(hdfsFilPath);

        // 创建连接池
        connPool = new ConnectionPool(ConfigManager.getString(VConstants.PHOENIX_DRIVER), url, "", "");
        connPool.createPool();

        // 初始化kafka
        Properties props = ConfigManager.getProducerProps();
        producer.initWithConfig(props);
        producer.setMsgTypeParam(ConfigManager.getString(VConstants.NOTIFY_TOPIC_MSGTYPE),
            ConfigManager.getString(VConstants.ZOOKEEPER_ADDR), ConfigManager.getInt(VConstants.NOTIFY_PARTITION_NUM),
            ConfigManager.getInt(VConstants.NOTIFY_REPLICATION_NUM));

        // [lq-add] 2018-05-21
        Properties pop = cm.getProps();
        points = PropertiesUtil.getFeaturePoints(pop);

    }

    /**
     * @return
     */
    // [lq-add] 2018-05-21 sensetime points read from hdfs
    public static Map<String, float[]> getPoints() {
        return points;
    }


    /**
     * @return 获取kafka通知producer, 通知kafka统一从这里调用
     */
    public static ProducerBase getProducer() {
        return producer;
    }

    /**
     * @param data
     * @return
     */
    public JSONObject getPicture(JSONObject data) {
        JSONObject result = new JSONObject();
        Connection conn = null;
        String id = data.getString("id");
        String tableName = data.getString("table_name");
        try {
            conn = connPool.getConnection();
            if (VConstants.QUERY_FSS_PERSONLIST_V113.equals(id)
                && tableName.equalsIgnoreCase(ConfigManager.getString(VConstants.FSS_PERSONLIST_V113_TABLE_NAME))) {
                // 获取名单库图片
                result = PersonListClient.getInstance().getPersonPicture(data, conn);
            } else if (VConstants.GET_PERSON_CARD_PICTURE_V113.equals(id)
                && tableName.equalsIgnoreCase(ConfigManager.getString(VConstants.FSS_PERSONLIST_V113_TABLE_NAME))) {
                // 获取名单库身份证图片
                result = PersonListClient.getInstance().getCardPicture(data, conn);
            } else if (VConstants.QUERY_FSS_HISTORY_V113.equalsIgnoreCase(id)
                && tableName.equalsIgnoreCase(ConfigManager.getString(VConstants.FSS_HISTORY_V113_TABLE_NAME))) {
                // 获取历史表图片
                result = HistorySearchClient.getInstance().getHistoryPicture(data, conn);
            } else if (VConstants.QUERY_FSS_ALARM_V113.equals(id)
                && tableName.equalsIgnoreCase(ConfigManager.getString(VConstants.FSS_AIALRM_V113_TABLE_NAME))) {
                // 获取告警表图片
                result = AlarmSearchClient.getInstance().getAlarmPicture(data, conn);
            } else if (VConstants.QUERY_FSS_HISTORY_BY_INDEX_V113.equals(id)
                && tableName.equalsIgnoreCase(ConfigManager.getString(VConstants.FSS_HISTORY_V113_TABLE_NAME))) {
                // 按照index获取历史表图片
                result = HistorySearchClient.getInstance().getHistoryPictureByIndex(data, conn);
            } else if (VConstants.GET_HISTORY_SUPER_SEARCH_PICTURE_V113.equalsIgnoreCase(id)) {
                // 超级检索获取图片
                result = HistorySearchClient.getInstance().getHistoryPictureForSuperSearch(data, conn);
            } else {
                result.put("errorCode", FssErrorCodeEnum.PHOENIX_TABLE_NOT_EXIST.getCode());
                L.error("getPicture error,table name: {} does not exist", tableName);
            }
        } catch (SQLException e) {
            result.put("errorCode", FssErrorCodeEnum.PHOENIX_INVALID_SQL.getCode());
            L.error("get connPool failed ", e);
        } finally {
            if (null != conn) {
                // 用完之后释放连接
                connPool.returnConnection(conn);
            }
        }
        return result;

    }

    /**
     * @param data
     * @return
     */
    public JSONObject deleteLibId(JSONObject data) {
        JSONObject result = new JSONObject();
        Connection conn = null;
        String id = data.getString("id");
        try {
            conn = connPool.getConnection();
            if (id.equals(VConstants.QUERY_FSS_CAMERA_LIB_V113)) {
                result = CameraLibClient.getInstance().deleteCameraLib(data, conn);
            } else {
                result.put("errorCode", FssErrorCodeEnum.PHOENIX_TABLE_NOT_EXIST.getCode());
                L.error("deleteLibId error,table  does not exist");
            }
        } catch (SQLException e) {
            result.put("errorCode", FssErrorCodeEnum.PHOENIX_INVALID_SQL.getCode());
            L.error("get connPool failed ", e);
        } finally {
            if (null != conn) {
                // 用完之后释放连接
                connPool.returnConnection(conn);
            }
        }
        return result;
    }

    /**
     * @param data
     * @return
     */
    public JSONObject count(JSONObject data) {
        JSONObject result = new JSONObject();
        Connection conn = null;
        String tableName = data.getString("table_name");
        String id = data.getString("id");
        JSONObject countData = data.getJSONObject("data");
        try {
            conn = connPool.getConnection();
            if (VConstants.QUERY_FSS_PERSONLIST_V113.equals(id)
                && tableName.equalsIgnoreCase(ConfigManager.getString(VConstants.FSS_PERSONLIST_V113_TABLE_NAME))) {
                // 统计lib_id下的名单数
                result = PersonListClient.getInstance().getPersonListCount(countData, conn);
            } else {
                result.put("errorCode", FssErrorCodeEnum.PHOENIX_TABLE_NOT_EXIST.getCode());
                L.error("insert error,table name: {} does not exist", tableName);
            }
        } catch (SQLException e) {
            result.put("errorCode", FssErrorCodeEnum.PHOENIX_INVALID_SQL.getCode());
            L.error("get connPool failed ", e);
        } finally {
            if (null != conn) {
                // 用完之后释放连接
                connPool.returnConnection(conn);
            }
        }
        return result;
    }

    /**
     * @param data
     * @return
     */
    public JSONObject getAll(JSONObject data) {
        JSONObject result = new JSONObject();
        Connection conn = null;
        String tableName = data.getString("table_name");
        String id = data.getString("id");
        try {
            conn = connPool.getConnection();
            if (VConstants.QUERY_FSS_CAMERA_LIB_V113.equals(id)
                && tableName.equalsIgnoreCase(ConfigManager.getString(VConstants.FSS_CAMERA_LIB_V113_TABLE_NAME))) {
                result = CameraLibClient.getInstance().getCameraLibAll(conn);
            } else if (VConstants.QUERY_FSS_LIB_CONFIG_V113.equals(id)
                && tableName.equalsIgnoreCase(ConfigManager.getString(VConstants.FSS_LIB_CONFIG_V113_TABLE_NAME))) {
                result = LibConfigClient.getInstance().getLibConfigAll(conn);
            } else {
                result.put("errorCode", FssErrorCodeEnum.PHOENIX_TABLE_NOT_EXIST.getCode());
                L.error("insert error,table name: {} does not exist", tableName);
            }

        } catch (SQLException e) {
            result.put("errorCode", FssErrorCodeEnum.PHOENIX_INVALID_SQL.getCode());
            L.error("get connPool failed ", e);
        } finally {
            if (null != conn) {
                // 用完之后释放连接
                connPool.returnConnection(conn);
            }
        }
        return result;
    }

    /**
     * @param insertData
     * @return
     */
    public JSONObject searchPersonList(JSONObject insertData) {
        JSONObject result = new JSONObject();
        Connection conn = null;
        String tableName = insertData.getString("table_name");
        String id = insertData.getString("id");
        JSONArray data = insertData.getJSONArray("data");
        try {
            conn = connPool.getConnection();
            if (VConstants.QUERY_FSS_PERSONLIST_V113.equals(id)
                && tableName.equalsIgnoreCase(ConfigManager.getString(VConstants.FSS_PERSONLIST_V113_TABLE_NAME))) {
                // 按照lib_id + person_id 查询名单库所有信息
                result = PersonListClient.getInstance().getPersonInfo(data, conn);
            } else {
                result.put("errorCode", FssErrorCodeEnum.PHOENIX_TABLE_NOT_EXIST.getCode());
                L.error("search error,table name: {} does not exist", tableName);
            }

        } catch (SQLException e) {
            result.put("errorCode", FssErrorCodeEnum.PHOENIX_INVALID_SQL.getCode());
            L.error("get connPool failed ", e);
        } finally {
            if (null != conn) {
                // 用完之后释放连接
                connPool.returnConnection(conn);
            }
        }
        return result;

    }

    /**
     * @param insertData 数据信息 添加数据
     * @return JSONObject
     */
    public JSONObject insert(JSONObject insertData) {
        JSONObject result = new JSONObject();
        Connection conn = null;
        String tableName = insertData.getString("table_name");
        String id = insertData.getString("id");
        JSONObject data = insertData.getJSONObject("data");
        try {
            conn = connPool.getConnection();
            if (VConstants.QUERY_FSS_PERSONLIST_V113.equals(id)
                && tableName.equalsIgnoreCase(ConfigManager.getString(VConstants.FSS_PERSONLIST_V113_TABLE_NAME))) {
                // 单条数据写入名单库 or 批量新增
                result = PersonListClient.getInstance().insert(data, conn);
            } else if (VConstants.QUERY_FSS_CAMERA_LIB_V113.equals(id)
                && tableName.equalsIgnoreCase(ConfigManager.getString(VConstants.FSS_CAMERA_LIB_V113_TABLE_NAME))) {
                // 数据写入布控表
                result = CameraLibClient.getInstance().insert(data, conn);
            } else if (VConstants.QUERY_FSS_REALTIONSHIP_V113.equals(id)
                && tableName.equalsIgnoreCase(ConfigManager.getString(VConstants.FSS_RELATIONSHIP_V113_TABLE_NAME))) {
                // 数据写入人员关系表
                result = RelationShipClient.getInstance().insert(data, conn);
            } else if (VConstants.QUERY_FSS_ALARM_V113.equals(id)
                && tableName.equalsIgnoreCase(ConfigManager.getString(VConstants.FSS_AIALRM_V113_TABLE_NAME))) {
                // 数据写入告警表（告警确认）
                result = AlarmSearchClient.getInstance().insert(data, conn);
            } else if (VConstants.QUERY_FSS_LIB_CONFIG_V113.equals(id)
                && tableName.equalsIgnoreCase(ConfigManager.getString(VConstants.FSS_LIB_CONFIG_V113_TABLE_NAME))) {
                // 数据写入配置表
                result = LibConfigClient.getInstance().insert(data, conn);
            } else {
                result.put("errorCode", FssErrorCodeEnum.PHOENIX_TABLE_NOT_EXIST.getCode());
                L.error("insert error,table name: {} does not exist", tableName);
            }
        } catch (SQLException e) {
            result.put("errorCode", FssErrorCodeEnum.PHOENIX_INVALID_SQL.getCode());
            L.error("get connPool failed ", e);
        } finally {
            if (null != conn) {
                // 用完之后释放连接
                connPool.returnConnection(conn);
            }
        }

        return result;
    }

    /**
     * @param updateData 数据信息 更新数据
     * @return JSONObject 结果
     */
    public JSONObject update(JSONObject updateData) {
        JSONObject result = new JSONObject();
        Connection conn = null;
        String tableName = updateData.getString("table_name");
        String id = updateData.getString("id");
        JSONObject data = updateData.getJSONObject("data");
        try {
            conn = connPool.getConnection();
            if (VConstants.QUERY_FSS_PERSONLIST_V113.equals(id)
                && tableName.equalsIgnoreCase(ConfigManager.getString(VConstants.FSS_PERSONLIST_V113_TABLE_NAME))) {
                // 修改名单库
                result = PersonListClient.getInstance().update(data, conn);
            } else if (VConstants.QUERY_FSS_CAMERA_LIB_V113.equals(id)
                && tableName.equalsIgnoreCase(ConfigManager.getString(VConstants.FSS_CAMERA_LIB_V113_TABLE_NAME))) {
                // 修改布控表
                result = CameraLibClient.getInstance().upsert(data, conn);
            } else if (VConstants.BATCH_MODIFY_PERSONLIST_FLAG_V113.equals(id)
                && tableName.equalsIgnoreCase(ConfigManager.getString(VConstants.FSS_PERSONLIST_V113_TABLE_NAME))) {
                // 批量修改民单库flag
                result = PersonListClient.getInstance().batchModifyFlag(data, conn);
            } else {
                result = insert(updateData);
            }
        } catch (SQLException e) {
            result.put("errorCode", FssErrorCodeEnum.PHOENIX_INVALID_SQL.getCode());
            L.error("update data  failed ", e);
        } finally {
            if (null != conn) {
                // 用完之后释放连接
                connPool.returnConnection(conn);
            }
        }

        return result;
    }

    /**
     * @param deleteData 数据信息 删除数据
     * @return JSONObject 结果
     */
    public JSONObject delete(JSONObject deleteData) {
        JSONObject result = new JSONObject();
        Connection conn = null;
        String tableName = deleteData.getString("table_name");
        String id = deleteData.getString("id");
        JSONObject data = deleteData.getJSONObject("data");
        try {
            conn = connPool.getConnection();

            if (VConstants.QUERY_FSS_PERSONLIST_V113.equals(id)
                && tableName.equalsIgnoreCase(ConfigManager.getString(VConstants.FSS_PERSONLIST_V113_TABLE_NAME))) {
                // 删除名单库中数据
                result = PersonListClient.getInstance().delete(data, conn);
            } else if (VConstants.QUERY_FSS_CAMERA_LIB_V113.equals(id)
                && tableName.equalsIgnoreCase(ConfigManager.getString(VConstants.FSS_CAMERA_LIB_V113_TABLE_NAME))) {
                // 删除布控表中数据
                result = CameraLibClient.getInstance().delete(data, conn);
            } else if (VConstants.QUERY_FSS_REALTIONSHIP_V113.equals(id)
                && tableName.equalsIgnoreCase(ConfigManager.getString(VConstants.FSS_RELATIONSHIP_V113_TABLE_NAME))) {
                // 删除关系表数据
                result = RelationShipClient.getInstance().delete(data, conn);
            } else if (VConstants.QUERY_FSS_LIB_CONFIG_V113.equals(id)
                && tableName.equalsIgnoreCase(ConfigManager.getString(VConstants.FSS_LIB_CONFIG_V113_TABLE_NAME))) {
                // 删除配置表
                result = LibConfigClient.getInstance().delete(data, conn);
            } else {
                result.put("errorCode", FssErrorCodeEnum.PHOENIX_TABLE_NOT_EXIST.getCode());
                L.error("delete error,table name: {} does not exist", tableName);
            }
        } catch (SQLException e) {
            result.put("errorCode", FssErrorCodeEnum.PHOENIX_INVALID_SQL.getCode());
            L.error("delete data fail ", e);
        } finally {
            if (null != conn) {
                // 用完之后释放连接
                connPool.returnConnection(conn);
            }
        }
        return result;
    }

    /**
     * 查询数据
     * @param queryData 查询协议
     * @return JSONObject
     */
    public JSONObject query(JSONObject queryData) {
        JSONObject result = new JSONObject();
        Connection conn = null;
        String tableName = queryData.getString("table_name"); // FSS_ALARM_V113
        String id = queryData.getString("id");
        JSONObject data = queryData;
        L.debug("phoenix search client query params:", queryData);
        try {
            conn = connPool.getConnection();
            if (VConstants.QUERY_FSS_PERSONLIST_V113.equalsIgnoreCase(id)
                && tableName.equalsIgnoreCase(ConfigManager.getString(VConstants.FSS_PERSONLIST_V113_TABLE_NAME))) {
                // 名单库结构化信息查询,需要过滤掉已经被删除的记录
                result = PersonListClient.getInstance().query(data, conn, true);
            } else if (VConstants.QUERY_STATIC_FSS_PERSONLIST_V113.equalsIgnoreCase(id)
                && tableName.equalsIgnoreCase(ConfigManager.getString(VConstants.FSS_PERSONLIST_V113_TABLE_NAME))) {
                // 名单库静态图片搜索
                result = PersonListClient.getInstance().personListStaticSearch(data, conn, true);
            } else if (VConstants.QUERY_FSS_CAMERA_LIB_V113.equals(id)
                && tableName.equalsIgnoreCase(ConfigManager.getString(VConstants.FSS_CAMERA_LIB_V113_TABLE_NAME))) {
                // 查询布控表
                result = CameraLibClient.getInstance().query(data, conn);
            } else if (VConstants.QUERY_FSS_REALTIONSHIP_V113.equals(id)
                && tableName.equalsIgnoreCase(ConfigManager.getString(VConstants.FSS_RELATIONSHIP_V113_TABLE_NAME))) {
                // 查询人员关系表
                result = RelationShipClient.getInstance().query(data, conn);
            } else if (VConstants.QUERY_FSS_HISTORY_V113.equalsIgnoreCase(id)
                && tableName.equalsIgnoreCase(ConfigManager.getString(VConstants.FSS_HISTORY_V113_TABLE_NAME))) {
                // 查询历史表
                result = HistorySearchClient.getInstance().query(data, conn);
            } else if (VConstants.QUERY_FSS_ALARM_V113.equals(id)
                && tableName.equalsIgnoreCase(ConfigManager.getString(VConstants.FSS_AIALRM_V113_TABLE_NAME))) {
                // 查询告警表
                result = AlarmSearchClient.getInstance().query(data, conn);
            } else if (VConstants.QUERY_FSS_LIB_CONFIG_V113.equals(id)
                && tableName.equalsIgnoreCase(ConfigManager.getString(VConstants.FSS_LIB_CONFIG_V113_TABLE_NAME))) {
                // 查询配置表
                result = LibConfigClient.getInstance().query(data, conn);
            } else if (VConstants.GET_ALARM_EXPORT_DATA_V113.equals(id)
                && tableName.equalsIgnoreCase(ConfigManager.getString(VConstants.FSS_AIALRM_V113_TABLE_NAME))) {
                // 告警表查询结果导出
                result = AlarmSearchClient.getInstance().getAlarmExportData(data, conn);
            } else {
                result.put("errorCode", FssErrorCodeEnum.PHOENIX_TABLE_NOT_EXIST.getCode());
                L.error("query error,unknown query id: {}", id);
            }
        } catch (Exception e) {
            result.put("errorCode", FssErrorCodeEnum.PHOENIX_SYS_ERROR.getCode());
            L.error(" querying data fail ", e);
        } finally {
            if (null != conn) {
                // 用完之后释放连接
                connPool.returnConnection(conn);
            }
        }
        return result;
    }

    /**
     * 释放资源
     */
    public void close() throws Exception {
        connPool.closeConnectionPool();
        producer.close();
        // PersonListClient.getInstance().close();
        // LibConfigClient.getInstance().close();
        // CameraLibClient.getInstance().close();
    }
}

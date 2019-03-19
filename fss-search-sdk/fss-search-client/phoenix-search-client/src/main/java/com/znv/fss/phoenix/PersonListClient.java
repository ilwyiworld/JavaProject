package com.znv.fss.phoenix;

import com.alibaba.fastjson.JSONArray;
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
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/7/27.
 */
public final class PersonListClient extends ClientBase {
    private static final Logger L = LoggerFactory.getLogger(PersonListClient.class);
    private static String tableName = ConfigManager.getTableName(VConstants.FSS_PERSONLIST_V113_TABLE_NAME);
    // private static ProducerBase producer = new ProducerBase();
    private static final String LIBID = "lib_id";
    // private static final String LIBNAME = "lib_name";
    private static final String PERSONID = "person_id";
    private static final String ISDEL = "is_del";
    private static final String RELATIONID = "relation_id";
    private static final String ORIGINALLIBID = "original_lib_id";
    private static final int SIMTHRESHOLD = 50;
    private static final String IS_END = "is_end";
    private static final String IS_SEND = "is_send";
    private static final String PERSONLIB_TYPE = "personlib_type";

    // static {
    // init();
    // }

    /**
     * 构造函数
     */
    private PersonListClient() {
    }

    /**
     * @param insertData {lib_id + personlib_type + "data":JSONArray}
     * @param phoenixConn
     * @return
     */
    // 插入数据前先判断是否已经达到名单库数量上限 ,person_id为16位固定长度
    // 判断单条新增 or 批量新增 解析新的插入语句，
    // 拼凑lib_id,personlib_type 插入配置表
    // 根据personlib_type判断是否需要通知kafka
    public/* static synchronized */ JSONObject insert(JSONObject insertData, Connection phoenixConn) {
        JSONObject result = new JSONObject();
        result.put("errorCode", FssErrorCodeEnum.SUCCESS.getCode());

        // check
        if (phoenixConn == null) {
            result.put("errorCode", FssErrorCodeEnum.PHOENIX_CONN_NULL.getCode());
            L.error("insert error, phoenixConn is null");
            return result;
        }
        // 检查必传字段是否已经传入
        if (!(insertData.containsKey(LIBID) && null != insertData.getInteger(LIBID))) {
            result.put("errorCode", FssErrorCodeEnum.PHOENIX_INVALID_PARAM.getCode());
            return result;
        }
        long insertS = System.currentTimeMillis();
        // 检查名单库重点人员数量是否已经到达上限
        int count = insertData.getInteger("count");
        long personListMaxNum = ConfigManager.getLong(VConstants.FSS_PERSONLIST_MAX_NUM);
        if (count > personListMaxNum/* count <= personListMaxNum && count >= 0 */) {
            result.put("errorCode", FssErrorCodeEnum.PHOENIX_PERSONLIST_TOO_MANY.getCode());
            return result;
        }

        // start insert
        int insertLibId = insertData.getIntValue(LIBID);
        int personLibType = insertData.getInteger(PERSONLIB_TYPE);
        JSONArray sourceArray = insertData.getJSONArray("data"); // 获取插入的数据
        JSONObject insertRes = new JSONObject();

        // lq add for batch insert solve the problem for sendToKafka too many times
        boolean isBatch = false; // 标记是否为批量新增
        boolean isEnd = false; // 标记是否为最后一次批量新增
        boolean isSend = false; // 标记批量新增之前的批次是否有新增成功的数据
        if (insertData.containsKey(IS_END) && insertData.containsKey(IS_SEND)) {
            isBatch = true;

            if (personLibType == 1) { // 新增重点人员时才发送kafka通知
                if (insertData.getString(IS_END).equals("1")) {
                    isEnd = true; // 重点人员才修改is_end字段
                }
                if (insertData.getString(IS_SEND).equals("1")) {
                    isSend = true;
                }
            }
        }

        if (/* 1 == arrayLen */ !isBatch) {
            // 单条新增
            JSONObject sourceObj = sourceArray.getJSONObject(0);
            long t1 = System.currentTimeMillis();
            sourceObj.put(ISDEL, "0"); // 新增的人员默认不被删除
            sourceObj.put(LIBID, insertLibId);
            String personIdStr = String.format("%13d%3d", t1, 0).replace(" ", "0");
            sourceObj.put(PERSONID, personIdStr);
            sourceObj.put(PERSONLIB_TYPE, personLibType);
            // 新增名单库 + 通知kafka
            insertRes = ClientBase.insert(sourceObj, tableName, phoenixConn);
            // 0：基础人员；1：重点人员；2：其他；重点人员才发送通知协议
            // 前面的逻辑修改以后不再需要判断result中是否为success，因为！success则直接返回了
            if (personLibType == 1
                && "success".equals(FssErrorCodeEnum.SUCCESS.getExplanationByCode(insertRes.getInteger("errorCode")))) {
                sendToKafka(insertLibId, personIdStr);
            }

        } else if (/* arrayLen > 1 */ isBatch) {
            // 批量新增
            int index = 0;
            JSONArray insertArray = new JSONArray();
            int arrayLen = sourceArray.size();

            // 对批量新增的data为空情况做相应处理，之前批次有写入成功+当前批次为最后一批时通知kafka
            if (arrayLen == 0) {
                // lq modify,batch insert last time and insertData.size = 0
                if (isEnd && isSend) {
                    // batch insert last time , think about whether sendToKafka
                    sendToKafka(insertLibId); // 批量新增kafka通知发lib_id
                }

            } else if (arrayLen > 0) {
                for (int idx = 0; idx < arrayLen; idx++) {
                    long t1 = System.currentTimeMillis();
                    JSONObject sourceObj = sourceArray.getJSONObject(idx);
                    sourceObj.put(ISDEL, "0"); // 新增的人员默认不被删除
                    sourceObj.put(LIBID, insertLibId);
                    String personIdStr = String.format("%13d%3d", t1, index++).replace(" ", "0");
                    if (index > 999) {
                        index = 0;
                    }
                    sourceObj.put(PERSONID, personIdStr);
                    sourceObj.put(PERSONLIB_TYPE, personLibType);
                    insertArray.add(sourceObj);
                }
                // 新增名单库
                insertRes = ClientBase.batchInsert(insertArray, tableName, phoenixConn);

            result.put("errorCode", insertRes.getInteger("errorCode"));
            result.put("data", insertRes.getJSONArray("data"));

                // 重点人员才可能修改is_end字段为true
                // 当前批次之前的批次新增成功（即is_send为true）或当前此批新增成功 都发送kafka通知
                if (isEnd && (isSend || "success"
                    .equals(FssErrorCodeEnum.SUCCESS.getExplanationByCode(insertRes.getInteger("errorCode"))))) {
                    sendToKafka(insertLibId); // 批量新增kafka通知发lib_id
                }
            }
        }

        long insertE = System.currentTimeMillis();
        result.put("use", (insertE - insertS));
        return result;
    }

    /**
     * @param updateData 单条数据的所有内容,{lib_id + personlib_type + "data":JSONObject}data中有person_id
     * @param phoenixConn
     * @return
     */
    public JSONObject update(JSONObject updateData, Connection phoenixConn) {
        long updateTime1 = System.currentTimeMillis();
        JSONObject result = new JSONObject();
        result.put("errorCode", FssErrorCodeEnum.SUCCESS.getCode());
        int updateLibId = updateData.getIntValue(LIBID);
        JSONObject reorganizedObj = updateData.getJSONObject("data");
        String upsertPersonId = reorganizedObj.getString(PERSONID);
        JSONObject deleteResult = new JSONObject();
        int personLibType = -1;
        // int personLibTypeOld = -1; // 验证是否修改了personlib_type
        // check
        if (phoenixConn == null) {
            result.put("errorCode", FssErrorCodeEnum.PHOENIX_CONN_NULL.getCode());
            L.error("insert error, phoenixConn is null");
            return result;
        }
        if (!(updateData.containsKey(LIBID) && reorganizedObj.containsKey(PERSONID)
            && null != updateData.getInteger(LIBID) && !"".equals(reorganizedObj.getString(PERSONID)))) {
            result.put("errorCode", FssErrorCodeEnum.PHOENIX_INVALID_PARAM.getCode());
            return result;
        }

        // update start
        int originalLibId = -1;
        if (updateData.containsKey(ORIGINALLIBID)) {
            originalLibId = updateData.getInteger(ORIGINALLIBID);
        }

        String isModifyRelation = ""; // 是否修改关系表
        if (updateData.containsKey("is_modify_relation")) {
            isModifyRelation = updateData.getString("is_modify_relation");
        }

        // // 【lq-modify】 删除ORIGINALLIBID+personID下的数据，简化修改逻辑，去掉不必要的判断
        if (originalLibId != updateLibId) {
            JSONObject deleteData = new JSONObject();
            JSONObject data = new JSONObject();
            data.put(PERSONID, upsertPersonId);
            deleteData.put("data", data);
            deleteData.put(LIBID, originalLibId);
            deleteData.put("is_modify_relation", isModifyRelation);
            deleteResult = delete(deleteData, phoenixConn);
            result.put("errorCode", deleteResult.getInteger("errorCode"));
        }
        // 组名单库查询语句,名单库中只有lib_id, 没有lib_name
        // 按照 original_lib_id + person_id 将人员的全部信息查出来，再添加修改字段信息
        String sql = String.format("select * from %s where %s = %d and %s = '%s' ", tableName, LIBID, originalLibId,
            PERSONID, upsertPersonId);

        try {
            // 默认只有一条记录
            List<JSONObject> totalResult = super.query(sql, phoenixConn).getObject("data", List.class);
            if (totalResult.size() > 0) {
                JSONObject newestObj = totalResult.get(0);
                // if (newestObj.containsKey(PERSONLIB_TYPE) && newestObj.getInteger(PERSONLIB_TYPE) != null) {
                // personLibTypeOld = newestObj.getInteger(PERSONLIB_TYPE);
                // }

                newestObj.put(ISDEL, "0"); // 默认人员信息不被删除
                newestObj.put(LIBID, updateLibId);
                newestObj.putAll(reorganizedObj);

                // [lq-add]
                long currentTime = System.currentTimeMillis();
                Date timeDate = new Date(currentTime);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String timeStr = sdf.format(timeDate);
                newestObj.put("modify_time", timeStr);

                if (newestObj.containsKey(PERSONLIB_TYPE) && newestObj.getInteger(PERSONLIB_TYPE) != null) {
                    personLibType = newestObj.getInteger(PERSONLIB_TYPE);
                }
                // 更新名单库数据
                super.insert(newestObj, tableName, phoenixConn);

                if (isModifyRelation.equals("1")) {
                    System.out.println(" 修改关系表！");
                    // 更新人员关系表数据
                    String relationTable = RelationShipClient.getInstance().getTableName();
                    String relationSql = String.format(
                        " upsert into %s (relation_id,person_id,relation_lib_id,is_del)"
                            + " select relation_id,person_id,%d,'0' from %s "
                            + "where relation_id = '%s' and is_del = '1' ",
                        relationTable, updateLibId, relationTable, upsertPersonId);
                    super.executeSql(relationSql, phoenixConn);
                }

            }

        } catch (Exception e) {
            result.put("errorCode", FssErrorCodeEnum.PHOENIX_INVALID_SQL.getCode());
            L.error("error occur  upsert ", e);
        }

        // 发生真正的修改操作，并修改成功，通知 kafka
        // 当最终写入到名单库中的数据personlib_type为1时需要通知kafka
        // personLibTypeOld 只需要取删除操作中判断
        if (personLibType == 1 /* || personLibTypeOld == 1 */) {
            // 重点人员才通知kafka（personlib_type 从1改成其他，或从其他改成1都需要通知）
            if ("success".equals(FssErrorCodeEnum.getExplanationByCode(result.getInteger("errorCode")))) {
                sendToKafka(updateLibId, upsertPersonId);
            }
        }

        long updateTime2 = System.currentTimeMillis();
        result.put("use", (updateTime2 - updateTime1));

        return result;
    }

    /**
     * @param deleteData ,lib_id、person_id必填
     * @param phoenixConn
     * @return
     */
    // 名单库支持单条删除，删除名单库信息不需要同步删除配置表
    // 名单库的删除操作是修改is_del字段为1，不是真正的删除
    // 删除名单库的同时删除人员关系信息
    public JSONObject delete(JSONObject deleteData, Connection phoenixConn) {
        JSONObject result = new JSONObject();
        JSONObject data = deleteData.getJSONObject("data");

        if (!(deleteData.containsKey(LIBID) && data.containsKey(PERSONID) && null != deleteData.getInteger(LIBID)
            && !"".equals(data.getString(PERSONID)))) {
            result.put("errorCode", FssErrorCodeEnum.PHOENIX_INVALID_PARAM.getCode());
            return result;
        }

        int deleteLibId = deleteData.getIntValue(LIBID);
        String deletePersonId = data.getString(PERSONID);

        long deleteTimeS = System.currentTimeMillis();
        // 查询名单库信息
        String deleteSql = String.format(" upsert into %s (%s,%s,%s) values (%d,'%s','%s')", tableName, LIBID, PERSONID,
                /*"is_del"*/ISDEL, deleteLibId, deletePersonId, "1");
        // L.info("delete from {}: {}", tableName, deleteSql);
        // 校验删除的人员是否为重点人员
        String checkSendSql = String.format(" select %s from %s where %s = '%s' and %s = %d", PERSONLIB_TYPE, tableName,
            PERSONID, deletePersonId, LIBID, deleteLibId);
        int personLibType = -1;
        try {
            super.executeSql(deleteSql, phoenixConn);
            result.put("errorCode", FssErrorCodeEnum.SUCCESS.getCode());

            String isModifyRelation = ""; // 是否修改关系表
            if (deleteData.containsKey("is_modify_relation")) {
                isModifyRelation = deleteData.getString("is_modify_relation");
            }

            if (isModifyRelation.equals("1")) {
                System.out.println(" 删除 关系表！");
                // 修改人员关系表
                String relationTable = RelationShipClient.getInstance().getTableName();
                // 先判断该人在人员关系中是否存在
                String checkSql = String.format(" select * from %s where (%s = '%s' or %s = '%s') and is_del = '0'",
                    relationTable, PERSONID, deletePersonId, RELATIONID, deletePersonId);
                List<JSONObject> checkResult = super.query(checkSql, phoenixConn).getObject("data", List.class);
                if (checkResult.size() > 0) {
                    String relationSql = String.format(
                        " upsert into %s (%s,%s,%s) select %s,%s,'1' from %s where %s = '%s' or %s = '%s' ",
                        relationTable, PERSONID, RELATIONID, ISDEL, PERSONID, RELATIONID, relationTable, PERSONID,
                        deletePersonId, RELATIONID, deletePersonId);
                    super.executeSql(relationSql, phoenixConn);
                    result.put("errorCode", FssErrorCodeEnum.SUCCESS.getCode());
                }
            }

            List<JSONObject> checkSendResult = super.query(checkSendSql, phoenixConn).getObject("data", List.class);

            if (checkSendResult.size() > 0) { // PERSONLIB_TYPE 可能为空
                if (checkSendResult.get(0).containsKey(PERSONLIB_TYPE)
                    && checkSendResult.get(0).getInteger(PERSONLIB_TYPE) != null) {
                    personLibType = checkSendResult.get(0).getInteger(PERSONLIB_TYPE); // 默认只有一条数据
                }

            }
            // personLibType = checkSendResult.get(0).getInteger(PERSONLIB_TYPE); // 默认只有一条数据

        } catch (Exception e) {
            result.put("errorCode", FssErrorCodeEnum.PHOENIX_INVALID_SQL.getCode());
            L.error(" error occur when delete data from {}", tableName, e);
        }

        if (personLibType == 1
            && "success".equals(FssErrorCodeEnum.getExplanationByCode(result.getInteger("errorCode")))) { // 重点人员发生修改才通知
            // if ("success".equals(FssErrorCodeEnum.getExplanationByCode(result.getInteger("errorCode")))) {
            sendToKafka(deleteLibId, deletePersonId);
            // }
        }

        long deleteTimeEnd = System.currentTimeMillis();
        result.put("use", (deleteTimeEnd - deleteTimeS));

        return result;
    }

    /**
     * @param queryData
     * @param phoenixConn
     * @return
     */
    /**
     * 分页查询，查询中要有关于分页的必填字段信息，查询结构化信息
     */
    public JSONObject query(JSONObject queryData, Connection phoenixConn, boolean checkDel) {
        JSONObject result = new JSONObject();
        result.put("errorCode", FssErrorCodeEnum.SUCCESS.getCode());
        // check
        if (phoenixConn == null) {
            result.put("errorCode", FssErrorCodeEnum.PHOENIX_CONN_NULL.getCode());
            L.error(String.format("error occur when querying %s data, phoenixConn is null", tableName));
            return result;
        } else if (!(queryData.containsKey("count") && queryData.containsKey("total_page")
            && queryData.containsKey("page_no") && queryData.containsKey("page_size")
            && null != queryData.getInteger("count") && null != queryData.getInteger("total_page")
            && null != queryData.getInteger("page_no") && null != queryData.getInteger("page_size"))) {
            result.put("errorCode", FssErrorCodeEnum.PHOENIX_INVALID_PARAM.getCode());
            return result;
        }

        // query start
        List<JSONObject> resList = new ArrayList<JSONObject>();
        long startTime = System.currentTimeMillis();
        // 首次查询先统计总行数和页数，翻页查询不再就算
        int count = queryData.getInteger("count");
        int totalPage = queryData.getInteger("total_page");

        // 每页显示的条数
        if (queryData.containsKey("page_size") && null != queryData.getInteger("page_size")) {
            int pageSize = queryData.getInteger("page_size");
            // 当传入的每页行数为0时默认每页10条
            if (pageSize <= 0) {
                pageSize = 10;
            }
            queryData.put("page_size", pageSize);
        }

        // 增加is_del字段，用来判断是否过滤查询结果中的已被删除的数据 lq add
        if (queryData.containsKey(ISDEL) && queryData.getString(ISDEL).equals("1")) {
            checkDel = false;
        }

        try {
            if (-1 == count && -1 == totalPage) {
                // 首次查询
                JSONObject total = organizeQuerySql(queryData, phoenixConn, checkDel, 1);
                count = total.getIntValue("count");
                totalPage = total.getIntValue("total_page");
                // L.info("query {},count: {},total page: {}", tableName, count, totalPage);
                result.put("errorCode", total.getInteger("errorCode"));
            }

            if (0 == count) {
                // 查询结果为空
                result.put("data", resList);
                result.put("time", System.currentTimeMillis() - startTime);
                result.put("count", count);
                result.put("total_page", totalPage);
            } else {
                // 查询结果中有值
                result = organizeQuerySql(queryData, phoenixConn, checkDel, 0);
                result.put("time", System.currentTimeMillis() - startTime);
                result.put("count", count);
                result.put("total_page", totalPage);
            }
        } catch (Exception e) {
            result.put("errorCode", FssErrorCodeEnum.PHOENIX_SYS_ERROR.getCode());
            L.error(String.format("error occur when querying data by %s ", tableName, e));
        }

        return result;
    }

    /**
     * @param query
     * @param phoenixConn
     * @param checkDel
     * @param flag
     * @return
     */
    /* 说明：只返回图片和特征值以外的字段 ,同时去掉身份证图片 */
    private JSONObject organizeQuerySql(JSONObject query, Connection phoenixConn, boolean checkDel, int flag) {
        JSONObject result = new JSONObject();
        result.put("errorCode", FssErrorCodeEnum.SUCCESS.getCode());
        List<JSONObject> resList = new ArrayList<>();
        // 版本2，改成先获取lib_id、person_id，然后再批量获取结果，已经放弃
        // JSONArray keyList = new JSONArray();

        StringBuilder sql = new StringBuilder();
        int pageNo = query.getIntValue("page_no");
        int pageSize = query.getIntValue("page_size");

        // 每页显示的条数
        if (query.containsKey("page_size") && null != query.getInteger("page_size")) {
            pageSize = query.getInteger("page_size");
            // 当传入的每页行数为0时默认每页10条
            if (pageSize <= 0) {
                pageSize = 10;
            }
        }

        // 时间范围查询
        JSONObject queryRange = null;
        if (query.containsKey("query_range_modify") && !query.getString("query_range_modify").isEmpty()) {
            queryRange = query.getJSONObject("query_range_modify");
        }

        // check,特征值查询为必传字段
        if (!(null != queryRange && !queryRange.isEmpty())) {
            result.put("errorCode", FssErrorCodeEnum.PHOENIX_INVALID_PARAM.getCode());
            return result;
        }

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

        // like查询条件，需要用到like的可以将条件添加到这里
        JSONObject queryLike = null;
        if (query.containsKey("query_like")) {
            queryLike = query.getJSONObject("query_like");
        }

        // 特征值查询条件，包含特征值和相似度
        JSONObject queryFeature = null;
        boolean isSearchFeature = false;
        if (query.containsKey("query_feature")) {
            queryFeature = query.getJSONObject("query_feature");
            if (queryFeature.containsKey("feature") && !queryFeature.getString("feature").isEmpty()) {
                isSearchFeature = true;
            }
            // 设置相似度阈值默认值
            if (queryFeature.containsKey("sim")) {
                if (queryFeature.getInteger("sim") < SIMTHRESHOLD) {
                    queryFeature.remove("sim");
                    queryFeature.put("sim", SIMTHRESHOLD);
                }
            }
        }

        // 只查找is_del为0的记录，is_del为1表示该记录已经被删除
        if (checkDel) {
            if (queryTerm == null) {
                queryTerm = new JSONObject();
            }
            queryTerm.put("is_del", "0");
        }

        // 组查询语句需要返回字段部分
        // flag，0-组装数据查询sql，1-组装数据统计sql
        if (flag == 0) {
            // sql.append("select * from ").append(tableName).append(" WHERE");
            // 返回除图片和特征值以外的字段，图片通过名单库获取图片接口获取
            sql.append("select ");
            for (String key : schema.keySet()) {
                // 去掉key中的列族信息
                if (key.contains(".")) {
                    key = key.split("\\.")[1];
                }
                sql.append(key).append(",");
            }

            if (isSearchFeature) { // 按图片查询返回相似度 add
                // 查询结果添加查询图片与实时图片间的相似度
                sql.append("FeatureComp(feature,?) as sim");
            } else {
                // 去掉多余的逗号
                int idx1 = sql.lastIndexOf(",");
                if (idx1 != -1) {
                    sql.delete(idx1, sql.length());
                }
            }
            sql.append(" from ").append(tableName).append(" WHERE");

        } else {
            sql.append("select count(1) from ").append(tableName).append(" WHERE");
        }

        // 获取查询字段
        // 修改时间范围查询(必填）
        String orderType = ""; // 排序方式，1：修改时间倒排序，0：姓名升序排序
        if (/* null != queryRange && */ queryRange.size() != 0) {
            sql.append(" modify_time BETWEEN '").append(queryRange.getString("start_time")).append("' AND '")
                .append(queryRange.getString("end_time")).append("'");
            orderType = queryRange.getString("order");
        }
        // 单值查询条件
        boolean hasQueryTerm = false;
        if (null != queryTerm && !queryTerm.isEmpty()) {
            hasQueryTerm = true;
            sql.append(" AND ");
            for (String key : queryTerm.keySet()) {
                sql.append(" ").append(key + " = ? AND");
            }
            sql.delete(sql.lastIndexOf("AND"), sql.length());
        }
        // 多值查询条件
        boolean hasMulti = false;
        if (null != queryMulti && !queryMulti.isEmpty()) {
            hasMulti = true;
            sql.append(" AND ");
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
        boolean hasLike = false;
        if (queryLike != null && !queryLike.isEmpty()) {
            hasLike = true;
            sql.append(" AND ");
            for (String key : queryLike.keySet()) {
                sql.append(" ").append(key).append(" like '%").append(queryLike.getString(key)).append("%' AND");
            }
            sql.delete(sql.lastIndexOf("AND"), sql.length());
        }
        // 特征值条件
        if (flag == 0) {
            if (null != queryFeature && !queryFeature.isEmpty()) {
                if (isSearchFeature) {
                    sql.append(" AND ");
                }
                sql.append(" FeatureComp(feature,?) >= ?").append(" order by FeatureComp(feature,?) desc limit ")
                    .append(pageSize);
            } else if ("1".equals(orderType)) {
                // 1:修改时间倒排序
                sql.append(" order by modify_time desc limit ").append(pageSize);
            } else {
                sql.append(" order by person_name asc limit ").append(pageSize);
            }

            if (pageNo != 1) {
                sql.append(" offset ").append((pageNo - 1) * pageSize);
            }
        } else {
            if (null != queryFeature && !queryFeature.isEmpty()) {
                if (isSearchFeature) {
                    sql.append(" AND ");
                }
                sql.append(" FeatureComp(feature,?) >= ?");
            }
        }

        // L.info("query: {}", sql.toString());

        // 获取查询字段对应的值
        int i = 1;
        PreparedStatement stat = null;
        ResultSet rs = null;
        try {
            stat = phoenixConn.prepareStatement(sql.toString());
            if (flag == 0) {
                // 按图片查询（返回字段）add
                if (isSearchFeature && flag == 0) {
                    byte[] byteFeature = Base64.getDecoder().decode(queryFeature.getString("feature"));
                    stat.setObject(i, byteFeature);
                    i++;
                }
            }
            // 单值查询条件
            if (null != queryTerm && !queryTerm.isEmpty()) {
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
            if (null != queryFeature && !queryFeature.isEmpty()) {
                if (flag == 0) {
                    byte[] byteFeature = Base64.getDecoder().decode(queryFeature.getString("feature"));
                    stat.setObject(i, byteFeature);
                    i++;
                    stat.setObject(i, queryFeature.getIntValue("sim"));
                    i++;
                    stat.setObject(i, byteFeature);
                } else {
                    byte[] byteFeature = Base64.getDecoder().decode(queryFeature.getString("feature"));
                    stat.setObject(i, byteFeature);
                    i++;
                    stat.setObject(i, queryFeature.getIntValue("sim"));
                }

            }

            // 执行查询语句获取结果集
            rs = stat.executeQuery();
            if (flag == 0) {
                while (rs.next()) {
                    JSONObject record = new JSONObject();
                    ResultSetMetaData rsMetaData = rs.getMetaData();
                    int columnCount = rsMetaData.getColumnCount();
                    for (int column = 0; column < columnCount; column++) {
                        String field = rsMetaData.getColumnLabel(column + 1);
                        record.put(field.toLowerCase(), rs.getObject(field));
                    }
                    resList.add(record); // 版本3
                    // keyList.add(record); // 版本2
                }
                // JSONObject tempResult = getPersonInfo(keyList, phoenixConn);
                // resList = tempResult.getObject("data", List.class);
                // result.put("errorCode", tempResult.getString("errorCode"));
                result.put("data", resList);
            } else {
                while (rs.next()) {
                    int count = rs.getInt(1);
                    result.put("count", count);
                    result.put("total_page", count % pageSize == 0 ? count / pageSize : count / pageSize + 1);
                }
            }
        } catch (Exception e) {
            result.put("errorCode", FssErrorCodeEnum.PHOENIX_INVALID_SQL.getCode());
            L.error("query blacklist error ", e);
            // e.printStackTrace();
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

        return result;
    }

    /**
     * @param queryData
     * @param phoenixConn
     * @return
     */
    /**
     * 带图片查询
     */
    public JSONObject personListStaticSearch(JSONObject queryData, Connection phoenixConn, boolean checkDel) {
        JSONObject result = new JSONObject();
        result.put("errorCode", FssErrorCodeEnum.SUCCESS.getCode());
        if (phoenixConn == null) {
            result.put("errorCode", FssErrorCodeEnum.PHOENIX_CONN_NULL.getCode());
            L.error("error occur when querying blackList data, phoenixConn is null");
            return result;
        } else if (!(queryData.containsKey("query_feature") && !queryData.getJSONObject("query_feature").isEmpty())) {
            result.put("errorCode", FssErrorCodeEnum.PHOENIX_FEATURE_NOT_EXIST.getCode());
            return result;
        }

        List<JSONObject> objList = new ArrayList<>();
        long t1 = System.currentTimeMillis();
        // 首次查询先查询总条数和总页数，翻页查询不需要再次查询总条数和总页数
        int count = queryData.getIntValue("count");
        int totalPage = queryData.getIntValue("total_page");
        try {
            if (count == -1 && totalPage == -1) {
                JSONObject total = organizeStaticSearch(queryData, phoenixConn, checkDel, 1);
                count = total.getIntValue("count");
                totalPage = total.getIntValue("total_page");
                // L.info("query blacklist,count: {},total page: {}", count, totalPage);
            }
            // 没有符合条件的记录，直接返回
            if (count == 0) {
                result.put("data", objList);
                result.put("time", System.currentTimeMillis() - t1);
                result.put("count", count);
                result.put("total_page", totalPage);
            } else {
                result = organizeStaticSearch(queryData, phoenixConn, checkDel, 0);
                result.put("time", System.currentTimeMillis() - t1);
                result.put("count", count);
                result.put("total_page", totalPage);
            }
        } catch (Exception e) {
            result.put("errorCode", FssErrorCodeEnum.PHOENIX_SYS_ERROR.getCode());
            L.error("error occur when querying blackList data", e);
        }

        return result;
    }

    /**
     * @param query
     * @param phoenixConn
     * @param checkDel
     * @param flag
     * @return
     * @throws Exception
     */
    private JSONObject organizeStaticSearch(JSONObject query, Connection phoenixConn, boolean checkDel, int flag)
        throws Exception {
        JSONObject result = new JSONObject();
        result.put("errorCode", FssErrorCodeEnum.SUCCESS.getCode());

        List<JSONObject> objList = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        int pageNo = query.getIntValue("page_no");
        int pageSize = query.getIntValue("page_size");

        // check,特征值查询条件，包含特征值和相似度
        if (!(query.containsKey("query_feature") && null != query.getJSONObject("query_feature"))) {
            result.put("errorCode", FssErrorCodeEnum.PHOENIX_FEATURE_NOT_EXIST.getCode());
            return result;
        }

        // 单值查询条件
        JSONObject queryTerm = null;
        if (checkDel) {
            queryTerm = new JSONObject();
            queryTerm.put("is_del", "0");
        }
        JSONObject queryFeature = query.getJSONObject("query_feature");
        // flag，0-组装数据查询sql，1-组装数据统计sql
        if (flag == 0) {
            sql.append("select ");
            for (String key : schema.keySet()) {
                // 去掉key中的列族信息
                if (key.contains(".")) {
                    key = key.split("\\.")[1];
                }
                sql.append(key).append(",");
            }
            sql.append("FeatureComp(feature,?) as sim from ").append(tableName).append(" WHERE");
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
        if (hasQueryTerm) {
            sql.append(" AND");
        }
        if (flag == 0) {
            sql.append(" FeatureComp(feature,?) >= ?").append(" order by FeatureComp(feature,?) desc limit ")
                .append(pageSize);
            if (pageNo != 1) {
                sql.append(" offset ").append((pageNo - 1) * pageSize);
            }
        } else {
            sql.append(" FeatureComp(feature,?) >= ?");
        }
        // L.info("organizeStaticSearch sql: {}", sql.toString());

        int i = 1;
        PreparedStatement stat = null;
        ResultSet rs = null;
        try {
            stat = phoenixConn.prepareStatement(sql.toString());
            if (flag == 0) {
                stat.setObject(i, Base64.getDecoder().decode(queryFeature.getString("feature")));
                i++;
            }
            // 单值查询条件
            if (null != queryTerm && !queryTerm.isEmpty()) {
                for (String key : queryTerm.keySet()) {
                    stat.setObject(i, queryTerm.get(key));
                    i++;
                }
            }

            if (flag == 0) {
                byte[] byteFeature = Base64.getDecoder().decode(queryFeature.getString("feature"));
                stat.setObject(i, byteFeature);
                i++;
                stat.setObject(i, queryFeature.getIntValue("sim"));
                i++;
                stat.setObject(i, byteFeature);
            } else {
                byte[] byteFeature = Base64.getDecoder().decode(queryFeature.getString("feature"));
                stat.setObject(i, byteFeature);
                i++;
                stat.setObject(i, queryFeature.getIntValue("sim"));
            }

            // 执行查询语句，获取结果集
            rs = stat.executeQuery();
            if (flag == 0) {
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

                    objList.add(record);

                }
                result.put("data", objList);
            } else {
                while (rs.next()) {
                    int count = rs.getInt(1);
                    result.put("count", count);
                    result.put("total_page", count % pageSize == 0 ? count / pageSize : count / pageSize + 1);
                }
            }
        } catch (Exception e) {
            result.put("errorCode", FssErrorCodeEnum.PHOENIX_INVALID_SQL.getCode());
            L.error("organizeStaticSearch query error ", e);
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

        return result;
    }

    /**
     * @param queryData
     * @param phoenixConn
     * @return
     */
    public JSONObject getPersonListCount(JSONObject queryData, Connection phoenixConn) {
        JSONObject result = new JSONObject();
        int libId = 0;
        String sql = String.format(" select count(1) from %s where ", tableName);
        if (queryData.containsKey(LIBID) && null != queryData.getInteger(LIBID)) {
            libId = queryData.getInteger(LIBID);
            sql = sql + String.format(" %s = %d and ", LIBID, libId);
        }
        sql = sql + String.format(" %S = '0'", ISDEL);
        PreparedStatement stat = null;
        ResultSet rs = null;

        try {
            stat = phoenixConn.prepareStatement(sql);
            rs = stat.executeQuery();
            while (rs.next()) {
                int count = rs.getInt(1);
                result.put("count", count);
                result.put("errorCode", FssErrorCodeEnum.SUCCESS.getCode());
            }
        } catch (SQLException e) {
            result.put("errorCode", FssErrorCodeEnum.PHOENIX_INVALID_SQL.getCode());
            L.error(" ERROR occur when getCount ", e);
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
        return result;
    }

    /**
     * @param queryList
     * @param phoenixConn
     * @return
     */
    // 方法调用较频繁，所以查询没有调用clientbase中的query（）方法
    public JSONObject getPersonInfo(JSONArray queryList, Connection phoenixConn) {
        JSONObject result = new JSONObject();
        List<JSONObject> objList = new ArrayList<>();
        int querySize = queryList.size();
        StringBuilder sql = new StringBuilder();
        sql.append("select * from ").append(tableName).append(" where ");

        if (querySize /* > */ <= 0) {
            result.put("errorCode", FssErrorCodeEnum.PHOENIX_INVALID_PARAM.getCode());
            result.put("data", objList);
            return result;
        }

        // 组查询语句
        for (int i = 0; i < querySize; i++) {
            JSONObject obj = queryList.getJSONObject(i);
            if (obj.containsKey(LIBID) && obj.containsKey(PERSONID) && null != obj.getInteger(LIBID)
                && !obj.getString(PERSONID).isEmpty()) {
                int libId = obj.getInteger(LIBID);
                String personId = obj.getString(PERSONID);
                sql.append(" (").append(LIBID).append(" = ").append(libId).append(" and ").append(PERSONID)
                    .append(" = '").append(personId).append("') or"); // (%s = %d and %s = '%s') or

            }

        }
        // 删除最后一个“or”
        sql.delete(sql.lastIndexOf("or"), sql.length());
        // System.out.println("getPersonInfo SQL : " + sql.toString());

        // 执行查询语句，获取结果集
        PreparedStatement stat = null;
        ResultSet rs = null;
        try {
            stat = phoenixConn.prepareStatement(sql.toString());
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
            result.put("data", objList);
            result.put("errorCode", FssErrorCodeEnum.SUCCESS.getCode());
        } catch (SQLException e) {
            result.put("errorCode", FssErrorCodeEnum.PHOENIX_INVALID_SQL.getCode());
            L.error("organizeStaticSearch query error ", e);
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

        return result;
    }

    /**
     * @param personResultMap
     * @param phoenixConn
     * @return
     * @throws SQLException
     */
    public Map getPersonInfo(Map<String, JSONObject> personResultMap, Connection phoenixConn) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("select * from ").append(tableName).append(" where ");
        Iterator<Map.Entry<String, JSONObject>> paramEntryIterator = personResultMap.entrySet().iterator();

        while (paramEntryIterator.hasNext()) {
            Map.Entry<String, JSONObject> e = paramEntryIterator.next();
            JSONObject obj = e.getValue();

            if (obj.containsKey(LIBID) && obj.containsKey(PERSONID) && null != obj.getInteger(LIBID)
                && !obj.getString(PERSONID).isEmpty()) {
                int libId = obj.getInteger(LIBID);
                String personId = obj.getString(PERSONID);
                sql.append(" (").append(LIBID).append(" = ").append(libId).append(" and ").append(PERSONID)
                    .append(" = '").append(personId).append("') or"); // (%s = %d and %s = '%s') or

            }
        }
        // 删除最后一个“or”
        sql.delete(sql.lastIndexOf("or"), sql.length());

        // 执行查询语句，获取结果集
        PreparedStatement stat = null;
        ResultSet rs = null;
        try {
            stat = phoenixConn.prepareStatement(sql.toString());
            rs = stat.executeQuery();
            while (rs.next()) {
                JSONObject record = new JSONObject();
                ResultSetMetaData rsMetaData = rs.getMetaData();
                int columnCount = rsMetaData.getColumnCount();
                for (int column = 0; column < columnCount; column++) {
                    String field = rsMetaData.getColumnLabel(column + 1);
                    record.put(field.toLowerCase(), rs.getObject(field));
                }
                String personId = record.getString(PERSONID);
                int libId = record.getInteger(LIBID);
                String mapKey = Integer.toString(libId) + personId;
                personResultMap.put(mapKey, record);
            }
            // System.out.println(" PERSON LIST ；" + personResultMap.get("30000000000000015"));
            // System.out.println(" alarm search personlist success !");
        } catch (SQLException e) {
            // System.out.println(" alarm search personList PHOENIX_INVALID_SQL .");
            L.error("organizeStaticSearch query error ", e);
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



        return personResultMap;
    }

    /**
     * @param queryData
     * @param phoenixConn
     * @return
     */
    public JSONObject getPersonPicture(JSONObject queryData, Connection phoenixConn) {
        JSONObject result = new JSONObject();
        result.put("errorCode", FssErrorCodeEnum.SUCCESS.getCode());
        if (phoenixConn == null) {
            result.put("errorCode", FssErrorCodeEnum.PHOENIX_CONN_NULL.getCode());
            L.error(String.format("error occur when getPersonPicture %s data, phoenixConn is null", tableName));
            return result;
        } else if (!(queryData.containsKey(PERSONID) && !queryData.getString(PERSONID).isEmpty()
            && queryData.containsKey(LIBID) && null != queryData.getInteger(LIBID))) {
            result.put("errorCode", FssErrorCodeEnum.PHOENIX_INVALID_PARAM.getCode());
            L.error(" rowkey not only when get person picture");
            return result;
        }

        // 组查询语句
        String sql = String.format(" select person_img,is_del from %s where ", tableName);
        String personId = queryData.getString(PERSONID);
        int libId = queryData.getInteger(LIBID);
        sql = sql + String.format(" %s = %d and %s = '%s'", LIBID, libId, PERSONID, personId);

        PreparedStatement stat = null;
        ResultSet rs = null;
        try {
            stat = phoenixConn.prepareStatement(sql);
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
            L.error("error occur when get person picture", e);
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

        return result;

    }

    /**
     * @param queryData
     * @param phoenixConn
     * @return
     */
    /* 获取身份证上的头像 */
    public JSONObject getCardPicture(JSONObject queryData, Connection phoenixConn) {
        JSONObject result = new JSONObject();
        result.put("errorCode", FssErrorCodeEnum.SUCCESS.getCode());
        if (phoenixConn == null) {
            result.put("errorCode", FssErrorCodeEnum.PHOENIX_CONN_NULL.getCode());
            L.error(String.format("error occur when getCardPicture %s data, phoenixConn is null", tableName));
            return result;
        } else if (!(queryData.containsKey(PERSONID) && !queryData.getString(PERSONID).isEmpty()
            && queryData.containsKey(LIBID) && null != queryData.getInteger(LIBID))) {
            result.put("errorCode", FssErrorCodeEnum.PHOENIX_INVALID_PARAM.getCode());
            L.error(" rowkey not only when get person card picture");
            return result;
        }

        String sql = String.format(" select positive_url,is_del from %s where ", tableName);
        String personId = queryData.getString(PERSONID);
        int libId = queryData.getInteger(LIBID);
        sql = sql + String.format(" %s = %d and %s = '%s'", LIBID, libId, PERSONID, personId);

        PreparedStatement stat = null;
        ResultSet rs = null;
        try {
            stat = phoenixConn.prepareStatement(sql);
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
            L.error("error occur when get person card picture", e);
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

        return result;

    }

    /**
     * @param queryData
     * @param phoenixConn
     * @return
     */
    public JSONObject batchModifyFlag(JSONObject queryData, Connection phoenixConn) {
        JSONObject result = new JSONObject();
        result.put("errorCode", FssErrorCodeEnum.SUCCESS.getCode());
        int aa = queryData.getInteger("flag");
        int bb = queryData.getInteger(LIBID);
        if (phoenixConn == null) {
            result.put("errorCode", FssErrorCodeEnum.PHOENIX_CONN_NULL.getCode());
            L.error(String.format("error occur when batchModifyFlag %s data, phoenixConn is null", tableName));
            return result;
        } else if (!(queryData.containsKey("flag") && null != queryData.getInteger("flag")
            && (queryData.getInteger("flag") == 0 || queryData.getInteger("flag") == 1) && queryData.containsKey(LIBID)
            && null != queryData.getInteger(LIBID) && queryData.containsKey(PERSONLIB_TYPE)
            && null != queryData.getInteger(PERSONLIB_TYPE))) {
            result.put("errorCode", FssErrorCodeEnum.PHOENIX_INVALID_PARAM.getCode());
            L.error("param not only when batchModifyFlag");
            return result;
        }

        // 组sql语句
        int flag = queryData.getInteger("flag");
        int libId = queryData.getInteger(LIBID);
        String isDel = "0";//只修改在库未删除人员的布控状态
        String controlStartTime = queryData.getString("control_start_time");
        String controlEndTime = queryData.getString("control_end_time");
        long currentTime = System.currentTimeMillis();
        Date timeDate = new Date(currentTime);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String modifyTime = sdf.format(timeDate);
        String sql = String.format(
            " upsert into %s (lib_id,person_id,flag,modify_time,control_start_time,control_end_time) select lib_id,person_id,%d,'%s','%s','%s' from %s where lib_id = %d and is_del = '%s' ",
            tableName, flag, modifyTime, controlStartTime, controlEndTime, tableName, libId, isDel);
        try {
            long t1 = System.currentTimeMillis();
            super.executeSql(sql, phoenixConn);
            int personLibType = queryData.getInteger(PERSONLIB_TYPE); // 1:重点
            if (personLibType == 1) {
                sendToKafka(libId);
            }
            long t2 = System.currentTimeMillis();
            result.put("time", t2 - t1);
        } catch (Exception e) {
            result.put("errorCode", FssErrorCodeEnum.PHOENIX_SYS_ERROR.getCode());
            L.error("batchModifyFlag error ",e);
            // e.printStackTrace();
        }

        return result;
    }

    /**
     * 发送到kafka
     */
    private/* static */ void sendToKafka(int primaryId) {
        ProducerBase producer = PhoenixClient.getProducer();
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
        System.out.println("batch-PersonListClient-batch-ret:" + ret + ",send_time:" + timeStr);
        System.out.println("batch-PersonListClient-batch-ret:" + notifyMsg.toString());

    }

    private/* static */ void sendToKafka(int primaryId, String referenceId) {
        ProducerBase producer = PhoenixClient.getProducer();
        long sendS = System.currentTimeMillis();
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
        System.out.println("single-PersonListClient-ret:" + ret + ",send_time:" + timeStr);
        long sendE = System.currentTimeMillis();
        System.out.println("sendToKafka use :" + (sendE - sendS));

    }

    /**
     * 初始化kafka
     */
    // private static void init() {
    // try {
    // Properties props = ConfigManager.getProducerProps();
    // producer.initWithConfig(props);
    // producer.setMsgTypeParam(ConfigManager.getString(VConstants.NOTIFY_TOPIC_MSGTYPE),
    // ConfigManager.getString(VConstants.ZOOKEEPER_ADDR),
    // ConfigManager.getInt(VConstants.NOTIFY_PARTITION_NUM),
    // ConfigManager.getInt(VConstants.NOTIFY_REPLICATION_NUM));
    // // L.info("finished init kafka...");
    // } catch (Exception e) {
    // L.error("init kafka... error ", e);
    // }
    // }

    // 表结构 注释掉部分查询不需要返回的字段
    private static HashMap<String, String> schema = new HashMap();
    {
        schema.put("lib_id", "unsigned_int not null");
        schema.put("person_id", "unsigned_int not null");
        schema.put("attr.person_name", "VARCHAR");
        schema.put("attr.birth", "VARCHAR");
        schema.put("attr.nation", "VARCHAR");
        schema.put("attr.country", "VARCHAR");
        // schema.put("attr.positive_url", "VARCHAR");
        // schema.put("attr.negative_url", "VARCHAR");
        schema.put("attr.addr", "VARCHAR");
        schema.put("attr.tel", "VARCHAR");
        schema.put("attr.nature_residence", "VARCHAR");
        schema.put("attr.room_number", "VARCHAR");
        schema.put("attr.door_open", "unsigned_int");
        schema.put("attr.sex", "unsigned_int");
        schema.put("attr.image_name", "VARCHAR");
        // schema.put("pics.person_img", "VARBINARY");
        // schema.put("pics.person_img2", "VARBINARY");
        // schema.put("pics.person_img3", "VARBINARY");
        // schema.put("feature.feature", "VARBINARY");
        // schema.put("feature.feature2", "VARBINARY");
        // schema.put("feature.feature3", "VARBINARY");
        schema.put("attr.card_id", "VARCHAR");
        schema.put("attr.flag", "unsigned_int");
        schema.put("attr.comment", "VARCHAR");
        schema.put("attr.control_start_time", "VARCHAR");
        schema.put("attr.control_end_time", "VARCHAR");
        schema.put("attr.is_del", "VARCHAR");
        schema.put("attr.create_time", "VARCHAR");
        schema.put("attr.modify_time", "VARCHAR");
        schema.put("attr.community_id", "VARCHAR");
        schema.put("attr.community_name", "VARCHAR");
        schema.put("attr.control_community_id", "VARCHAR");
        schema.put("attr.control_person_id", "VARCHAR");
        schema.put("attr.control_event_id", "VARCHAR");
        // schema.put("attr.image_id", "VARCHAR"); // lq-add for sensetime
        schema.put("attr.personlib_type", "unsigned_int");
    }

    /**
     * 获取表结构
     */
    public HashMap<String, String> getSchema() {
        return schema;
    }

    /**
     * @return 获取单例
     */
    // public static synchronized PersonListClient getInstance() {
    // return new PersonListClient();
    // }

    private static class SingletonHolder {
        private static PersonListClient instance = new PersonListClient();
    }

    public static PersonListClient getInstance() {
        return SingletonHolder.instance;
    }

    public String getTableName() {
        return tableName;
    }

    // public void close() {
    // if (producer != null) {
    // producer.close();
    // }
    // }

}

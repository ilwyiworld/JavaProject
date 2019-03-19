package com.znv.fss.phoenix;

import com.alibaba.fastjson.JSONObject;
import com.znv.fss.phoenix.PersonListClient;
import com.znv.fss.common.VConstants;
import com.znv.fss.common.errorcode.FssErrorCodeEnum;
import com.znv.fss.conf.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ct on 2017/5/25.
 */
public final class RelationShipClient extends ClientBase {
    // 单例
    private static RelationShipClient relationShipClient = null;
    private static final Logger L = LoggerFactory.getLogger(RelationShipClient.class);
    private String tableName = ConfigManager.getTableName(VConstants.FSS_RELATIONSHIP_V113_TABLE_NAME);
    private static final String PERSONID = "person_id";
    private static final String RELATIONID = "relation_id";
    private static final String LIBID = "lib_id";
    private static final String BIRTH = "birth";
    private static final String RELATIONLIBID = "relation_lib_id";
    private static final String ISDEL = "is_del";

    // 表结构
    private static HashMap<String, String> schema = new HashMap();
    {
        schema.put("person_id", "VARCHAR not null");
        schema.put("relation_id", "VARCHAR not null");
        schema.put("statData.relation_lib_id", "unsigned_int");
        schema.put("statData.relation_type", "unsigned_int");
        schema.put("statData.relation_grade", "unsigned_int");
        schema.put("statData.is_del", "unsigned_int");
    }

    /**
     * 构造函数
     */
    private RelationShipClient() {

    }

    /**
     * 获取单例
     */
    // public static synchronized RelationShipClient getInstance() {
    // return new RelationShipClient();
    // }

    private static class RelationShipSingletonHolder {
        private static RelationShipClient instance = new RelationShipClient();
    }

    public static RelationShipClient getInstance() {
        return RelationShipSingletonHolder.instance;
    }

    /**
     * @return
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * 添加数据
     * @param data 数据信息，人员关系表中所有字段
     * @param phoenixConn phoenix连接
     * @return JSONObject 结果
     */
    public JSONObject insert(JSONObject data, Connection phoenixConn) {
        JSONObject result = new JSONObject();
        result.put("errorCode", FssErrorCodeEnum.SUCCESS.getCode());
        if (phoenixConn == null) {
            result.put("errorCode", FssErrorCodeEnum.PHOENIX_CONN_NULL.getCode());
            L.error("insert error, phoenixConn is null");
        } else if (data.containsKey(PERSONID) && !data.getString(PERSONID).isEmpty() && data.containsKey(RELATIONID)
            && !data.getString(RELATIONID).isEmpty() && !data.getString(PERSONID).equals(data.getString(RELATIONID))) {
            // person_id 不能等于 relation_id
            try {
                // 需判断用户id和关系人id是否存在，以及关系人是否为小孩(小孩不允许被设置为关系人)
                String sql = "select lib_id,birth from " + PersonListClient.getInstance().getTableName()
                    + " where is_del = '0' and person_id = '";
                // 先判断用户是否存在
                String personSql = sql + data.getString(PERSONID) + "'";
                JSONObject rsPerson = super.query(personSql, phoenixConn);
                List<JSONObject> personData = (List<JSONObject>) rsPerson.get("data");
                if (!personData.isEmpty()) {
                    // 判断关系人是否存在，存在的话一并判断关系人类型，这里不考虑一个id查询出多条记录的情况
                    String relationSql = sql + data.getString(RELATIONID) + "'";
                    JSONObject rsRelation = super.query(relationSql, phoenixConn);
                    List<JSONObject> relationData = (List<JSONObject>) rsRelation.get("data");
                    if (!relationData.isEmpty()) {
                        JSONObject relationObj = relationData.get(0);
                        // 判断关系人是否为小孩，通过生日判断
                        String relationBirth = relationObj.getString(BIRTH);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        long childUnit = 12 * (365 * 24 * 60 * 60 * 1000L);
                        long longBirth = sdf.parse(relationBirth).getTime();
                        long nowTime = System.currentTimeMillis();
                        if ((nowTime - longBirth) >= childUnit) {
                            // 根据查询出来的子库类型，更新lib_id信息
                            data.put(RELATIONLIBID, relationObj.getIntValue(LIBID));

                            // 获取personId + relationId
                            String personId = data.getString(PERSONID);
                            data.put(PERSONID, personId);
                            String relationId = data.getString(RELATIONID);
                            data.put(RELATIONID, relationId);
                            data.put(ISDEL, "0"); // 默认不删除

                            // 数据插入关系表
                            result = super.insert(data, tableName, phoenixConn);
                        } else {
                            // result.put("errorCode", "relationId_isChild");
                            result.put("errorCode", FssErrorCodeEnum.PHOENIX_RELATIONID_ISCHILD.getCode());
                        }
                    } else {
                        // result.put("errorCode", "relationId_notExist");
                        result.put("errorCode", FssErrorCodeEnum.PHOENIX_RELATIONID_NOT_EXIST.getCode());
                    }
                } else {
                    // result.put("errorCode", "personId_notExist");
                    result.put("errorCode", FssErrorCodeEnum.PHOENIX_PERSONID_NOT_EXIST.getCode());
                }
            } catch (Exception e) {
                // result.put("errorCode", "sysError");
                result.put("errorCode", FssErrorCodeEnum.PHOENIX_INVALID_SQL.getCode());
                L.error("error occur when inserting relation data", e);
            }
        } else {
            result.put("errorCode", FssErrorCodeEnum.PHOENIX_INVALID_PARAM.getCode());
        }

        return result;
    }

    /**
     * 删除数据
     * @param data 要删除数据的主键，包括lig_id,sub_type,person_id
     * @param phoenixConn phoenix连接
     * @return JSONObject 结果
     */
    public JSONObject delete(JSONObject data, Connection phoenixConn) {
        JSONObject result = new JSONObject();
        if (data.containsKey(PERSONID) && !data.getString(PERSONID).isEmpty() && data.containsKey(RELATIONID)
            && !data.getString(RELATIONID).isEmpty()) {
            // 该条记录标记为删除
            String personId = data.getString(PERSONID);
            String relationId = data.getString(RELATIONID);
            String sql = String.format("upsert into %s (%s,%s,%s) values ('%s','%s','%s')", tableName, PERSONID,
                RELATIONID, ISDEL, personId, relationId, "1");
            try {
                executeSql(sql, phoenixConn);
                result.put("errorCode", FssErrorCodeEnum.SUCCESS.getCode());
            } catch (Exception e) {
                result.put("errorCode", FssErrorCodeEnum.PHOENIX_INVALID_SQL.getCode());
                L.error("error occur when deleting relation data ", e);
            }
        } else {
            result.put("errorCode", FssErrorCodeEnum.PHOENIX_INVALID_PARAM.getCode());
        }
        return result;
    }

    /**
     * 名单库结构化查询
     * @param query 查询协议,单值查询条件中一定要有person_id（减少查询的运算量）
     * @param phoenixConn phoenix连接
     * @return JSONObject
     * @throws Exception 异常
     */
    public JSONObject query(JSONObject query, Connection phoenixConn/* , boolean checkDel */) throws Exception {
        JSONObject result = new JSONObject();
        result.put("errorCode", FssErrorCodeEnum.SUCCESS.getCode());
        if (phoenixConn == null) {
            result.put("errorCode", FssErrorCodeEnum.PHOENIX_CONN_NULL.getCode());
            L.error("query error, phoenixConn is null");
        } else if (query.containsKey("count") && query.containsKey("total_page") && query.containsKey("page_no")
            && query.containsKey("page_size") && null != query.getInteger("count")
            && null != query.getInteger("total_page") && null != query.getInteger("page_no")
            && null != query.getInteger("page_size")) {
            List<JSONObject> objList = new ArrayList<>();
            long t1 = System.currentTimeMillis();

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

            JSONObject total = new JSONObject();

            if (count == -1 && totalPage == -1) {
                total = getCountAndPage(query, phoenixConn/* , checkDel */);
                if ("success".equals(FssErrorCodeEnum.getExplanationByCode(total.getInteger("errorCode")))) {

                    count = total.getIntValue("count");
                    totalPage = total.getIntValue("total_page");
                    // L.info("query relationship ,count: {},total page: {}", count, totalPage);

                } else {
                    result.put("errorCode", total.getInteger("errorCode"));
                    // L.info("query relationship illegalParam");
                }
            }

            if (count == -1 && totalPage == -1) {
                // 查询参数异常
                result.put("errorCode", FssErrorCodeEnum.PHOENIX_INVALID_PARAM.getCode());
                result.put("data", objList);
                result.put("time", System.currentTimeMillis() - t1);
                result.put("count", count);
                result.put("total_page", totalPage);
            } else if (count == 0) {
                // 没有符合条件的记录，直接返回
                // result.put("errorCode", "success");
                result.put("data", objList);
                result.put("time", System.currentTimeMillis() - t1);
                result.put("count", count);
                result.put("total_page", totalPage);
            } else {
                int pageNo = query.getIntValue("page_no");
                int pageSize = query.getIntValue("page_size");

                // 单值查询条件
                JSONObject queryTerm = null;
                if (query.containsKey("query_term")) {
                    queryTerm = query.getJSONObject("query_term");
                }

                boolean isSearch = false;

                if (queryTerm != null) {

                    isSearch = true;

                }
                if (isSearch) {
                    // 组获取lib_id、person_id的查询条件
                    boolean isAddAnd = false;
                    String personListTable = PersonListClient.getInstance().getTableName();
                    StringBuilder tempSql = new StringBuilder();
                    tempSql.append("select pp.person_id as person_id,pr.relation_id as relation_id,")
                        .append("pr.relation_type as relation_type,pr.relation_grade as relation_grade,")
                        .append("pp.lib_id as lib_id,pr.relation_lib_id as relation_lib_id from ")
                        .append(personListTable).append(" pp,").append(tableName).append(" pr")
                        .append(" where pp.person_ID = pr.person_ID ").append(" and pr.is_del = '0'");

                    isAddAnd = true;

                    // 单值查询条件
                    if (queryTerm != null) {
                        boolean isDeleteAnd = false;
                        if (isAddAnd && queryTerm.size() != 0) {
                            tempSql.append(" AND ");
                        }
                        // 不支持图片查询
                        for (Object key : queryTerm.keySet()) {
                            if (!key.toString().equals("sim") && !key.toString().equals("feature")) {
                                tempSql.append(" pp.").append(key.toString() + " = ? AND");
                                isAddAnd = true;
                                isDeleteAnd = true;
                            }
                        }
                        if (isDeleteAnd) {
                            int index = tempSql.lastIndexOf("AND");
                            if (index != -1) {
                                tempSql.delete(index, tempSql.length());
                            }
                        }
                    }

                    tempSql.append(" order by pp.person_ID asc limit ").append(pageSize);

                    if (pageNo != 1) {
                        tempSql.append(" offset ").append((pageNo - 1) * pageSize);
                    }

                    List<JSONObject> tempResult = new ArrayList<JSONObject>();
                    int i = 1;
                    ResultSet rs = null;
                    try (PreparedStatement stat = phoenixConn.prepareStatement(tempSql.toString())) {
                        // 单值查询条件
                        if (queryTerm != null) {
                            for (Object key : queryTerm.keySet()) {
                                if (!key.toString().equals("sim") && !key.toString().equals("feature")) {
                                    stat.setObject(i, queryTerm.get(key));
                                    i++;
                                }
                            }
                        }

                        rs = stat.executeQuery();
                        while (rs.next()) {
                            JSONObject record = new JSONObject();
                            ResultSetMetaData rsMetaData = rs.getMetaData();
                            int columnCount = rsMetaData.getColumnCount();
                            for (int column = 0; column < columnCount; column++) {
                                String field = rsMetaData.getColumnLabel(column + 1);
                                record.put(field.toLowerCase(), rs.getObject(field));
                            }
                            tempResult.add(record);

                        }
                    } catch (Exception e) {
                        result.put("errorCode", FssErrorCodeEnum.PHOENIX_INVALID_SQL.getCode());
                        L.error("query relationship error {}", e);
                    }finally {
                        if (rs != null){
                            try {
                                rs.close();
                            }catch (Exception e){
                                L.error("query relationship release resource error ", e);
                            }
                        }
                    }

                    // 获取名单库人员信息相关字段
                    if (tempResult.size() > 0) {
                        // 组查询语句
                        StringBuilder personSql = new StringBuilder();

                        // 被查询人在名单库中的字段
                        personSql.append("select "); // 指定join的查询方式为sort-merge-join
                        for (String key : PersonListClient.getInstance().getSchema().keySet()) {
                            // 去掉key中的列族信息
                            if (key.contains(".")) {
                                key = key.split("\\.")[1];
                            }
                            personSql.append("personP.").append(key).append(" as ").append(key).append(",");
                        }

                        // 关系人相关信息
                        for (String key : PersonListClient.getInstance().getSchema().keySet()) {
                            // 去掉key中的列族信息
                            if (key.contains(".")) {
                                key = key.split("\\.")[1];
                            }
                            personSql.append("personR.").append(key).append(" as relation_").append(key).append(",");
                        }
                        int index = personSql.lastIndexOf(",");
                        if (index != -1) {
                            personSql.delete(index, personSql.length());
                        }
                        personSql.append(" from ").append(personListTable).append(" personP,").append(personListTable)
                            .append(" personR");
                        String personSqlStr = personSql.toString();
                        for (JSONObject obj : tempResult) {
                            String personId = obj.getString(PERSONID);
                            int libId = obj.getInteger(LIBID);
                            String relationPersonId = obj.getString(RELATIONID);
                            int relationLibId = obj.getInteger(RELATIONLIBID);
                            StringBuilder infoSql = new StringBuilder(); // 组过滤条件
                            infoSql.append(" where personP.lib_id = ").append(libId)
                                .append(" and personP.person_id = '").append(personId).append("'").append(" and")
                                .append(" personR.lib_id =").append(relationLibId).append(" and")
                                .append(" personR.person_id = '").append(relationPersonId).append("'");

                            String querySql = personSqlStr + infoSql.toString();
                            // 组合查询结果
                            JSONObject totalResult = new JSONObject();
                            List<JSONObject> personRes = super.query(querySql, phoenixConn).getObject("data",
                                List.class);

                            if (personRes.size() > 0) {
                                // 按照lib_id + person_id 查询只有一条记录
                                totalResult.putAll(personRes.get(0));
                            }

                            // 组人员关系relation_type、relation_grade 信息
                            if (obj.containsKey("relation_type") && null != obj.getInteger("relation_type")) {
                                totalResult.put("relation_type", obj.getInteger("relation_type"));
                            }
                            if (obj.containsKey("relation_grade") && null != obj.getInteger("relation_grade")) {
                                totalResult.put("relation_grade", obj.getInteger("relation_grade"));
                            }

                            objList.add(totalResult);
                        }
                    }

                    // result.put("errorCode", "success");
                    result.put("data", objList);
                    result.put("time", System.currentTimeMillis() - t1);
                    result.put("total_page", totalPage);
                    result.put("count", count);
                } else if (!isSearch) {
                    result.put("errorCode", FssErrorCodeEnum.PHOENIX_INVALID_PARAM.getCode());

                }
            }
        } else {
            result.put("errorCode", FssErrorCodeEnum.PHOENIX_INVALID_PARAM.getCode());
        }
        return result;
    }

    /**
     * @param query 查询条件,不支持按图片查询 获取查询结果的总条数和总页数
     * @param phoenixConn phoenix连接
     * @return JSONObject，包含总条数和页数
     * @throws Exception
     */
    private JSONObject getCountAndPage(JSONObject query, Connection phoenixConn) throws Exception {
        StringBuilder sql = new StringBuilder();
        JSONObject result = new JSONObject();

        // 单值查询条件
        JSONObject queryTerm = null;
        if (query.containsKey("query_term")) {
            queryTerm = query.getJSONObject("query_term");
        }

        boolean isSearch = false;
        if (queryTerm != null) {
            isSearch = true;
        }

        if (isSearch) {

            String personListTable = PersonListClient.getInstance().getTableName();
            sql.append("select count(1) from ").append(personListTable).append(" pp,").append(tableName)
                .append(" pr where pp.person_id = pr.person_id and pr.is_del = '0' ");

            boolean isAddAnd = true;
            // 单值查询条件
            if (queryTerm != null && isAddAnd) {
                sql.append(" and ");
                boolean isDeleteAnd = false;
                for (Object key : queryTerm.keySet()) {
                    if (!key.toString().equals("sim") && !key.toString().equals("feature")) {
                        sql.append(" pp.").append(key.toString() + " = ? AND");
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

            int i = 1;
            ResultSet rs = null;
            try (PreparedStatement stat = phoenixConn.prepareStatement(sql.toString())) {
                // 单值查询条件
                if (queryTerm != null) {
                    for (Object key : queryTerm.keySet()) {
                        if (!key.toString().equals("sim") && !key.toString().equals("feature")) {
                            stat.setObject(i, queryTerm.get(key));
                            i++;
                        }
                    }
                }

                rs = stat.executeQuery();
                // 每页显示的条数
                int pageSize = query.getIntValue("page_size");
                while (rs.next()) {
                    int count = rs.getInt(1);
                    result.put("count", count);
                    result.put("total_page", count % pageSize == 0 ? count / pageSize : count / pageSize + 1);
                    result.put("errorCode", FssErrorCodeEnum.SUCCESS.getCode());
                }
            } catch (Exception e) {
                result.put("errorCode", FssErrorCodeEnum.PHOENIX_INVALID_SQL.getCode());
                L.error("getCountAndPage error ", e);
            }finally {
                if (rs != null){
                    try {
                        rs.close();
                    }catch (Exception e){
                        L.error("getCountAndPage release resource error ", e);
                    }
                }
            }
        } else {
            result.put("errorCode", FssErrorCodeEnum.PHOENIX_INVALID_PARAM.getCode());
        }
        return result;
    }

}

package com.znv.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

/**
 * Created by Administrator on 2018/6/1.
 */
public class PhoenixUtil {

    private static final Logger L = LogManager.getLogger(PhoenixUtil.class.getName());

    public static JSONObject getHistoryPicture(String uuid, Connection phoenixConn) {
        JSONObject result = new JSONObject();
        result.put("errorCode", FssErrorCodeEnum.SUCCESS.getCode());
        if (phoenixConn == null) {
            result.put("errorCode", FssErrorCodeEnum.PHOENIX_CONN_NULL.getCode());
            L.error(String.format("error occur when getHistoryPicture %s data, phoenixConn is null"), "FSS_V1_1.FSS_BIG_PICTURE");
        } else if (!uuid.isEmpty()) {
            String sql = String.format(" select rt_image_data from %s where", "FSS_V1_1.FSS_BIG_PICTURE");
            sql = sql + String.format(" uuid = '%s'", uuid);

            try (PreparedStatement stat = phoenixConn.prepareStatement(sql)) {
                ResultSet rs = stat.executeQuery();
                while (rs.next()) {
                    result.put("rt_image_data", rs.getBytes("rt_image_data"));
                }
            } catch (SQLException e) {
                result.put("errorCode", FssErrorCodeEnum.PHOENIX_INVALID_SQL.getCode());
                L.error("error occur when get history picture", e);
            }
        } else {
            result.put("errorCode", FssErrorCodeEnum.PHOENIX_INVALID_PARAM.getCode());
            L.error(" rowkey not only when get history picture");

        }
        return result;
    }
}

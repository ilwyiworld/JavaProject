package Write10000W.rewritetable.hbasetophoenix;

import Write10000W.rewritetable.hbasetohbase.MultiWrite2HbaseFromHbase;
import Write10000W.util.ConnectionPool;
import Write10000W.util.HBaseConfig1;
import Write10000W.util.PictureUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.sql.*;
import java.sql.Connection;
import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * Created by zhuhx on 2018/4/18.
 */
public class RewritePersonListFromHBase2Phoenix extends Thread {
    private CountDownLatch threadSignal;
    private Scan scan;
    private String tableName;
    private String targetTableName;
    private int targetSaltBuckets;
    private String faceUrl;
    private ConnectionPool connectionPool;

    public RewritePersonListFromHBase2Phoenix(CountDownLatch threadSignal, Scan scan, String tableNmae, String targetTableName, int targetSaltBuckets, String faceUrl, ConnectionPool connPool) {
        this.threadSignal = threadSignal;
        this.scan = scan;
        this.tableName = tableNmae;
        this.targetTableName = targetTableName;
        this.targetSaltBuckets = targetSaltBuckets;
        this.faceUrl = faceUrl;
        this.connectionPool = connPool;
    }

    @Override
    public void run() {
        rewritePersonListHBase2Phoenix();
        threadSignal.countDown();
    }

    /**
     * 重写名单库从hbase到phoenix
     */
    private void rewritePersonListHBase2Phoenix() {
        ResultScanner rs = null;
        HTable table = null;
        HTable targetTable = null;

        //分隔符
        byte[] x00 = new byte[1];
        x00[0] = (byte) 0x00;

        //盐值
        byte[] salt = new byte[1];


        try {
            table = HBaseConfig1.getTable(tableName);
            targetTable = HBaseConfig1.getTable(targetTableName);
            targetTable.setAutoFlush(false, false);
            rs = table.getScanner(scan);

            //查询结果重写
            HashMap<String, Result> featureMap = new HashMap<>();
            List<Put> putList = new ArrayList<>();
            for (Result r : rs) {
                String rowkey = Base64.encodeBase64String(r.getRow());
                featureMap.put(rowkey, r);
                if (featureMap.size() == 30) {
                    //提交一批数据
                    if (!putBatchPhoenix(featureMap, faceUrl, salt, putList, targetTable)) {
                        continue;
                    }
                }
            }
            //处理最后不够30条的数据
            if (featureMap.size() > 0) {
                putBatchPhoenix(featureMap, faceUrl, salt, putList, targetTable);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            rs.close();
            try {
                table.close();
                targetTable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    /**
     * phoenix写名单库
     *
     * @param featureMap
     * @param faceUrl
     * @param salt
     * @param putList
     * @param targetTable
     * @return
     */
    private boolean putBatchPhoenix(HashMap<String, Result> featureMap, String faceUrl, byte[] salt, List<Put> putList, HTable targetTable) {

        //每30条数据获取一次特征
        String stResult = PictureUtils.getFeatureBatch(featureMap, faceUrl);
        JSONObject resultJSON = JSON.parseObject(stResult);
        if (!"success".equalsIgnoreCase(resultJSON.getString("result"))) {
            // 请求商汤服务器人脸特征值提取失败
            System.out.println(" 商汤获取特征失败！");
            featureMap.clear();
            return false;
        }

        //获取特征成功的列
        JSONArray resultArray = resultJSON.getJSONArray("success");
        for (int i = 0; i < resultArray.size(); i++) {
            //写phoenix
            JSONObject jsonObject = resultArray.getJSONObject(i);

            //从商汤返回结果中获取一个的特征
            byte[] feature = Base64.decodeBase64(jsonObject.getString("feature"));

            //从商汤返回结果中获取当前特征对应的rowkey
            String row = jsonObject.getString("name");

            //从数据列表中取出对应的行，重组该行的数据写入新表
            Result rScanner = featureMap.get(row);

            //row--盐值+libId+分隔符+personId
            byte[] rowSource = rScanner.getRow();
            int libId = Bytes.toInt(Bytes.copy(rowSource, 1, 4));
            String personId = Bytes.toString(Bytes.copy(rowSource, 6, rowSource.length - 5));

            JSONObject data = new JSONObject();
            data.put("lib", libId);
            data.put("person_id", personId);


            for (Cell cell : rScanner.rawCells()) {
                String col = Bytes.toString(CellUtil.cloneQualifier(cell)).toLowerCase();
                String value = Bytes.toString(CellUtil.cloneValue(cell));
                //特征列重写
                if (col.equals("feature")) {
                    data.put("feature", feature);
                } else if (col.equals("feature2")) {
                    data.put("feature", feature);
                } else if (col.equals("feature3")) {
                    data.put("feature", feature);
                } else if (col.equals("person_name")) {
                    data.put("person_name", value);
                } else if (col.equals("birth")) {
                    data.put("birth", value);
                } else if (col.equals("nation")) {
                    data.put("nation", value);
                } else if (col.equals("country")) {
                    data.put("county", value);
                } else if (col.equals("positive_url")) {
                    data.put("positive_url", value);
                } else if (col.equals("negative_url")) {
                    data.put("negative_url", value);
                } else if (col.equals("addr")) {
                    data.put("addr", value);
                } else if (col.equals("tel")) {
                    data.put("tel", value);
                } else if (col.equals("nature_residence")) {
                    data.put("nature_residence", value);
                } else if (col.equals("room_number")) {
                    data.put("room_number", value);
                } else if (col.equals("door_open")) {
                    data.put("door_open", Bytes.toInt(CellUtil.cloneValue(cell)));
                } else if (col.equals("sex")) {
                    data.put("sex", Bytes.toInt(CellUtil.cloneValue(cell)));
                } else if (col.equals("image_name")) {
                    data.put("image_name", value);
                } else if (col.equals("person_img")) {
                    data.put("person_img", CellUtil.cloneValue(cell));
                } else if (col.equals("person_img2")) {
                    data.put("person_img2", CellUtil.cloneValue(cell));
                } else if (col.equals("person_img3")) {
                    data.put("person_img3", CellUtil.cloneValue(cell));
                } else if (col.equals("card_id")) {
                    data.put("card_id", value);
                } else if (col.equals("flag")) {
                    data.put("flag", Bytes.toInt(CellUtil.cloneValue(cell)));
                } else if (col.equals("comment")) {
                    data.put("comment", value);
                } else if (col.equals("control_start_time")) {
                    data.put("control_start_time", value);
                } else if (col.equals("control_end_time")) {
                    data.put("control_end_time", value);
                } else if (col.equals("is_del")) {
                    data.put("is_del", value);
                } else if (col.equals("create_time")) {
                    data.put("create_time", value);
                } else if (col.equals("modify_time")) {
                    data.put("modify_time", value);
                } else if (col.equals("community_id")) {
                    data.put("community_id", value);
                } else if (col.equals("community_name")) {
                    data.put("community_name", value);
                } else if (col.equals("control_community_id")) {
                    data.put("control_community_id", value);
                } else if (col.equals("control_person_id")) {
                    data.put("control_person_id", value);
                } else if (col.equals("control_event_id")) {
                    data.put("control_event_id", value);
                } else if (col.equals("image_id")) {
                    data.put("image_id", value);
                } else if (col.equals("personlib_type")) {
                    data.put("personlib_type", Bytes.toInt(CellUtil.cloneValue(cell)));
                }
            }
            java.sql.Connection conn = null;
            try {
                conn = connectionPool.getConnection();
                upsert2Phoenix(data, targetTableName, conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            connectionPool.returnConnection(conn);

            int totalNum = MultiWrite2HbaseFromHbase.Count.addAndGet(putList.size());
            if (totalNum % 900 == 0) {
                System.out.println("data number: " + totalNum);
            }
            //todo 获取特征失败的列
            featureMap.clear();
            putList.clear();

        }
        return true;
    }


    public static synchronized void upsert2Phoenix(JSONObject obj, String tableName, Connection conn) {
        StringBuilder insertSql = new StringBuilder();
        insertSql.append("UPSERT INTO ").append(tableName).append("(");
        for (String keys : obj.keySet()) {
            insertSql.append(keys + ",");
        }
        if (insertSql.charAt(insertSql.length() - 1) == ',') {
            insertSql.deleteCharAt(insertSql.length() - 1);
        }
        insertSql.append(") VALUES(");
        for (@SuppressWarnings("unused")
                String keys : obj.keySet()) {
            insertSql.append("?,");
        }
        if (insertSql.charAt(insertSql.length() - 1) == ',') {
            insertSql.deleteCharAt(insertSql.length() - 1);
        }
        insertSql.append(")");

        int i = 1;
        // Connection conn = PhoenixUtils.getPhoenixConnection(phoenixConnUrl);
        try (PreparedStatement stat = conn.prepareStatement(insertSql.toString())) {
            Iterator<Map.Entry<String, Object>> iterator = obj.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Object> entry = iterator.next();
                stat.setObject(i, entry.getValue());
                i++;
            }
            stat.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}

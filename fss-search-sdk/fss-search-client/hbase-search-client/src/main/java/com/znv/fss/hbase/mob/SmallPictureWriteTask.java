package com.znv.fss.hbase.mob;

import com.alibaba.fastjson.JSON;
import com.znv.fss.common.VConstants;
import com.znv.fss.hbase.HBaseConfig;
import com.znv.fss.hbase.HBaseManager;
import com.znv.fss.hbase.JsonResultType;
import com.znv.fss.hbase.MultiHBaseSearch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.phoenix.schema.SaltingUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/12/4.
 */
public class SmallPictureWriteTask extends MultiHBaseSearch {

    private static final Log LOG = LogFactory.getLog(SmallPictureWriteTask.class);
    private static final String SCHEMA_NAME = HBaseConfig.getProperty(VConstants.FSS_PHOENIX_SCHEMA_NAME);
    private static final String MOB_TABLE_NAME = SCHEMA_NAME + ":"
        + HBaseConfig.getProperty(VConstants.FSS_BIGPICTURE_V113_TABLE_NAME);
    private static final int SALT_BUCKETS = Integer.parseInt(HBaseConfig.getProperty(VConstants.PIC_SALT_BUCKETS));

    // private static final String MOB_TABLE_NAME = "FSS_DEVELOP_410:FSS_BIG_PICTURE_V1_1_3_20170727_2";

    public SmallPictureWriteTask() {
        super("write");
    }

    // 解析输入参数
    /**
     * 获取查询结果
     */
    @Override
    public String requestSearch(String jsonParamStr) throws Exception {
        String jsonstr = "";
        long t1 = System.currentTimeMillis(); // 查询统计开始时间

        MOBWriteJsonInput inputParam = JSON.parseObject(jsonParamStr, MOBWriteJsonInput.class);
        MOBWriteReportServiceIn service = inputParam.getReportservice();
        HBaseManager.SearchId id = HBaseManager.SearchId.getSearchId(service.getId());
        String type = service.getType();
        MOBWriteParam param = new MOBWriteParam();

        if (id.equals(HBaseManager.SearchId.WriteSmallPicture) && type.equals("request")) {
            param = service.getMobWriteParam();
            if (param == null) {
                throw new IOException("SmallPictureWrite Exception: Param is null");
            } else if (param.getData().size() > 0) {
                // 组写入数据
                List<Put> puts = new ArrayList<Put>();
                List<MOBInputData> insertDatas = param.getData();
                int len = insertDatas.size();
                for (int i = 0; i < len; i++) {

                    String key = insertDatas.get(i).getUuid();
                    byte[] imageData = insertDatas.get(i).getImageData();

                    byte[] salt2 = new byte[1];

                    // hash加盐方式——用sql语句可以查询
                    // picsl.rt_image_data
                    byte[] byteKey = Bytes.toBytes(key);
                    salt2[0] = SaltingUtil.getSaltingByte(byteKey, 0, byteKey.length, SALT_BUCKETS);
                    Put put = new Put(Bytes.add(salt2, byteKey));
                    // put.add(Bytes.toBytes("PICSL"), Bytes.toBytes("_0"), Bytes.toBytes("x")); // 统计行数使用
                    put.add(Bytes.toBytes("PICS"), Bytes.toBytes("_0"), Bytes.toBytes("x")); // 统计行数使用,需要写建标语句中的第一个列族
                    put.add(Bytes.toBytes("PICSL"), Bytes.toBytes("RT_IMAGE_DATA"), imageData);
                    puts.add(put);
                }

                // htable puts
                Connection conn = HBaseConfig.getConnectionFromPool();
                HTable htable = (HTable) conn.getTable(TableName.valueOf(MOB_TABLE_NAME));
                htable.setAutoFlushTo(true);

                try {
                    htable.put((List<Put>) puts);
                } catch (Exception e) {
                    LOG.info("htable put Exception! ", e);
                } finally {
                    HBaseConfig.returnPoolConnection(conn);
                    htable.close();
                }

                MOBWriteReportServiceOut result = new MOBWriteReportServiceOut();
                result.setId(service.getId());
                result.setType(type);
                result.setTime(System.currentTimeMillis() - t1);
                result.setErrorcode(JsonResultType.SUCCESS);

                MOBWriteJsonOutput out = new MOBWriteJsonOutput();
                out.setReportService(result);
                Object jsonObject = JSON.toJSON(out);
                jsonstr = JSON.toJSONString(jsonObject);
            } else { // 没有需要插入的数据
                MOBWriteReportServiceOut result = new MOBWriteReportServiceOut();
                result.setId(service.getId());
                result.setType(type);
                result.setTime(System.currentTimeMillis() - t1);
                result.setErrorcode(JsonResultType.SUCCESS);

                MOBWriteJsonOutput out = new MOBWriteJsonOutput();
                out.setReportService(result);
                Object jsonObject = JSON.toJSON(out);
                jsonstr = JSON.toJSONString(jsonObject);
            }
        } else { // 参数异常
            MOBWriteReportServiceOut result = new MOBWriteReportServiceOut();
            result.setId(service.getId());
            result.setType(type);
            result.setTime(System.currentTimeMillis() - t1);
            result.setErrorcode(JsonResultType.ERROR);

            MOBWriteJsonOutput out = new MOBWriteJsonOutput();
            out.setReportService(result);
            Object jsonObject = JSON.toJSON(out);
            jsonstr = JSON.toJSONString(jsonObject);
        }
        return jsonstr;
    }
}

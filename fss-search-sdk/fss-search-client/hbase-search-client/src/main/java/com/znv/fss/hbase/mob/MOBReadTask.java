package com.znv.fss.hbase.mob;

import com.alibaba.fastjson.JSONObject;
import com.sun.istack.Nullable;
import com.znv.fss.common.VConstants;
import com.znv.fss.common.errorcode.FssErrorCodeEnum;
import com.znv.fss.hbase.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.phoenix.schema.SaltingUtil;

import java.io.IOException;

public class MOBReadTask extends MultiHBaseSearch {
    private static final String schemaName = HBaseConfig.getProperty(VConstants.FSS_PHOENIX_SCHEMA_NAME);
    private static final String picTableName = HBaseConfig.getProperty(VConstants.FSS_BIGPICTURE_V113_TABLE_NAME);
    private static final int saltBuckets = Integer.parseInt(HBaseConfig.getProperty(VConstants.PIC_SALT_BUCKETS));
    private final Log LOG = LogFactory.getLog(MOBReadTask.class);
    private final byte[] bigPicCol = Bytes.toBytes("IMAGE_DATA");
    private final byte[] bigPicCF = Bytes.toBytes("PICS");
    private final byte[] smallPicCol = Bytes.toBytes("RT_IMAGE_DATA");
    private final byte[] smallPicCF = Bytes.toBytes("PICSL");

    public MOBReadTask() {
        super("read");
    }

    @Override
    public JSONObject requestSearch(JSONObject jsonParamObj) throws Exception {
        JSONObject service = jsonParamObj.getJSONObject("reportService");
        String uuid = service.getString("uuid");
        String type = service.getString("picType");//big or small
        int errcode = FssErrorCodeEnum.SUCCESS.getCode();
        if (StringUtils.isEmpty(uuid)) {
            errcode = FssErrorCodeEnum.HBASE_INVALID_PARAM.getCode();
            LOG.info("please input uuid! params：" + jsonParamObj);
        }
        if (StringUtils.isEmpty(type) || (!type.equals("big") && !type.equals("small"))) {
            errcode = FssErrorCodeEnum.HBASE_INVALID_PARAM.getCode();
            LOG.info("please input uuid! params：" + jsonParamObj);
        }
        if (errcode != FssErrorCodeEnum.SUCCESS.getCode()) {
            return getErrorResult(errcode, null);
        }

        String tablename = schemaName + ":" + picTableName;
        byte[] picCol = null;
        byte[] picCF = null;
        if (type.equals("big")) {
            picCol = bigPicCol;
            picCF = bigPicCF;
        } else {
            picCol = smallPicCol;
            picCF = smallPicCF;
        }

        byte[] unsaltedRowKey = Bytes.toBytes(uuid);
        byte[] salt =  new byte[1];
        salt[0] = SaltingUtil.getSaltingByte(unsaltedRowKey, 0, unsaltedRowKey.length, saltBuckets);
        Get get = new Get(Bytes.add(salt, unsaltedRowKey));
        get.addColumn(picCF, picCol);

        HTable picTable = null;

        byte[] img = null;
        try {
            picTable = HBaseConfig.getTable(tablename);
            Result r = picTable.get(get);
            img = r.getValue(picCF, picCol);
        } catch (IOException e) {
            errcode = FssErrorCodeEnum.HBASE_GET_EXCEPTION.getCode();
            LOG.info(e);
        } catch (Exception e) {
            errcode = FssErrorCodeEnum.HBASE_GET_EXCEPTION.getCode();
            LOG.info(e);
        } finally {
            try {
                if (picTable != null) {
                    picTable.close();
                }
            } catch (IOException e) {
                LOG.info(e);
            }
        }

        return getErrorResult(errcode, img);
    }

    private JSONObject getErrorResult(int errCode, @Nullable byte[] img) {
        JSONObject ret = new JSONObject();
        ret.put("id", HBaseManager.SearchId.ReadMOB.getId());
        ret.put("type", "response");
        ret.put("errorCode", errCode);
        if (errCode == FssErrorCodeEnum.SUCCESS.getCode() && img != null) {
            ret.put("imageData", img); // 图片为二进制byte数组
            ret.put("count", 1);
        } else {
            ret.put("count", 0);
        }
        JSONObject result = new JSONObject();
        result.put("reportService", ret);

        return result;
    }

}

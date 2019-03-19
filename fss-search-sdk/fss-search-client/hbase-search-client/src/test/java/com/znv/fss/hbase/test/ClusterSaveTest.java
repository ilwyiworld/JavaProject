package com.znv.fss.hbase.test;

import com.znv.fss.hbase.HBaseConfig;
import com.znv.hbase.coprocessor.endpoint.staytimestat.StayTimeSearchOutData;
import org.apache.commons.codec.binary.Base64;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import sun.misc.BASE64Decoder;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/6/15.
 */
public class ClusterSaveTest {
    public static void peertest(List<StayTimeSearchOutData> peerlist, String historyTableName) {
        int i = 0;
        byte[] imagefam = Bytes.toBytes("PICS");
        byte[] imagequal = Bytes.toBytes("RT_IMAGE_DATA");
        try {
            Base64 base64 = new Base64();
            HTable table = HBaseConfig.getTable(historyTableName);
            for (StayTimeSearchOutData data : peerlist) {
                Get get = new Get(data.getRowKey());
                get.addColumn(imagefam, imagequal);
                get.addColumn(Bytes.toBytes("ATTR"), Bytes.toBytes("IMG_URL"));
                Result result = table.get(get);
                String imgurl[] = Bytes.toString(result.getValue(Bytes.toBytes("ATTR"), Bytes.toBytes("IMG_URL")))
                    .split("/");
                base642jpg(base64.encodeBase64String(result.getValue(imagefam, imagequal)), String
                    .format("E:\\项目\\10-大数据\\10-FSS\\test\\test-peer\\test-%s-%s.jpg", i++, imgurl[imgurl.length - 1]));
            }
            table.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void clustertest(Map<StayTimeSearchOutData, List<StayTimeSearchOutData>> peerClusterInfo, String path,
        String historyTableName) {
        int i = 0;
        byte[] imagefam = Bytes.toBytes("PICS");
        byte[] imagequal = Bytes.toBytes("RT_IMAGE_DATA");
        try {
            Base64 base64 = new Base64();
            HTable table = HBaseConfig.getTable(historyTableName);
            for (Map.Entry<StayTimeSearchOutData, List<StayTimeSearchOutData>> entry : peerClusterInfo.entrySet()) {
                i = 0;
                for (StayTimeSearchOutData data : entry.getValue()) {
                    int j = 0;
                    Get get = new Get(data.getRowKey());
                    get.addColumn(imagefam, imagequal);
                    get.addColumn(Bytes.toBytes("ATTR"), Bytes.toBytes("IMG_URL"));
                    Result result = table.get(get);
                    String imgurl[] = Bytes.toString(result.getValue(Bytes.toBytes("ATTR"), Bytes.toBytes("IMG_URL")))
                        .split("/");
                    base642jpg(base64.encodeBase64String(result.getValue(imagefam, imagequal)), String
                        .format("%s\\test-group%s-%s-%s.jpg", path, j++, i++, imgurl[imgurl.length - 1]));
                }
            }
            table.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static boolean base642jpg(String imgStr, String imgFilePath) {
        if (imgStr == null) {
            return false;
        }
        // base解码
        BASE64Decoder decoder = new BASE64Decoder();
        try {
            byte[] decodeBuffer = decoder.decodeBuffer(imgStr);
            // for (int i = 0; i < decodeBuffer.length; i++) {
            // //调整异常数据
            // if (decodeBuffer[i]<0) {
            // decodeBuffer[i]+=256;
            // }
            // }
            // 生成JPEG图片
            OutputStream out = new FileOutputStream(imgFilePath);
            out.write(decodeBuffer);
            out.flush();
            out.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }
}

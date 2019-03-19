package Write10000W.rewritetable.hbasetohbase;

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
import org.apache.phoenix.schema.SaltingUtil;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;


/**
 * Created by zhuhx on 2018/4/13.
 */
public class RewritePersonListFromHBase2HBase extends Thread {
    private CountDownLatch threadSignal;
    private Scan scan;
    private String tableName;
    private String targetTableName;
    private int targetSaltBuckets;
    private String faceUrl;
    private String faceUrlSingle;
    private int reTry;

    public RewritePersonListFromHBase2HBase(CountDownLatch threadSignal, Scan scan, String tableNmae, String targetTableName, int targetSaltBuckets, String faceUrl, String faceUrlSingle, int reTry) {
        this.threadSignal = threadSignal;
        this.scan = scan;
        this.tableName = tableNmae;
        this.targetTableName = targetTableName;
        this.targetSaltBuckets = targetSaltBuckets;
        this.faceUrl = faceUrl;
        this.faceUrlSingle = faceUrlSingle;
        this.reTry = reTry;
    }

    @Override
    public void run() {
        rewritePersonListHBase2HBase();
        threadSignal.countDown();
    }

    /**
     * 重写名单库从hbase到hbase
     */
    private void rewritePersonListHBase2HBase() {
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
            targetTable = HBaseConfig1.getTableTarget(targetTableName);
            //targetTable.setAutoFlush(false, false);
            rs = table.getScanner(scan);

            //查询结果重写
            HashMap<String, Result> featureMap = new HashMap<>();
            List<Put> putList = new ArrayList<>();
            for (Result r : rs) {
                String rowkey = Base64.encodeBase64String(r.getRow());
                featureMap.put(rowkey, r);
                if (featureMap.size() == 30) {
                    //提交一批数据
                    putBatch(featureMap, salt, putList, targetTable);
                }
            }
            //处理最后不够30条的数据
            if (featureMap.size() > 0) {
                putBatch(featureMap, salt, putList, targetTable);
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
     * 提交一批数据获取特征
     *
     * @param featureMap
     * @param salt
     * @param putList
     * @param targetTable
     * @return
     * @throws IOException
     */
    private void putBatch(HashMap<String, Result> featureMap, byte[] salt, List<Put> putList, HTable targetTable) throws IOException {
        int success = 0;
        int fail = 0;
        //每30条数据获取一次特征
        String stResult = PictureUtils.getFeatureBatch(featureMap, faceUrl);
        JSONObject resultJSON = JSON.parseObject(stResult);

        //批量获取失败，尝试单条获取，加入putList
        if (!"success".equalsIgnoreCase(resultJSON.getString("result"))) {
            // 请求商汤服务器人脸特征值提取失败
            MultiWrite2HbaseFromHbase.FailCount.addAndGet(featureMap.size());
            System.out.println(" 商汤获取特征失败！");
            for (Map.Entry<String, Result> entry : featureMap.entrySet()) {
                boolean flag = processFailFeature(entry.getValue(), salt, putList);
                success += flag ? 1 : 0;
                fail += flag ? 0 : 1;
            }
        } else {
            //批量获取特征成功的行
            JSONArray resultArraySuccess = resultJSON.getJSONArray("success");
            success += resultArraySuccess.size();
            for (int i = 0; i < resultArraySuccess.size(); i++) {
                JSONObject jsonObject = resultArraySuccess.getJSONObject(i);

                //从商汤返回结果中获取一个的特征
                byte[] feature = Base64.decodeBase64(jsonObject.getString("feature"));

                //从商汤返回结果中获取当前特征对应的rowkey
                String row = jsonObject.getString("name");

                //从数据列表中取出对应的行，重组该行的数据加入putList
                Result rScanner = featureMap.get(row);
                putData(feature, rScanner, salt, putList);
            }

            // 批量获取特征失败的行
            JSONArray resultArrayFail = resultJSON.getJSONArray("fail");
            for (int i = 0; i < resultArrayFail.size(); i++) {

                JSONObject jsonObject = resultArrayFail.getJSONObject(i);

                //从商汤返回结果中获取当前特征对应的rowkey
                String row = jsonObject.getString("name");

                //从数据列表中取出对应的行，重新获取特征，加入putList
                Result rScanner = featureMap.get(row);
                boolean flag = processFailFeature(rScanner, salt, putList);
                success += flag ? 1 : 0;
                fail += flag ? 0 : 1;
            }
        }
        targetTable.getRegionLocator();
        targetTable.getConfiguration();
        targetTable.put(putList);
        targetTable.flushCommits();
        MultiWrite2HbaseFromHbase.SuccessCount.addAndGet(success);
        MultiWrite2HbaseFromHbase.FailCount.addAndGet(fail);
        int totalNum = MultiWrite2HbaseFromHbase.Count.addAndGet(putList.size());
        if (totalNum % 9000 == 0) {
            System.out.println("data number: " + totalNum);
        }
        featureMap.clear();
        putList.clear();
    }

    /**
     * 获取特征失败的数据重试
     *
     * @param rScanner
     * @param salt
     * @param putList
     * @return
     * @throws IOException
     */
    private boolean processFailFeature(Result rScanner, byte[] salt, List<Put> putList) throws IOException {
        boolean flag = true;
        byte[] pic = rScanner.getValue(Bytes.toBytes("PICS"), Bytes.toBytes("PERSON_IMG"));
        byte[] feature = null;
        //单条获取特征
        for (int i = 0; i < reTry; i++) {
            String stResultString = PictureUtils.getFeatureByImage("a", pic, faceUrlSingle);
            JSONObject stJSON = JSON.parseObject(stResultString);

            if ("success".equals(stJSON.getString("result"))) {
                feature = Base64.decodeBase64(stJSON.getString("feature"));
                flag = true;
                break;
            } else {
                flag = false;
            }
        }

        if (!flag) {
            byte[] row = rScanner.getRow();
            int libId = Bytes.toInt(Bytes.copy(row, 1, 4));
            String personId = Bytes.toString(Bytes.copy(row, 5, row.length - 5));
            System.out.println(String.format("single get feautre failed, libid:%d personid:%s", libId, personId));
        }

        putData(feature, rScanner, salt, putList);
        return flag;
    }

    /**
     * 重组写入新表的数据
     *
     * @param feature
     * @param rScanner
     * @param salt
     * @param putList
     * @throws IOException
     */
    private void putData(byte[] feature, Result rScanner, byte[] salt, List<Put> putList) throws IOException {

        //row--盐值+libId+personId
        byte[] rowSource = rScanner.getRow();
        byte[] rowPrepare = Bytes.copy(rowSource, 1, rowSource.length - 1);

        //生成盐值的方式
        salt[0] = SaltingUtil.getSaltingByte(rowPrepare, 0, rowPrepare.length, targetSaltBuckets /*saltBuckets*/);

        //生成rowkey
        byte[] rowNew = Bytes.add(salt, rowPrepare);

        Put put = new Put(rowNew);

        for (Cell cell : rScanner.rawCells()) {
            String col = Bytes.toString(CellUtil.cloneQualifier(cell)).toLowerCase();
            Cell newCell;

            //特征列重写
            if (col.equals("feature") || col.equals("feature2") || col.equals("feature3")) {
                if (feature == null) {
                    continue;
                }

                newCell = CellUtil.createCell(rowNew, cell.getFamily(), cell.getQualifier(), cell.getTimestamp(), cell.getTypeByte(), feature);
                put.add(newCell);

            } else {
                newCell = CellUtil.createCell(rowNew, cell.getFamily(), cell.getQualifier(), cell.getTimestamp(), cell.getTypeByte(), CellUtil.cloneValue(cell));
                put.add(newCell);
            }
        }
        putList.add(put);
    }
}

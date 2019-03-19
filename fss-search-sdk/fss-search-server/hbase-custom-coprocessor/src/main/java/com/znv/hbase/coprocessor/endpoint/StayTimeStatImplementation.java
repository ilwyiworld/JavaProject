package com.znv.hbase.coprocessor.endpoint;

import com.google.protobuf.ByteString;
import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import com.google.protobuf.Service;
import com.znv.fss.common.VConstants;
import com.znv.hbase.coprocessor.endpoint.staytimestat.FaceCluster;
import com.znv.hbase.coprocessor.endpoint.staytimestat.StayTimeSearchOutData;
import com.znv.hbase.protobuf.generated.StayTimeStatProtos;
import com.znv.hbase.util.PhoenixConvertUtil;
import com.znv.hbase.util.ProtoUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.Coprocessor;
import org.apache.hadoop.hbase.CoprocessorEnvironment;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.coprocessor.CoprocessorException;
import org.apache.hadoop.hbase.coprocessor.CoprocessorService;
import org.apache.hadoop.hbase.coprocessor.RegionCoprocessorEnvironment;
import org.apache.hadoop.hbase.regionserver.InternalScanner;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by ZNV on 2017/5/26.
 */
public class StayTimeStatImplementation
        extends StayTimeStatProtos.StayTimeStatService implements CoprocessorService, Coprocessor {
    protected static final Log LOG = LogFactory.getLog(StayTimeStatImplementation.class);
    private RegionCoprocessorEnvironment env;
    private static String featureFamily = "FEATURE"; // 图片特征值列族
    private static String featureColumn = "RT_FEATURE"; // 特征值列
    private static String attrFamily = "ATTR"; // 除特征值和图片之外的列族 摄像头ID,局站ID列
    private static String durTimeColumn = "DURATION_TIME"; //驻留时长
    private static byte[] phoenixGapbs = new byte[1];


    @Override
    public void getStayTimeStatResult(RpcController controller, StayTimeStatProtos.StayTimeStatRequest request,
                                      RpcCallback<StayTimeStatProtos.StayTimeStatResponse> done) {
        StayTimeStatProtos.StayTimeStatResponse response = null;
        List<StayTimeStatProtos.StayTimeStatOut> out = new ArrayList<>();

        int size = request.getSize();
        float threshold = request.getThreshold();
        String startTime = request.getStartTime();
        String endTime = request.getEndTime();
        // 按查询条件过滤数据
        List<StayTimeSearchOutData> searchData = getFilterData(request, startTime, endTime);
        // 人脸聚类
        Map<StayTimeSearchOutData, List<StayTimeSearchOutData>> clusteringMap = null;
        FaceCluster faceCluster = new FaceCluster(threshold);
        if (null != searchData && searchData.size() > 0) {
            clusteringMap = faceCluster.getFaceClusteringResult(searchData, threshold);
        }
        // 排序后的结果
        List<StayTimeSearchOutData> sortData = new ArrayList<StayTimeSearchOutData>();
        if (null != clusteringMap && clusteringMap.size() > 0) {
            sortData = faceCluster.sortByStayTime(clusteringMap);
        }
        // 解析数据到StayTimeStatProtos.StayTimeStatOut,返回top N 条数据
        if (null != sortData && sortData.size() > 0) {
            int count = 0;
            for (StayTimeSearchOutData temp : sortData) {
                StayTimeStatProtos.StayTimeStatOut.Builder builder = StayTimeStatProtos.StayTimeStatOut.newBuilder();
                builder.setStayTime(temp.getDurationTime());
                builder.setRowKey(ByteString.copyFrom(temp.getRowKey()));
                builder.setFeature(ByteString.copyFrom(temp.getFeature()));
                out.add(builder.build());
                count++;
                if (count >= size) {
                    break;
                }
            }
        }
        // 查询结果返回给客户端
        if (out.size() > 0) {
            StayTimeStatProtos.StayTimeStatResponse.Builder responseBuilder = StayTimeStatProtos.StayTimeStatResponse
                    .newBuilder();
            responseBuilder.addAllResults(out);
            response = responseBuilder.build();
        }
        done.run(response);
    }

    /**
     * 根据过滤条件获取数据rowkey:(enter_time, uuid)
     */
    private List<StayTimeSearchOutData> getFilterData(StayTimeStatProtos.StayTimeStatRequest request, String startTime,
                                                      String endTime) {

        byte[] saltedKey = getSaltedKey();
        List<StayTimeSearchOutData> retList = new ArrayList<StayTimeSearchOutData>();
        phoenixGapbs[0] = (byte) 0xff; //时间降序，为xff; 升序则为x00
        if (saltedKey != null && saltedKey.length > 0) {
            InternalScanner scanner = null;

            try {
                Scan s = ProtoUtil.toScan(request.getScan()); // 过滤条件
                s.addFamily(Bytes.toBytes(featureFamily)); // 添加列族
                s.addColumn(Bytes.toBytes(featureFamily), Bytes.toBytes(featureColumn)); // 特征值
                //客户端已添加attrFamily列族
                s.addColumn(Bytes.toBytes(attrFamily), Bytes.toBytes(durTimeColumn)); // 驻留时长

                byte[] starttimeConvt = Bytes.add(PhoenixConvertUtil.convertDescField(Bytes.toBytes(endTime)),
                        phoenixGapbs);
                byte[] stoptimeConvt = Bytes.add(PhoenixConvertUtil.convertDescField(Bytes.toBytes(startTime)),
                        phoenixGapbs);
                // 包含开始时间和结束时间
                byte[] startRowkey = Bytes.add(saltedKey, starttimeConvt);
                byte[] stopRowkey = Bytes.add(saltedKey, stoptimeConvt, phoenixGapbs);

                s.setStartRow(startRowkey);
                s.setStopRow(stopRowkey);

                List<Cell> results = new ArrayList<Cell>();

                boolean hasMoreRows = false;
                scanner = env.getRegion().getScanner(s);
                do {
                    hasMoreRows = scanner.next(results);
                    if (results.size() > 0) {
                        Cell res = results.get(0);
                        byte[] rowKey = res.getRow();
                        StayTimeSearchOutData outData = new StayTimeSearchOutData();
                        outData.setRowKey(rowKey);
                        for (Cell cell : results) {
                            String col = Bytes.toString(CellUtil.cloneQualifier(cell));
                            switch (col) {
                                case "RT_FEATURE":
                                    outData.setFeature(CellUtil.cloneValue(cell));
                                    break;
                                case "DURATION_TIME":
                                    long durationTime = (Bytes.toLong(CellUtil.cloneValue(cell)));
                                    outData.setDurationTime(durationTime);
                                    break;
                                default:
                                    break;
                            }
                        }
                        retList.add(outData);
                    }
                    results.clear();
                } while (hasMoreRows);
            } catch (IOException e) {
                LOG.info(e);
            } catch (Exception e) {
                LOG.info(e);
            } finally {
                if (scanner != null) {
                    try {
                        scanner.close();
                    } catch (IOException e) {
                        LOG.info(e);
                    }
                }
            }

        }
        return retList;
    }

    /**
     * 获取盐值
     */
    private byte[] getSaltedKey() {
        byte[] saltedkey = new byte[VConstants.PHOENIX_ROWKEY_SALTING_LENGTH];
        byte[] regionStartKey = env.getRegion().getRegionInfo().getStartKey(); // 获取region的startkey
        if (regionStartKey.length == 0) {
            saltedkey[0] = VConstants.STAY_TIME_EMPTY_REGION_START_KEY;
        } else {
            System.arraycopy(regionStartKey, 0, saltedkey, 0, VConstants.PHOENIX_ROWKEY_SALTING_LENGTH);
        }
        return saltedkey;
    }

    @Override
    public void start(CoprocessorEnvironment env) throws IOException {
        if (env instanceof RegionCoprocessorEnvironment) {
            this.env = (RegionCoprocessorEnvironment) env;
        } else {
            throw new CoprocessorException("Must be loaded on a table region!");
        }
    }

    @Override
    public void stop(CoprocessorEnvironment env) throws IOException {
        // nothing to do
    }

    @Override
    public Service getService() {
        return this;
    }
}

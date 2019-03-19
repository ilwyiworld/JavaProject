package com.znv.hbase.client.coprocessor;

import com.znv.hbase.client.staytimestat.StayTimeStatParam;
import com.znv.hbase.coprocessor.endpoint.staytimestat.FaceCluster;
import com.znv.hbase.coprocessor.endpoint.staytimestat.StayTimeSearchOutData;
import com.znv.hbase.protobuf.generated.StayTimeStatProtos;
import com.znv.hbase.protobuf.generated.StayTimeStatProtos.StayTimeStatOut;
import com.znv.hbase.protobuf.generated.StayTimeStatProtos.StayTimeStatRequest;
import com.znv.hbase.protobuf.generated.StayTimeStatProtos.StayTimeStatService;
import com.znv.hbase.util.ProtoUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.client.coprocessor.Batch;
import org.apache.hadoop.hbase.ipc.BlockingRpcCallback;
import org.apache.hadoop.hbase.ipc.ServerRpcController;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by ZNV on 2017/6/2.
 */
public class StayTimeStatClient {
    protected static final Log LOG = LogFactory.getLog(StayTimeStatClient.class);
    float threshold = 0.0f;

    public List<StayTimeSearchOutData> getStayTimeStat(final Table table, StayTimeStatParam param, final Scan scan) throws Throwable {
        final StayTimeStatRequest requestArg = validateArgAndGetPB(param, scan);

        /**
         * StayTimeStatCallBack
         */
        class StayTimeStatCallBack implements Batch.Callback<List<StayTimeStatOut>> {
            List<StayTimeSearchOutData> list = new CopyOnWriteArrayList<StayTimeSearchOutData>();
            List<StayTimeSearchOutData> retlist = new ArrayList<StayTimeSearchOutData>();

            // 获取查询结果
            public List<StayTimeSearchOutData> getStayTimeStatSearch() {
                // 人脸聚类
                Map<StayTimeSearchOutData, List<StayTimeSearchOutData>> clusteringMap = new LinkedHashMap<StayTimeSearchOutData, List<StayTimeSearchOutData>>();
                FaceCluster faceCluster = new FaceCluster(threshold);

                if (null != list && list.size() > 0) {
                    clusteringMap = faceCluster.getFaceClusteringResult(list, requestArg.getThreshold());
                }
                // 驻留时间排序后的结果
                List<StayTimeSearchOutData> sortData = new ArrayList<StayTimeSearchOutData>();
                if (null != clusteringMap && clusteringMap.size() > 0) {
                    sortData = faceCluster.sortByStayTime(clusteringMap);
                }
                // 返回top N 结果
                int size = 0;
                for (StayTimeSearchOutData res : sortData) {
                    retlist.add(res);
                    size++;
                    if (size >= requestArg.getSize()) {
                        break;
                    }
                }
                return retlist;
            }

            // 保存查询结果
            @Override
            public void update(byte[] region, byte[] row, List<StayTimeStatOut> result) {
                if (result != null && result.size() > 0) {
                    for (StayTimeStatOut out : result) {
                        StayTimeSearchOutData temp = new StayTimeSearchOutData();
                        temp.setRowKey(out.getRowKey().toByteArray());
                        temp.setFeature(out.getFeature().toByteArray());
                        temp.setDurationTime(out.getStayTime());
                        list.add(temp);
                    }
                }
            }
        }
        // 查询结果返回
        StayTimeStatCallBack callBack = new StayTimeStatCallBack();
        table.coprocessorService(StayTimeStatService.class, null, null,
                new Batch.Call<StayTimeStatService, List<StayTimeStatOut>>() {
                    @Override
                    public List<StayTimeStatOut> call(StayTimeStatService instance) throws IOException {
                        ServerRpcController controller = new ServerRpcController();
                        BlockingRpcCallback<StayTimeStatProtos.StayTimeStatResponse> rpcCallback = new BlockingRpcCallback<StayTimeStatProtos.StayTimeStatResponse>();
                        instance.getStayTimeStatResult(controller, requestArg, rpcCallback);
                        StayTimeStatProtos.StayTimeStatResponse response = rpcCallback.get();
                        if (controller.failedOnException()) {
                            throw controller.getFailedOn();
                        }
                        return response.getResultsList();
                    }
                }, callBack);
        return callBack.getStayTimeStatSearch();
    }

    // 解析输入参数
    StayTimeStatRequest validateArgAndGetPB(StayTimeStatParam param, final Scan scan) throws IOException {
        if (param == null || param.getSize() <= 0 || param.getThreshold() <= 0 || param.getThreshold() > 1) {
            throw new IOException("StayTimeStatClient Exception: Param is invalid!");
        }
        if (scan == null
                || (Bytes.equals(scan.getStartRow(), scan.getStopRow())
                && !Bytes.equals(scan.getStartRow(), HConstants.EMPTY_START_ROW))
                || ((Bytes.compareTo(scan.getStartRow(), scan.getStopRow()) > 0)
                && !Bytes.equals(scan.getStopRow(), HConstants.EMPTY_END_ROW))) {
            throw new IOException("StayTimeStat client Exception: Startrow should be smaller than Stoprow");
        }
        final StayTimeStatRequest.Builder requestBuilder = StayTimeStatRequest
                .newBuilder();

        requestBuilder.setScan(ProtoUtil.toScan(scan));
        requestBuilder.setSize(param.getSize());
        requestBuilder.setThreshold(param.getThreshold()); // 相似度阈值
        this.threshold = param.getThreshold();
        if (StringUtils.isBlank(param.getStartTime())) {
            throw new IllegalArgumentException("开始时间不能为空！");
        } else {
            requestBuilder.setStartTime(param.getStartTime());
        }
        if (StringUtils.isBlank(param.getEndTime())) {
            throw new IllegalArgumentException("结束时间不能为空！");
        } else {
            requestBuilder.setEndTime(param.getEndTime());
        }
        return requestBuilder.build();
    }

}

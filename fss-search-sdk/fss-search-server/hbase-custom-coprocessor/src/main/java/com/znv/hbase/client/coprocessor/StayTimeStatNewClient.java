package com.znv.hbase.client.coprocessor;


import com.znv.hbase.client.staytimestat.StayTimeStatParam;
import com.znv.hbase.coprocessor.endpoint.staytimestat.FaceCluster;
import com.znv.hbase.coprocessor.endpoint.staytimestat.StayTimeSearchOutData;
import com.znv.hbase.protobuf.generated.StayTimeStatProtos;
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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
/**
 * Created by ZNV on 2017/6/8.
 */

public class StayTimeStatNewClient {
    protected static final Log LOG = LogFactory.getLog(StayTimeStatNewClient.class);
    float threshold = 0.0f;

    public Map<StayTimeSearchOutData, List<StayTimeSearchOutData>> getStayTimeStatNew(final Table table, StayTimeStatParam param, final Scan scan) throws Throwable {
        final StayTimeStatProtos.StayTimeStatRequest requestArg = validateArgAndGetPB(param, scan);

        /**
         * StayTimeStatNewCallBack
         */
        class StayTimeStatNewCallBack implements Batch.Callback<List<StayTimeStatProtos.StayTimeMap>> {
            //第一次聚类结果
            Map<StayTimeSearchOutData, List<StayTimeSearchOutData>> resultMap = new ConcurrentHashMap<>();
            //第一次聚类leader信息
            List<StayTimeSearchOutData> list = new CopyOnWriteArrayList<StayTimeSearchOutData>();

            //客户端返回结果
            Map<StayTimeSearchOutData, List<StayTimeSearchOutData>> retMap = new LinkedHashMap<>();

            // 客户端获取查询结果
            public Map<StayTimeSearchOutData, List<StayTimeSearchOutData>> getStayTimeStatNewSearch() {
                // 人脸聚类
                Map<StayTimeSearchOutData, List<StayTimeSearchOutData>> clusteringMap = new LinkedHashMap<>();
                FaceCluster faceCluster = new FaceCluster(threshold);
                //第二次聚类
                if (null != list && list.size() > 0) {
                    clusteringMap = faceCluster.getFaceClusteringResult(list, requestArg.getThreshold());
                }

                //聚类总结果
                Map<StayTimeSearchOutData, List<StayTimeSearchOutData>> totalMap = new LinkedHashMap<>();

                if (null != clusteringMap && clusteringMap.size() > 0) {
                    //两次聚类之间对第二次聚类的每一个组员和第一次聚类的leader根据rowkey寻找相同的组员
                    Iterator<Map.Entry<StayTimeSearchOutData, List<StayTimeSearchOutData>>> scnItrCluster = clusteringMap.entrySet()
                            .iterator();

                    while (scnItrCluster.hasNext()) { //第二次聚类的每个组

                        Map.Entry<StayTimeSearchOutData, List<StayTimeSearchOutData>> cluterEntry = scnItrCluster.next();
                        List<StayTimeSearchOutData> cluterPersonData = cluterEntry.getValue(); //第二次聚类组员

                        List<StayTimeSearchOutData> totalPerson = new ArrayList<StayTimeSearchOutData>();

                        for (StayTimeSearchOutData data : cluterPersonData) {
                            Iterator<Map.Entry<StayTimeSearchOutData, List<StayTimeSearchOutData>>> scnItrResultMap = resultMap.entrySet()
                                    .iterator();
                            while (scnItrResultMap.hasNext()) {
                                Map.Entry<StayTimeSearchOutData, List<StayTimeSearchOutData>> mapResultEntry = scnItrResultMap.next();
                                StayTimeSearchOutData mapLeader = mapResultEntry.getKey(); //第一次聚类leader

                                if (0 == Bytes.compareTo(data.getRowKey(), mapLeader.getRowKey())) { //第二次聚类的每个组员找第一次leader
                                    totalPerson.addAll(mapResultEntry.getValue());
                                    break;
                                }
                            }
                            totalMap.put(cluterEntry.getKey(), totalPerson);
                        }
                    }
                }

                //针对每个组员，根据camera_id进行合并，进行驻留时长累加,leader不变
                Map<StayTimeSearchOutData, List<StayTimeSearchOutData>> groupMap = null;
                if (totalMap != null && totalMap.size() > 0) {
                    groupMap = faceCluster.groupByCameraId(totalMap);
                }

                // 驻留时间排序后的结果，得到组员
                Map<StayTimeSearchOutData, List<StayTimeSearchOutData>> sortMap = null;
                if (null != groupMap && groupMap.size() > 0) {
                    sortMap = faceCluster.sortByStayTimeMap(groupMap);
                }

                // 返回top N 结果
                int size = 0;
                if (sortMap != null && sortMap.size() > 0) {
                    Iterator<Map.Entry<StayTimeSearchOutData, List<StayTimeSearchOutData>>> scnItrSortMap = sortMap.entrySet()
                            .iterator();
                    while (scnItrSortMap.hasNext()) {
                        Map.Entry<StayTimeSearchOutData, List<StayTimeSearchOutData>> entry = scnItrSortMap.next();
                        retMap.put(entry.getKey(), entry.getValue());
                        size++;
                        if (size >= requestArg.getSize()) {
                            break;
                        }
                    }
                }
                return retMap;
            }

            // 保存查询结果
            @Override
            public void update(byte[] region, byte[] row, List<StayTimeStatProtos.StayTimeMap> result) {
                if (result != null && result.size() > 0) {
                    for (StayTimeStatProtos.StayTimeMap out : result) {
                        //leader信息
                        StayTimeSearchOutData leader = new StayTimeSearchOutData();
                        leader.setRowKey(out.getRowKey().toByteArray());
                        leader.setFeature(out.getFeature().toByteArray());
                        leader.setDurationTime(out.getStayTime());
                        leader.setCameraId(out.getCameraId());
                        list.add(leader);
                        //数据信息
                        List<StayTimeSearchOutData> personList = new ArrayList<StayTimeSearchOutData>();
                        List<StayTimeStatProtos.StayTimeStatOut> dataList = out.getDatasList();
                        if (null != dataList && dataList.size() > 0) {
                            for (StayTimeStatProtos.StayTimeStatOut data : dataList) {
                                StayTimeSearchOutData person = new StayTimeSearchOutData();
                                person.setRowKey(data.getRowKey().toByteArray());
                                person.setFeature(data.getFeature().toByteArray());
                                person.setDurationTime(data.getStayTime());
                                person.setCameraId(data.getCameraId());
                                person.setCameraName(data.getCameraName());
                                person.setImgUrl(data.getImgUrl());
                                personList.add(person);
                            }
                        }
                        resultMap.put(leader, personList);
                    }
                }
            }
        }
        // 查询结果返回
        StayTimeStatNewCallBack callBack = new StayTimeStatNewCallBack();
        table.coprocessorService(StayTimeStatProtos.StayTimeStatNewService.class, null, null,
                new Batch.Call<StayTimeStatProtos.StayTimeStatNewService, List<StayTimeStatProtos.StayTimeMap>>() {
                    @Override
                    public List<StayTimeStatProtos.StayTimeMap> call(StayTimeStatProtos.StayTimeStatNewService instance) throws IOException {
                        ServerRpcController controller = new ServerRpcController();
                        BlockingRpcCallback<StayTimeStatProtos.StayTimeStatNewResponse> rpcCallback = new BlockingRpcCallback<StayTimeStatProtos.StayTimeStatNewResponse>();
                        instance.getStayTimeStatNewResult(controller, requestArg, rpcCallback);
                        StayTimeStatProtos.StayTimeStatNewResponse response = rpcCallback.get();
                        if (controller.failedOnException()) {
                            throw controller.getFailedOn();
                        }
                        return response.getResultsList();
                    }
                }, callBack);
        return callBack.getStayTimeStatNewSearch();
    }

    // 解析输入参数
    StayTimeStatProtos.StayTimeStatRequest validateArgAndGetPB(StayTimeStatParam param, final Scan scan) throws IOException {
        if (param == null) {
            throw new IOException("StayTimeStatNewClient Exception: Param is invalid!");
        }

       /* if (param.getAnalysis() == null) {
            throw new IOException("StayTimeStatNewClient Exception: analysis is invalid!");
        }*/

        if (param.getSize() <= 0 || param.getThreshold() <= 0 || param.getThreshold() > 1) {
            throw new IOException("StayTimeStatNewClient Exception: size or threshold is invalid!");
        }
        if (scan == null
                || (Bytes.equals(scan.getStartRow(), scan.getStopRow())
                && !Bytes.equals(scan.getStartRow(), HConstants.EMPTY_START_ROW))
                || ((Bytes.compareTo(scan.getStartRow(), scan.getStopRow()) > 0)
                && !Bytes.equals(scan.getStopRow(), HConstants.EMPTY_END_ROW))) {
            throw new IOException("StayTimeStatNewClient Exception: Startrow should be smaller than Stoprow");
        }
        final StayTimeStatProtos.StayTimeStatRequest.Builder requestBuilder = StayTimeStatProtos.StayTimeStatRequest
                .newBuilder();

        requestBuilder.setScan(ProtoUtil.toScan(scan));
        requestBuilder.setSize(param.getSize());
        requestBuilder.setThreshold(param.getThreshold()); // 相似度阈值
        threshold = param.getThreshold();
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

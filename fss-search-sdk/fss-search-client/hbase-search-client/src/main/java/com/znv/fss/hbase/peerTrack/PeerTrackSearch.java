package com.znv.fss.hbase.peerTrack;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.znv.fss.common.VConstants;
import com.znv.fss.common.errorcode.FssErrorCodeEnum;
import com.znv.fss.common.utils.FeatureCompUtil;
import com.znv.fss.hbase.HBaseConfig;
import com.znv.fss.hbase.HBaseManager;
import com.znv.fss.hbase.JsonResultType;
import com.znv.fss.hbase.MultiHBaseSearch;
import com.znv.hbase.client.coprocessor.ImageSearchClient;
import com.znv.hbase.client.featureComp.ImageSearchParam;
import com.znv.hbase.coprocessor.endpoint.searchByImage.ImageSearchOutData;
import com.znv.fss.hbase.utils.DateTimeFun;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Administrator on 2017/10/21.
 */
public class PeerTrackSearch extends MultiHBaseSearch {

    private static final String schemaName = HBaseConfig.getProperty(VConstants.FSS_PHOENIX_SCHEMA_NAME);
    private static final String historyTableName = HBaseConfig.getProperty(VConstants.FSS_HISTORY_V113_TABLE_NAME);
    private static final String tableName = schemaName + ":" + historyTableName;
    private final Log LOG = LogFactory.getLog(PeerTrackSearch.class);
    private final int maxN = 100; // 最多返回条数
    private String sortOrder = "desc"; // asc：升序，desc：降序
    private List<ImageSearchOutData> personList = new ArrayList<ImageSearchOutData>();
    private int pageSize = 0;
    private int pageIndex = 1;
    private static int maxPageSize = 500; // 返回最大记录数
    private String errorCode = " SUCCESS";
    private FeatureCompUtil fc = new FeatureCompUtil();

    public PeerTrackSearch() {
        super("search");
        this.timeOutMinute = 10;// 同行人超时时长设为10分钟
    }

    /**
     * 获取查询结果
     */
    @Override
    public String requestSearch(String jsonParamStr) throws Exception {
        fc.setFeaturePoints(HBaseConfig.getFeaturePoints()); // [lq-add]
        String jsonstr = "";
        float hbaseThreshold = fc.reversalNormalize(0.89f) ; // [lq-modify] 协处理器传入反归一化阈值
        long t1 = System.currentTimeMillis(); // 查询统计开始时间
        PeerTrackParam paramIn = parseJsonParams(jsonParamStr);

        // 返回下一页数据
        if (pageIndex > 1 && personList.size() > 0) {
            int startIndex = (pageIndex - 1) * pageSize;
            if (startIndex >= personList.size()) {
                // 再无下一页数据
                PeerTrackReportServiceOut result = new PeerTrackReportServiceOut();
                result.setId(HBaseManager.SearchId.SearchForPeer.getId());
                result.setErrorcode(JsonResultType.SUCCESS);
                result.setType("response");
                result.setCount(0);
                long t2 = System.currentTimeMillis() - t1;
                result.setTime(t2);
                PeerTrackJsonOutput jsonOut = new PeerTrackJsonOutput();
                jsonOut.setReportservice(result);
                Object jsonObject = JSON.toJSON(jsonOut);
                jsonstr = JSON.toJSONString(jsonObject);
                return jsonstr;
            }

            int stopIndex = pageIndex * pageSize;
            if (stopIndex > personList.size()) {
                stopIndex = personList.size();
            }
            List<ImageSearchOutData> searchPersonList = new ArrayList<ImageSearchOutData>();
            for (int idx = startIndex; idx < stopIndex; idx++) {
                searchPersonList.add(personList.get(idx));
            }

            // 获取同行人信息
            List<PeerTrackOutputData> outputList = getNextPageData(paramIn, searchPersonList);
            // 组输出结果
            PeerTrackReportServiceOut result = new PeerTrackReportServiceOut();
            result.setId(HBaseManager.SearchId.SearchForPeer.getId());
            result.setErrorcode(JsonResultType.SUCCESS);
            result.setType("response");
            if (outputList == null || outputList.isEmpty()) {
                result.setCount(0);
            } else {
//                result.setCount(outputList.size());
                result.setCount(personList.size());
                result.setPeerTrackData(outputList);
            }
            long t2 = System.currentTimeMillis() - t1;
            result.setTime(t2);

            PeerTrackJsonOutput jsonOut = new PeerTrackJsonOutput();
            jsonOut.setReportservice(result);

            Object jsonObject = JSON.toJSON(jsonOut);
            jsonstr = JSON.toJSONString(jsonObject);
            return jsonstr;

        }

        // 以图搜图，查询目标人的查询时间范围内的所有数据
        ImageSearchParam searchParam = new ImageSearchParam();
        if (paramIn != null) {
            searchParam.setStartTime(paramIn.getStarttime());
            searchParam.setEndTime(paramIn.getEndtime());
            searchParam.setThreshold(hbaseThreshold);
            List<String> featureStrs = paramIn.getSearchfeature();
            searchParam.setSearchFeatures(featureStrs);
            searchParam.setSearchType("4"); // 同行人轨迹查询
            searchParam.setSelType("1"/* paramIn.getPicfiltertype() */); // 1:交集
            searchParam.setOfficeIds(paramIn.getOfficeids());
            searchParam.setCameraIds(paramIn.getCameraids());
        }

        // 第一次查询，获取目标人数据
        try {
            // 以图搜图协处理器，输入：图片集，SearchType，StartTime，EndTime，SelType
            // 输出疑似图片enter_time,uuid,duration_time,leave_time,gpsx,gpsy,camera_id
            ImageSearchClient client1 = new ImageSearchClient();
            personList = client1.getSearchByImageResult(HBaseConfig.getTable(tableName), searchParam); // 协处理器中已排序

            // 目标人信息按时间倒排序
            getCatchTimeSortList(personList);

            // 查询第一页数据
            List<ImageSearchOutData> firstPersonList = new ArrayList<ImageSearchOutData>();
            if (personList.size() <= pageSize) {
                firstPersonList = personList;
            } else {
                for (int i = 0; i < pageSize; i++) {
                    firstPersonList.add(personList.get(i));
                }
            }

            List<PeerTrackOutputData> outputList = getNextPageData(paramIn, firstPersonList);

            // 组输出结果
            PeerTrackReportServiceOut result = new PeerTrackReportServiceOut();
            result.setId(HBaseManager.SearchId.SearchForPeer.getId());
            result.setErrorcode(JsonResultType.SUCCESS);
            result.setType("response");
            if (outputList == null || outputList.isEmpty()) {
                result.setCount(0);
            } else {
//                result.setCount(outputList.size());
                result.setCount(personList.size());
                result.setPeerTrackData(outputList);
            }
            long t2 = System.currentTimeMillis() - t1;
            result.setTime(t2);
            PeerTrackJsonOutput jsonOut = new PeerTrackJsonOutput();
            jsonOut.setReportservice(result);
            Object jsonObject = JSON.toJSON(jsonOut);
            jsonstr = JSON.toJSONString(jsonObject);

        } catch (Throwable e) {
            errorCode = "ERROR";
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("errorCode",FssErrorCodeEnum.HBASE_GET_EXCEPTION);
            jsonstr = JSON.toJSONString(jsonObject);
            LOG.error(e);
        }
        return jsonstr;
    }

    private List<PeerTrackOutputData> getNextPageData(PeerTrackParam paramIn, List<ImageSearchOutData> searchData) {
        List<PeerTrackOutputData> outputList = new ArrayList<PeerTrackOutputData>();
        if (paramIn != null) {
            try {
                // 查找同行人,将同行人信息写入到map中
                Map<String, PeerTrackOutputData> targetAndPeerMap = new ConcurrentHashMap<String, PeerTrackOutputData>();
                int nSalts = Integer.parseInt(HBaseConfig.getProperty(VConstants.HISTORY_SALT_BUCKETS));
                ExecutorService fixedThreadPool = Executors.newFixedThreadPool(36);
                CountDownLatch threadSignal = new CountDownLatch(nSalts);
                List<String> features = paramIn.getSearchfeature();
                for (int i = 0; i < nSalts; i++) {
                    Thread t = new PeerTrackThread(threadSignal, tableName, searchData, paramIn.getPeerinterval(), i,
                            features, targetAndPeerMap);
                    fixedThreadPool.execute(t);
                }
                // 等待所有子线程执行完
                try {
                    threadSignal.await();
                } catch (InterruptedException e) {
                    fixedThreadPool.shutdownNow();
                    e.printStackTrace();
                }
                fixedThreadPool.shutdown();
                // 缓存返回结果，并将同一个目标人的同行人信息组在一个结果集中
            //List<PeerTrackOutputData> outputList = new ArrayList<PeerTrackOutputData>();
            for (Map.Entry<String, PeerTrackOutputData> entry : targetAndPeerMap.entrySet()) {
                PeerTrackOutputData val = entry.getValue();
                outputList.add(val);
            }

            getPeerSortList(outputList);

            } catch (/*Interrupted*/Exception e) {
                LOG.error(e);
//                Thread.currentThread().interrupted();
            }
        }
        return outputList;
    }

    // 删除ArrayList中重复元素
    private static List<PeerTrackOutputData> removeDuplicate(List<PeerTrackOutputData> list) {
        List<PeerTrackOutputData> result = new ArrayList<PeerTrackOutputData>();
        result.add(list.get(0));
        for (PeerTrackOutputData rt : list) {
            String enterTime = rt.getTargetData().getEnterTime();
            String uuid = rt.getTargetData().getUuid();
            for (int i = 0; i < result.size(); i++) {
                PeerTrackOutputData data = result.get(i);

                // for (PeerTrackOutputData data : result) {
                String dataEnterTime = data.getTargetData().getEnterTime();
                String dataUuid = data.getTargetData().getUuid();
                if (!enterTime.equals(dataEnterTime) || !uuid.equals(dataUuid)) {
                    result.add(rt);
                }
            }
        }
        return result;
    }

    // 查询结果降序排序
    public void getCatchTimeSortList(List<ImageSearchOutData> list) {
        // enter_time降序排序
        Collections.sort(list, new Comparator<ImageSearchOutData>() {
            @Override
            public int compare(ImageSearchOutData o1, ImageSearchOutData o2) {
                String catchTime1 = o1.getEnterTime();
                String catchTime2 = o2.getEnterTime();
                if (sortOrder.equals("asc")) {
                    return catchTime1.compareTo(catchTime2);
                } else if (sortOrder.equals("desc")) {
                    return catchTime2.compareTo(catchTime1);
                }
                return catchTime2.compareTo(catchTime1);// 默认降序
            }
        });
    }

    // 查询结果降序排序
    private void getPeerSortList(List<PeerTrackOutputData> list) {
        // enter_time降序排序
        Collections.sort(list, new Comparator<PeerTrackOutputData>() {
            @Override
            public int compare(PeerTrackOutputData o1, PeerTrackOutputData o2) {
                String catchTime1 = o1.getTargetData().getEnterTime();
                String catchTime2 = o2.getTargetData().getEnterTime();
                if (sortOrder.equals("asc")) {
                    return catchTime1.compareTo(catchTime2);
                } else if (sortOrder.equals("desc")) {
                    return catchTime2.compareTo(catchTime1);
                }
                return catchTime2.compareTo(catchTime1);// 默认降序
            }
        });
    }

    // 解析web传入查询条件
    private PeerTrackParam parseJsonParams(String jsonParamStr) throws IOException {
        PeerTrackJsonInput inputParam = JSON.parseObject(jsonParamStr, PeerTrackJsonInput.class);
        PeerTrackReportServiceIn service = inputParam.getReportservice();
        HBaseManager.SearchId id = HBaseManager.SearchId.getSearchId(service.getId());
        String type = service.getType();
        if (id.equals(HBaseManager.SearchId.SearchForPeer) && type.equals("request")) { // 12008
            PeerTrackParam param = service.getPeerTrackParam();
            validateArg(param);
            pageIndex = Integer.parseInt(param.getPage().getIndex());
            if (pageIndex < 1) {
                pageIndex = 1;
            }
            pageSize = Integer.parseInt(param.getPage().getSize());
            if (pageSize < 10) {
                pageSize = 10;
            } else if (pageSize > maxPageSize) {
                pageSize = maxPageSize; // 设置最大返回数
            }
            return param; // 获取查询条件
        }
        return null;
    }

    private void validateArg(PeerTrackParam param) throws IOException {
        if (param == null) {
            throw new IOException("PeerTrack Exception: Param is null");
        } else {
            if (StringUtils.isEmpty(param.getStarttime())) {
                throw new IllegalArgumentException("StartTime can't be empty！");
            }
            if (StringUtils.isEmpty(param.getEndtime())) {
                throw new IllegalArgumentException("EndTime can't be empty！");
            }
            String timeDelta = null;
            try {
                timeDelta = DateTimeFun.getTimeDelta(param.getStarttime(), param.getEndtime());
            } catch (ParseException e) {
                LOG.error(e);
                throw new IllegalArgumentException("Time format is wrong！");
            }
            if (timeDelta != null && Float.parseFloat(timeDelta) > 7 * 24 * 60) {
                throw new IllegalArgumentException("The range of time can't exceed 7 days！");
            }
            if (param.getSearchfeature().isEmpty()) {
                throw new IllegalArgumentException("The image feature can't be empty！");
            }
            // if (!StringUtils.equals(param.getAssociationtype(), "1")
            // && !StringUtils.equals(param.getAssociationtype(), "2")) {
            // throw new IllegalArgumentException("The association type value is wrong！");
            // }
            if (param.getPeerinterval() < 0 || param.getPeerinterval() > 30) { // 单位：秒
                throw new IllegalArgumentException("The range of peer interval value is between 0 and 30！");
            }
            if (param.getTopn() < 0 || param.getTopn() > maxN) {
                throw new IllegalArgumentException("The range of returned topN value is between 0 and 100！");
            }

            // if (param.getOfficeids().isEmpty()) {
            // throw new IllegalArgumentException("office can't be empty！");
            // }
        }
    }
}

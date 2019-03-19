package com.znv.fss.hbase.relationshipsearch;

import com.alibaba.fastjson.JSON;
import com.znv.fss.common.VConstants;
import com.znv.fss.common.utils.FeatureCompUtil;
import com.znv.fss.hbase.HBaseConfig;
import com.znv.fss.hbase.HBaseManager;
import com.znv.fss.hbase.JsonResultType;
import com.znv.fss.hbase.MultiHBaseSearch;
import com.znv.hbase.client.coprocessor.ImageSearchClient;
import com.znv.hbase.client.featureComp.ImageSearchParam;
import com.znv.hbase.coprocessor.endpoint.searchByImage.ImageSearchOutData;
import com.znv.hbase.coprocessor.endpoint.staytimestat.FaceCluster;
import com.znv.hbase.coprocessor.endpoint.staytimestat.StayTimeSearchOutData;
import com.znv.fss.hbase.utils.DateTimeFun;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by huangyan on 2017/6/6.
 */
public class RelationshipSearch extends MultiHBaseSearch {
    private final String schemaName = HBaseConfig.getProperty(VConstants.FSS_PHOENIX_SCHEMA_NAME);
    private final String historyTableName = HBaseConfig.getProperty(VConstants.FSS_HISTORY_V113_TABLE_NAME);
    private final Log LOG = LogFactory.getLog(RelationshipSearch.class);
    private final int maxN = 100; // 最多返回条数
    private FeatureCompUtil fc = new FeatureCompUtil();

    public RelationshipSearch() {
        super("search");
        this.timeOutMinute = 10;// 同行人超时时长设为10分钟
    }

    /**
     * 获取查询结果
     */
    @Override
    public String requestSearch(String jsonParamStr) throws Exception {
        fc.setFeaturePoints(HBaseConfig.getFeaturePoints()); // [lq-add]
        RelationshipParam paramIn = parseJsonParams(jsonParamStr);
        String jsonstr = "";
        float hbaseThreshold = fc.reversalNormalize(0.89f); // [lq-modify]

        ImageSearchParam searchParam = new ImageSearchParam();
        List<String> featureStrs = new ArrayList<>();
        if (paramIn != null) {
            searchParam.setStartTime(paramIn.getStarttime());
            searchParam.setEndTime(paramIn.getEndtime());
            searchParam.setThreshold(hbaseThreshold);
            featureStrs = paramIn.getSearchfeature();
            searchParam.setSearchFeatures(featureStrs);
            searchParam.setSearchType("2");//查询类型，1-以脸搜脸 ,2-人物关系查询, 3-轨迹
            searchParam.setSelType(paramIn.getPicfiltertype());
            searchParam.setOfficeIds(paramIn.getOfficeids());
            searchParam.setCameraIds(paramIn.getCameraids());
        }

        List<ImageSearchOutData> personList;
        try {
            long t1 = System.currentTimeMillis();

            // 以图搜图协处理器，返回疑似图片特征值、相似度
            ImageSearchClient client1 = new ImageSearchClient();
            String tablename = schemaName + ":" + historyTableName;
            personList = client1.getSearchByImageResult(HBaseConfig.getTable(tablename), searchParam); // 协处理器中已排序

            // 查找同行人
            List<StayTimeSearchOutData> peerlist = new CopyOnWriteArrayList<StayTimeSearchOutData>();
            int nSalts = Integer.parseInt(HBaseConfig.getProperty(VConstants.HISTORY_SALT_BUCKETS));
            ExecutorService fixedThreadPool = Executors.newFixedThreadPool(36);
            CountDownLatch threadSignal = new CountDownLatch(nSalts);
            List<byte[]> searchFeatures = new ArrayList<>(featureStrs.size());
            Base64 base64 = new Base64();
            for (String feature : featureStrs) {
                searchFeatures.add(base64.decode(feature));
            }
            for (int i = 0; i < nSalts; i++) {
                if (paramIn != null) {
                    Thread t = new PeerSearchThread(threadSignal, tablename, personList, peerlist,
                            paramIn.getPeerinterval(), i, searchFeatures);
                    fixedThreadPool.execute(t);
                }
            }
            // 等待所有子线程执行完
            try {
                threadSignal.await();
            } catch (InterruptedException e) {
                fixedThreadPool.shutdownNow();
                e.printStackTrace();
            }
            fixedThreadPool.shutdown();
            // 对同行人聚类
            FaceCluster faceCluster = new FaceCluster(hbaseThreshold);
            Map<StayTimeSearchOutData, List<StayTimeSearchOutData>> peerClusterInfo = faceCluster
                    .getFaceClusteringResult(peerlist, hbaseThreshold);

            // 排除查询本人
            // excludeQueryPerson(peerClusterInfo, hbaseThreshold, paramIn.getSearchfeature());

            // 根据排序类型对同行人聚类后的数据进行排序，并返回结果给WEB
            List<RelationshipData> outlist = null;
            if (paramIn != null) {
                List<StayTimeSearchOutData> sortPeerInfo = sortPeerInfo(peerClusterInfo, paramIn.getSorttype());
                outlist = getRelationResult(sortPeerInfo, paramIn.getTopn());
            }
            RelationReportServiceOut result = new RelationReportServiceOut();
            result.setId(HBaseManager.SearchId.RelationshipSearch.getId());
            result.setErrorcode(JsonResultType.SUCCESS);
            result.setType("response");
            if (outlist == null || outlist.isEmpty()) {
                result.setCount(0);
            } else {
                result.setCount(outlist.size());
                result.setRelationshipdata(outlist);
            }
            long t2 = System.currentTimeMillis() - t1;
            result.setTime(t2);
            RelationshipJsonOutput jsonout = new RelationshipJsonOutput();
            jsonout.setReportservice(result);
            Object jsonObject = JSON.toJSON(jsonout);
            jsonstr = JSON.toJSONString(jsonObject);

        } catch (Throwable e) {
            LOG.info(e);
        }

        return jsonstr;
    }

    private List<StayTimeSearchOutData> sortPeerInfo(Map<StayTimeSearchOutData, List<StayTimeSearchOutData>> clusteringMap, String sortType) {
        if (null == clusteringMap || clusteringMap.size() == 0) {
            LOG.warn("clusteringMap is null!");
            return null;
        }
        Iterator<Map.Entry<StayTimeSearchOutData, List<StayTimeSearchOutData>>> scnItr = clusteringMap.entrySet()
                .iterator();
        List<StayTimeSearchOutData> outDataList;
        // 排序后的数据值保留总的时长、次数和rowkey
        List<StayTimeSearchOutData> sortDataList = new ArrayList<StayTimeSearchOutData>();

        while (scnItr.hasNext()) {
            Map.Entry<StayTimeSearchOutData, List<StayTimeSearchOutData>> entry = scnItr.next();
            outDataList = entry.getValue();
            long stayTime = 0L;
            if (null != outDataList && outDataList.size() > 0) {
                for (StayTimeSearchOutData data : outDataList) {
                    stayTime += data.getDurationTime();
                }
                StayTimeSearchOutData leader = entry.getKey();
                leader.setCount(outDataList.size());
                leader.setDurationTime(stayTime);
                sortDataList.add(leader);
            }
        }

        if (sortType.equals("1")) { // 次数
            Collections.sort(sortDataList, new Comparator<StayTimeSearchOutData>() {
                @Override
                public int compare(StayTimeSearchOutData o1, StayTimeSearchOutData o2) {
                    return Integer.compare(o2.getCount(), o1.getCount());
                }
            });
        } else { // 时长
            Collections.sort(sortDataList, new Comparator<StayTimeSearchOutData>() {
                @Override
                public int compare(StayTimeSearchOutData o1, StayTimeSearchOutData o2) {
                    return Long.compare(o2.getDurationTime(), o1.getDurationTime());
                }
            });
        }

        return sortDataList;
    }

    // 排除结果中查询图片数据
    private void excludeQueryPerson(Map<StayTimeSearchOutData, List<StayTimeSearchOutData>> peerClusterInfo,
                                    float threshold, String sfeature) {
        Base64 base64 = new Base64();
        byte[] f1 = base64.decode(sfeature);
        FeatureCompUtil fc = new FeatureCompUtil();
        float reservalThreshold = fc.reversalNormalize(threshold);
        Iterator<Map.Entry<StayTimeSearchOutData, List<StayTimeSearchOutData>>> it = peerClusterInfo.entrySet()
                .iterator();
        while (it.hasNext()) {
            Map.Entry<StayTimeSearchOutData, List<StayTimeSearchOutData>> entry = it.next();
            StayTimeSearchOutData data = entry.getKey();
            float sim = fc.Dot(data.getFeature(), f1, 12); // 比对相似度
            if (sim >= reservalThreshold) { // 相似度超过阈值，认为是同一类人脸
                it.remove();
            }
        }
    }

    // 获取图像数据等信息
    private List<RelationshipData> getRelationResult(List<StayTimeSearchOutData> sortPeerInfo, int topN) {
        if (null == sortPeerInfo || sortPeerInfo.size() <= 0) {
            return null;
        }

        List<Get> listGets = new ArrayList<Get>(sortPeerInfo.size());
        byte[] imagefam = Bytes.toBytes("ATTR");
        byte[] imagequal = Bytes.toBytes("IMG_URL");
        int i = 0;
        for (StayTimeSearchOutData temp : sortPeerInfo) {
            Get get = new Get(temp.getRowKey());
            get.addColumn(imagefam, imagequal);
            listGets.add(get);

            if (++i >= topN) {
                break;
            }
        }

        Base64 base64 = new Base64();
        HTable table = null;
        List<RelationshipData> outlist = new ArrayList<RelationshipData>(sortPeerInfo.size());
        try {
            String tablename = schemaName + ":" + historyTableName;
            table = HBaseConfig.getTable(tablename);
            Result[] results = table.get(listGets);

            i = 0;
            for (Result r : results) {
                byte[] image = r.getValue(imagefam, imagequal);
                if (image == null) { // 防止出现无图片的情况
                    i++;
                    continue;
                }
                String imgUrl = Bytes.toString(r.getValue(imagefam, imagequal));
                RelationshipData outdata = new RelationshipData();
                StayTimeSearchOutData data = sortPeerInfo.get(i++);
                outdata.setPeercount(data.getCount());
                outdata.setPeertime(data.getDurationTime());
                outdata.setPeerlibid(data.getLibId());
                outdata.setPeerpersonid(data.getPersonId());
                //outdata.setImagedata(base64.encodeBase64String(image)); // todo
                outdata.setImgUrl(Bytes.toString(image));
                outlist.add(outdata);
            }

        } catch (IOException e) {
            LOG.info(e);
        } finally {
            try {
                if (table != null) {
                    table.close();
                }
            } catch (IOException e) {
                LOG.info(e);
            }
        }

        return outlist;
    }

    // 解析web传入查询条件
    private RelationshipParam parseJsonParams(String jsonParamStr) throws IOException {
        RelationshipJsonInput inputParam = JSON.parseObject(jsonParamStr, RelationshipJsonInput.class);
        RelationReportServiceIn service = inputParam.getReportservice();
        HBaseManager.SearchId id = HBaseManager.SearchId.getSearchId(service.getId());
        String type = service.getType();
        if (id.equals(HBaseManager.SearchId.RelationshipSearch) && type.equals("request")) { // 12005
            RelationshipParam param = service.getRelationshipparam();
            validateArg(param);
            return param; // 获取查询条件
        }

        return null;
    }

    private void validateArg(RelationshipParam param) throws IOException {
        if (param == null) {
            throw new IOException("RelationshipParam Exception: Param is null");
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
            if (!StringUtils.equals(param.getSorttype(), "1")
                    && !StringUtils.equals(param.getSorttype(), "2")) {
                throw new IllegalArgumentException("The sort type value is wrong！");
            }
            if (param.getPeerinterval() < 0 || param.getPeerinterval() > 30) {
                throw new IllegalArgumentException("The range of peer interval value is between 0 and 30！");
            }
            if (param.getTopn() < 0 || param.getTopn() > maxN) {
                throw new IllegalArgumentException("The range of returned topN value is between 0 and 100！");
            }
            if (!StringUtils.equals(param.getPicfiltertype(), "1")
                    && !StringUtils.equals(param.getPicfiltertype(), "2")) {
                throw new IllegalArgumentException("The sort type value is wrong！");
            }
        }
    }

}

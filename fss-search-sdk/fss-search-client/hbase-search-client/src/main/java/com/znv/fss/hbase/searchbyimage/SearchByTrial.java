package com.znv.fss.hbase.searchbyimage;

import com.alibaba.fastjson.JSON;
import com.znv.fss.common.VConstants;
import com.znv.fss.common.errorcode.FssErrorCodeEnum;
import com.znv.fss.common.utils.FeatureCompUtil;
import com.znv.fss.hbase.HBaseConfig;
import com.znv.fss.hbase.HBaseManager;
import com.znv.fss.hbase.MultiHBaseSearch;
import com.znv.hbase.client.coprocessor.ImageSearchClient;
import com.znv.hbase.client.featureComp.ImageSearchParam;
import com.znv.hbase.coprocessor.endpoint.searchByImage.ImageSearchOutData;
import com.znv.fss.hbase.utils.DateTimeFun;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.client.Get;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collections;
import java.util.Comparator;

/**
 *
 */
public class SearchByTrial extends MultiHBaseSearch {
    private final Log log = LogFactory.getLog(SearchByTrial.class);
    private final String schemaName = HBaseConfig.getProperty(VConstants.FSS_PHOENIX_SCHEMA_NAME);
    private final String hbaseTableName = schemaName + ":" + HBaseConfig.getProperty(VConstants.FSS_HISTORY_V113_TABLE_NAME);
    //private final String hbaseTableName = HBaseConfig.getProperty(VConstants.HISTORY_TABLE_NAME); // FSS_imageData_searchByImage
    // private static final float hbaseThrehold = 0.485547f; // 0.8900057
    private FssInPutParams param = new FssInPutParams();
    private List<ImageSearchOutData> suspectList = new ArrayList<ImageSearchOutData>();

    private int count = 0; // 总记录数
    private String exception = "";
    private String sortType = ""; // 新增排序方式，1：相似度倒排序，2：抓拍时间倒排序
    private String sortOrder = ""; // asc：升序，desc：降序
    private FeatureCompUtil fc = new FeatureCompUtil();

    public SearchByTrial() {
        super("search");
    }

    /**
     * 获取查询结果
     */
    @Override
    public String requestSearch(String jsonParamStr) throws Exception {
        fc.setFeaturePoints(HBaseConfig.getFeaturePoints()); // [lq-add]
        // 解析json协议
        parseJsonParams(jsonParamStr); // 查询图片特征值
        // 解析查询条件
        //String feature = param.getSearchFeature(); // 特征值
        List<String> features = param.getSearchFeatures(); // 特征值

        String startTime = param.getStartTime(); // 开始时间
        String endTime = param.getEndTime(); // 结束时间
        List<String> cameraIds = param.getCameraIds(); // 多个cameraId
        // float hbaseThreshold = param.getSimThreshold() / 100.00f;
        float hbaseThreshold = fc.reversalNormalize(param.getSimThreshold() / 100.00f); // 协处理器传入反归一化阈值
        String picFilterType = param.getPicFilterType(); //交并集选择，1-交集，2-并集
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        long t1 = System.currentTimeMillis();
        String jsonstr = "";
        List<Get> listGets = new ArrayList<Get>();
        // List<FssOutputData> outList = new ArrayList<FssOutputData>();
        List<TrialOutputData> outList = new ArrayList<TrialOutputData>();
        TrialOutputDatas fssDatas = new TrialOutputDatas();

        // 增加协议异常判断！
        if (features.isEmpty() || param.getSimThreshold() < 0 || startTime.equals("")
                || endTime.equals("") || sdf.parse(startTime).getTime() > sdf.parse(endTime).getTime()
                || (!param.getSortType().equals("1") && !param.getSortType().equals("2"))
                || (!param.getSortOrder().equals("asc") && !param.getSortOrder().equals("desc"))
                || (!param.getPicFilterType().equals("1") && !param.getPicFilterType().equals("2"))) {
            TrialReportServiceOut serviceOut = new TrialReportServiceOut();
            serviceOut.setId(HBaseManager.SearchId.SearchByTrial.getId());
            serviceOut.setErrorCode(FssErrorCodeEnum.HBASE_INVALID_PARAM.getCode());
            TrialJsonOutput outData = new TrialJsonOutput();
            outData.setReportService(serviceOut);
            Object jsonObject = JSON.toJSON(outData);
            jsonstr = JSON.toJSONString(jsonObject);
            return jsonstr;
        }

        ImageSearchParam searchParam = new ImageSearchParam();
        searchParam.setStartTime(startTime);
        searchParam.setEndTime(endTime);
        searchParam.setThreshold(hbaseThreshold);
        searchParam.setSearchFeatures(features);
        searchParam.setSearchType("3");
        searchParam.setSelType(picFilterType);
        if (cameraIds != null && !cameraIds.isEmpty()) {
            searchParam.setCameraIds(cameraIds);
        }

        if (param.getPage().getIndex().equals("1") || suspectList.isEmpty()) {
            // 以图搜图协处理器，返回gpsx和gpsy
            try {
                ImageSearchClient client1 = new ImageSearchClient();
                suspectList = client1.getSearchByImageResult(HBaseConfig.getTable(hbaseTableName), searchParam);

                // [lq-add] 2018-05-21 sensetime points read from hdfs,Normalize/reversalNormalize do in SDK
                for (ImageSearchOutData tempData : suspectList) {
                    float tempSim = tempData.getSuspectSim();
                    if (tempSim > 0f) {
                        tempData.setSuspectSim(fc.Normalize(tempSim));
                    }
                }
            } catch (Throwable e) {
                log.error(e);
               // e.printStackTrace();
            }

            // long sortTime1 = System.currentTimeMillis();
            // sortType = param.getSortType();
            sortOrder = param.getSortOrder();
            getCatchTimeSortList(suspectList);
        }

        // 保存数据
        count = suspectList.size(); // 总记录数
        if (count > 0) {
            String cameraIdTemp = null;
            Iterator<ImageSearchOutData> it = suspectList.iterator();
            while (it.hasNext()) {
                ImageSearchOutData cur = it.next();
                if ((null == cameraIdTemp) || ((null != cameraIdTemp) && (!(cur.getCameraId().equals(cameraIdTemp))))) {
                    TrialOutputData info1 = new TrialOutputData();
                    info1.setGpsx(cur.getGpsx());
                    info1.setGpsy(cur.getGpsy());
                    info1.setCameraId(cur.getCameraId());
                    outList.add(info1);
                    cameraIdTemp = cur.getCameraId();
                }
            }
        }
        fssDatas.setSearchDatas(outList);

        // 输出总时间
        long t2 = System.currentTimeMillis() - t1;
        //System.out.println("查询耗时：" + t2 + " 毫秒");

        // 异常判断
        if (exception.equals("Exception")) {
            TrialReportServiceOut serviceOut = new TrialReportServiceOut();
            serviceOut.setId(HBaseManager.SearchId.SearchByTrial.getId());
            serviceOut.setErrorCode(FssErrorCodeEnum.HBASE_TIMEOUT.getCode());
            TrialJsonOutput outData = new TrialJsonOutput();
            outData.setReportService(serviceOut);
            Object jsonObject = JSON.toJSON(outData);
            jsonstr = JSON.toJSONString(jsonObject);
            return jsonstr;
        }
        // 无异常则输出结果
        TrialReportServiceOut serviceOut = new TrialReportServiceOut();
        serviceOut.setId(HBaseManager.SearchId.SearchByTrial.getId());
        serviceOut.setType("response");
        serviceOut.setUsedTime(String.valueOf(t2));
        serviceOut.setFssSearchLists(fssDatas);
        serviceOut.setErrorCode(FssErrorCodeEnum.SUCCESS.getCode());
        serviceOut.setCount(outList.size());
        // 查询结果转为json格式
        TrialJsonOutput outData = new TrialJsonOutput();
        outData.setReportService(serviceOut);
        Object jsonObject = JSON.toJSON(outData);
        jsonstr = JSON.toJSONString(jsonObject);
        return jsonstr;
    }

    //查询结果按相似度排序
    public void getSimSortList(List<ImageSearchOutData> list) {
        Collections.sort(list, new Comparator<ImageSearchOutData>() {
            @Override
            public int compare(ImageSearchOutData o1, ImageSearchOutData o2) {
                Float sim1 = o1.getSuspectSim();
                Float sim2 = o2.getSuspectSim();
                if (sortOrder.equals("asc")) {
                    return sim1.compareTo(sim2);
                } else if (sortOrder.equals("desc")) {
                    return sim2.compareTo(sim1);
                }
                return sim2.compareTo(sim1); //默认降序
            }
        });
    }

    // 查询结果降序排序
    public void getCatchTimeSortList(List<ImageSearchOutData> list) {
        // List<ImageSearchOutData> sorted = new ArrayList<ImageSearchOutData>();
        // sortList赋值
//         for (ImageSearchOutData res : list) { sorted.add(res); }


        // 抓拍时间enter_time降序排序
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
                return catchTime2.compareTo(catchTime1); //默认降序
            }
        });
    }

    // 解析web传入查询条件
    private void parseJsonParams(String jsonParamStr) throws Exception {
        FssJsonInput inputParam = JSON.parseObject(jsonParamStr, FssJsonInput.class);
        FssReportServiceIn service = inputParam.getReportService();
        HBaseManager.SearchId id = HBaseManager.SearchId.getSearchId(service.getId());
        String type = service.getType();
        if (id.equals(HBaseManager.SearchId.SearchByTrial) && type.equals("request")) { // 12007
            param = service.getFssSearch(); // 获取查询条件
            validateArg(param);
        }
    }

    private void validateArg(FssInPutParams param) throws IOException {
        if (param == null) {
            throw new IOException("FssInPutParams Exception: Param is null");
        } else {
            if (StringUtils.isEmpty(param.getStartTime())) {
                throw new IllegalArgumentException("开始时间不能为空！");
            }
            if (StringUtils.isEmpty(param.getEndTime())) {
                throw new IllegalArgumentException("结束时间不能为空！");
            }
            String timeDelta = null;
            try {
                timeDelta = DateTimeFun.getTimeDelta(param.getStartTime(), param.getEndTime());
            } catch (ParseException e) {
                throw new IllegalArgumentException("时间格式错误！");
            }
            if (timeDelta != null && Float.parseFloat(timeDelta) > 366 * 24 * 60) {
                throw new IllegalArgumentException("时间范围不能超过一年！");
            }
            if (param.getSearchFeatures().size() > 3) {
                throw new IllegalArgumentException("图片数量不能超过3张！");
            }
            if (!StringUtils.equals(param.getPicFilterType(), "1")
                    && !StringUtils.equals(param.getPicFilterType(), "2")) {
                throw new IllegalArgumentException("交并集类型取值错误！");
            }
            if (!StringUtils.equals(param.getSortOrder(), "asc")
                    && !StringUtils.equals(param.getSortOrder(), "desc")) {
                throw new IllegalArgumentException("排序类型取值错误！");
            }
            if (!StringUtils.equals(param.getSortType(), "1")
                    && !StringUtils.equals(param.getSortType(), "2")) {
                throw new IllegalArgumentException("排序方式取值错误！");
            }
            if (/*param.getSimThreshold() < 89 ||*/ param.getSimThreshold() > 100) {
                throw new IllegalArgumentException("相似度阈值取值错误！");
            }
        }
    }
}

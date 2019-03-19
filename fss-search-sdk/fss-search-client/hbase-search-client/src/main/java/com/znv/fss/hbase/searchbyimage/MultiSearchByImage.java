package com.znv.fss.hbase.searchbyimage;

import com.alibaba.fastjson.JSON;
import com.znv.fss.common.VConstants;
import com.znv.fss.common.errorcode.FssErrorCodeEnum;
import com.znv.fss.common.utils.FeatureCompUtil;
import com.znv.fss.hbase.MultiHBaseSearch;
import com.znv.fss.hbase.HBaseConfig;
import com.znv.fss.hbase.HBaseManager;

import com.znv.hbase.client.coprocessor.ImageSearchClient;
import com.znv.hbase.client.featureComp.ImageSearchParam;
import com.znv.hbase.coprocessor.endpoint.searchByImage.ImageSearchOutData;
import com.znv.fss.hbase.utils.DateTimeFun;
import com.znv.hbase.util.PhoenixConvertUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


/**
 * Created by Estine on 2016/12/22. rowkey:salting-id-time-trackId-taskId-resultIdx
 */
public class MultiSearchByImage extends MultiHBaseSearch {
    private final Log LOG = LogFactory.getLog(MultiSearchByImage.class);
    private final String schemaName = HBaseConfig.getProperty(VConstants.FSS_PHOENIX_SCHEMA_NAME);
    private final String hbaseTableName = schemaName + ":" + HBaseConfig.getProperty(VConstants.FSS_HISTORY_V113_TABLE_NAME); // FSS_imageData_searchByImage
    // private static final float hbaseThrehold = 0.485547f; // 0.8900057
    private FssInPutParams param = new FssInPutParams();
    private List<ImageSearchOutData> suspectList = new ArrayList<ImageSearchOutData>();

    private FeatureCompUtil fc = new FeatureCompUtil();


    private int pageIndex = 0;
    private int pageSize = 0;
    private int count = 0; // 总记录数
    private int totalPage = 0; // 总页数
    private String exception = "";
    private String sortType = ""; // 新增排序方式，0：相似度倒排序，1：抓拍时间倒排序
    private String sortOrder = ""; // asc：升序，desc：降序

    public MultiSearchByImage() {
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

        // [lq-modify] 2018-05-21 sensetime points read from hdfs,Normalize/reversalNormalize do in SDK
        // float hbaseThreshold = param.getSimThreshold() / 100.00f;
        FeatureCompUtil fc = new FeatureCompUtil();
        fc.setFeaturePoints(HBaseConfig.getFeaturePoints());
        float hbaseThreshold = fc.reversalNormalize(param.getSimThreshold() / 100.00f); // 协处理器传入反归一化阈值

        String picFilterType = param.getPicFilterType(); //交并集选择，1-交集，2-并集
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        long t1 = System.currentTimeMillis();
        String jsonstr = "";
        List<Get> listGets = new ArrayList<Get>();
        List<FssOutputData> outList = new ArrayList<FssOutputData>();
        FssOutputDatas fssDatas = new FssOutputDatas();
        String pictureExist = "false"; // 存在符合相似度要求的图片？

        // 增加协议异常判断！
        if (features.isEmpty() || param.getSimThreshold() < 0 || startTime.equals("")
                || endTime.equals("") || sdf.parse(startTime).getTime() > sdf.parse(endTime).getTime()
                || (!param.getSortType().equals("1") && !param.getSortType().equals("2"))
                || (!param.getSortOrder().equals("asc") && !param.getSortOrder().equals("desc"))
                || (!param.getPicFilterType().equals("1") && !param.getPicFilterType().equals("2"))) {
            FssReportServiceOut serviceOut = new FssReportServiceOut();
            serviceOut.setId(HBaseManager.SearchId.SearchByImage.getId());
            serviceOut.setErrorCode(FssErrorCodeEnum.HBASE_INVALID_PARAM.getCode());
            serviceOut.setPictureExist(pictureExist);
            FssJsonOutput outData = new FssJsonOutput();
            outData.setReportService(serviceOut);
            Object jsonObject = JSON.toJSON(outData);
            jsonstr = JSON.toJSONString(jsonObject);
            return jsonstr;
        }

        /** 分页显示，查询下一页 */
        if (!suspectList.isEmpty()) {
            pictureExist = "true";
            List<ImageSearchOutData> topSortList = new ArrayList<ImageSearchOutData>();
            int startIndex = (pageIndex - 1) * pageSize;
            if (startIndex >= suspectList.size()) { // 返回结果为空
                return jsonstr;
            }
            int stopIndex = pageIndex * pageSize;
            if (stopIndex > suspectList.size()) {
                stopIndex = suspectList.size();
            }
            for (int idx = startIndex; idx < stopIndex; idx++) {
                topSortList.add(suspectList.get(idx));
            }
            listGets = getHbaseRows(topSortList);
            outList = getHbaseResult(listGets, topSortList); // 批量获取图片相关信息
            fssDatas.setSearchDatas(outList);

            long t2 = System.currentTimeMillis() - t1;
            //System.out.println("查询耗时：" + t2 + " 毫秒");
            // 无异常则输出结果
            FssReportServiceOut serviceOut = new FssReportServiceOut();
            serviceOut.setId(HBaseManager.SearchId.SearchByImage.getId());
            serviceOut.setType("response");
            serviceOut.setPictureExist(pictureExist);
            serviceOut.setUsedTime(String.valueOf(t2));
            serviceOut.setFssSearchLists(fssDatas);
            serviceOut.setErrorCode(FssErrorCodeEnum.SUCCESS.getCode());
            serviceOut.setCount(count);
            serviceOut.setTotalPage(totalPage);
            // 查询结果转为json格式
            FssJsonOutput outData = new FssJsonOutput();
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
        searchParam.setSearchType("1");
        searchParam.setSelType(picFilterType);
        if (cameraIds != null && !cameraIds.isEmpty()) {
            searchParam.setCameraIds(cameraIds);
        }

        // 以图搜图协处理器，返回疑似图片特征值、相似度
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
            LOG.error(e);
           // e.printStackTrace();
        }

        // long sortTime1 = System.currentTimeMillis();
        sortType = param.getSortType();
        sortOrder = param.getSortOrder();
        if (sortType.equals("2")) { // 1：相似度排序，2：抓拍时间排序
            getCatchTimeSortList(suspectList);
        } else if (sortType.equals("1")) {
            getSimSortList(suspectList);
        }
        // long sortTime2 = System.currentTimeMillis();
        // System.out.println("排序耗时：" + (sortTime2 - sortTime1) + " 毫秒");

        // 保存数据
        count = suspectList.size(); // 总记录数
        totalPage = count % pageSize == 0 ? count / pageSize : count / pageSize + 1;
        if (suspectList.size() > pageSize) {
            pictureExist = "true";
            List<ImageSearchOutData> topSortList = new ArrayList<ImageSearchOutData>();

            int startIndex = (pageIndex - 1) * pageSize;
            if (startIndex >= suspectList.size()) { // 返回结果为空
                return jsonstr;
            }
            int stopIndex = pageIndex * pageSize;
            if (stopIndex > suspectList.size()) {
                stopIndex = suspectList.size();
            }

            for (int idx = startIndex; idx < stopIndex; idx++) {
                topSortList.add(suspectList.get(idx));
            }
            listGets = getHbaseRows(topSortList); // 批量获取rowkey
            outList = getHbaseResult(listGets, topSortList); // 批量获取图片相关信息
        } else if (suspectList.size() > 0) {
            pictureExist = "true";
            listGets = getHbaseRows(suspectList);
            outList = getHbaseResult(listGets, suspectList);
        }
        fssDatas.setSearchDatas(outList);


        // 输出第一页
        long t2 = System.currentTimeMillis() - t1;
        //System.out.println("查询耗时：" + t2 + " 毫秒");

        // 异常判断
        if (exception.equals("Exception")) {
            FssReportServiceOut serviceOut = new FssReportServiceOut();
            serviceOut.setId(HBaseManager.SearchId.SearchByImage.getId());
            serviceOut.setErrorCode(FssErrorCodeEnum.HBASE_TIMEOUT.getCode());
            serviceOut.setPictureExist(pictureExist);
            FssJsonOutput outData = new FssJsonOutput();
            outData.setReportService(serviceOut);
            Object jsonObject = JSON.toJSON(outData);
            jsonstr = JSON.toJSONString(jsonObject);
            return jsonstr;
        }
        // 无异常则输出结果
        FssReportServiceOut serviceOut = new FssReportServiceOut();
        serviceOut.setId(HBaseManager.SearchId.SearchByImage.getId());
        serviceOut.setType("response");
        serviceOut.setPictureExist(pictureExist);
        serviceOut.setUsedTime(String.valueOf(t2));
        serviceOut.setFssSearchLists(fssDatas);
        //serviceOut.setErrorCode(JsonResultType.SUCCESS);
        serviceOut.setErrorCode(FssErrorCodeEnum.SUCCESS.getCode());
        serviceOut.setCount(count);
        serviceOut.setTotalPage(totalPage);
        // 查询结果转为json格式
        FssJsonOutput outData = new FssJsonOutput();
        outData.setReportService(serviceOut);
        Object jsonObject = JSON.toJSON(outData);
        jsonstr = JSON.toJSONString(jsonObject);
        return jsonstr;

    }

    // 解析hbase表相关字段和相似度
    private List<FssOutputData> getHbaseResult(List<Get> listGets, List<ImageSearchOutData> sortList) {
        List<FssOutputData> outList = new ArrayList<FssOutputData>();

        try {
            Table table = HBaseConfig.getTable(hbaseTableName);
            Result[] rs = table.get(listGets);
            int idx = 0;
//            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//            Calendar cal = Calendar.getInstance();
//            Base64 base64 = new Base64();
            for (Result r : rs) {
                FssOutputData info = new FssOutputData();
                Cell[] cells = r.rawCells();
                byte[] enterTimeConv = Bytes.copy(r.getRow(), 1, VConstants.DATE_TIME_STR_LENGTH); // 盐值占一个字节
                String uuid = Bytes.toString(r.getRow(), VConstants.DATE_TIME_STR_LENGTH + 2); // 盐值+间隔符共占两个字节
                info.setUuid(uuid);
                info.setTimeStamp(Bytes.toString(PhoenixConvertUtil.convertDescField(enterTimeConv)));
                info.setSim(sortList.get(idx).getSuspectSim()); // 获取相似度
                int len = cells.length;
                for (int i = 0; i < len; i++) {
                    Cell cell = cells[i];
                    String col = Bytes.toString(CellUtil.cloneQualifier(cell)).toLowerCase();
                    byte[] value0 = CellUtil.cloneValue(cell);
                    String value = Bytes.toString(CellUtil.cloneValue(cell));
                    switch (col) {
//                        case "rt_image_data":
//                            String imageData = base64.encodeBase64String(value0);
//                            info.setImageData(imageData);
//                            break;
                        case "task_dx":
                            info.setTaskIdx(value);
                            break;
                        case "track_idx":
                            info.setTrackIdx(value);
                            break;
                        case "camera_id":
                            info.setCameraId(value); //int
                            break;
                        case "camera_name":
                            info.setCameraName(value);
                            break;
                        case "office_id":
                            info.setOfficeId(value);
                            break;
                        case "office_name":
                            info.setOfficeName(value);
                            break;
                        case "img_url":
                            info.setImg_url(value);
                            break;
                        case "pitch":
                            info.setPitch(Float.intBitsToFloat((Bytes.toInt(value0) ^ 0x80000001))); //float
                            break;
                        case "roll":
                            info.setRoll(Float.intBitsToFloat((Bytes.toInt(value0) ^ 0x80000001))); //float
                            break;
                        case "yaw":
                            info.setYaw(Float.intBitsToFloat((Bytes.toInt(value0) ^ 0x80000001))); //float
                            break;
                        case "gpsx":
                            info.setGpsx(Float.intBitsToFloat((Bytes.toInt(value0) ^ 0x80000001))); //float
                            break;
                        case "gpsy":
                            info.setGpsy(Float.intBitsToFloat((Bytes.toInt(value0) ^ 0x80000001))); //float
                            break;
                        case "quality_score":
                            info.setQualityScore(Float.intBitsToFloat((Bytes.toInt(value0) ^ 0x80000001))); //float
                            break;
                        case "beard":
                            info.setBeard(Bytes.toInt(value0)); //int
                            break;
                        case "emotion":
                            info.setEmotion(Bytes.toInt(value0)); //int
                            break;
                        case "eye_open":
                            info.setEyeOpen(Bytes.toInt(value0)); //int
                            break;
                        case "gender":
                            info.setGender(Bytes.toInt(value0)); //int
                            break;
                        case "glass":
                            info.setGlass(Bytes.toInt(value0)); //int
                            break;
                        case "img_height":
                            info.setImgHeight(Bytes.toInt(value0)); //int
                            break;
                        case "img_width":
                            info.setImgWidth(Bytes.toInt(value0)); //int
                            break;
                        case "left_pos":
                            info.setLeft(Bytes.toInt(value0)); //int
                            break;
                        case "mask":
                            info.setMask(Bytes.toInt(value0)); //int
                            break;
                        case "mouth_open":
                            info.setMouthOpen(Bytes.toInt(value0)); //int
                            break;
                        case "race":
                            info.setRace(Bytes.toInt(value0)); //int
                            break;
                        case "top":
                            info.setTop(Bytes.toInt(value0)); //int
                            break;
                        case "frame_index":
                            info.setFrameIndex(Bytes.toInt(value0)); //int
                            break;
                        case "right_pos":
                            info.setRight(Bytes.toInt(value0)); //int
                            break;
                        case "bottom":
                            info.setBottom(Bytes.toInt(value0)); //int
                            break;
                        case "lib_id":
                            info.setLib_id(Bytes.toInt(value0)); //int
                            break;
                        case "person_id":
                            info.setPerson_id(value);
                            break;
                        case "is_alarm":
                            info.setIs_alarm(value);
                            break;
                        case "big_picture_uuid":
                            info.setBig_picture_uuid(value);
                            break;
                        case "similarity":
//                            info.setSimilarity(Float.intBitsToFloat((Bytes.toInt(value0) ^ 0x80000001)));
                            info.setSimilarity(Bytes.toFloat(value0));
                            break;
                        default:
                            break;
                    }
                }
                outList.add(info);
                idx++;
            }

        } catch (IOException e) {
            LOG.error(e);
           // e.printStackTrace();
        }
        return outList;
    }

    // 批量获取hbase表相关行
    private List<Get> getHbaseRows(List<ImageSearchOutData> sortList) {
        // List<FssOutputData> results = new ArrayList<FssOutputData>();
        List<Get> listGets = new ArrayList<Get>();
        for (ImageSearchOutData sortRes : sortList) {
            Get get = new Get(sortRes.getSuspectRowKey());
            get.addFamily(Bytes.toBytes("ATTR")); //hy added 不需要获取图片
            listGets.add(get);
        }
        return listGets;
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

    // 查询结果按抓拍时间排序
    public void getCatchTimeSortList(List<ImageSearchOutData> list) {
        // List<ImageSearchOutData> sorted = new ArrayList<ImageSearchOutData>();
        // sortList赋值
        /*
         * for (ImageSearchOutData res : list) { sorted.add(res); }
         */
        // 抓拍时间排序
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
        if (id.equals(HBaseManager.SearchId.SearchByImage) && type.equals("request")) { // 12001
            param = service.getFssSearch(); // 获取查询条件
            validateArg(param);
            pageIndex = Integer.parseInt(param.getPage().getIndex());
            if (pageIndex < 1) {
                pageIndex = 1;
            }
            pageSize = Integer.parseInt(param.getPage().getSize());
            if (pageSize < 10) {
                pageSize = 10;
            }
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

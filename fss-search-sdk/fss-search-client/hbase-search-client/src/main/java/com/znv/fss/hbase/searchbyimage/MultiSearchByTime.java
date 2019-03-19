package com.znv.fss.hbase.searchbyimage;

import com.alibaba.fastjson.JSON;
import com.znv.fss.common.VConstants;
import com.znv.fss.common.errorcode.FssErrorCodeEnum;
import com.znv.fss.hbase.HBaseConfig;
import com.znv.fss.hbase.HBaseManager;
import com.znv.fss.hbase.MultiHBaseSearch;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.filter.BinaryComparator;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


/**
 * Created by estine on 2017/3/8.
 */
public class MultiSearchByTime extends MultiHBaseSearch {
    private final Log log = LogFactory.getLog(MultiSearchByTime.class);
    private final String hbaseTableName = HBaseConfig.getProperty(VConstants.FSS_HISTORY_V113_TABLE_NAME);
    private FssInPutParams param = new FssInPutParams();
    private List<Row> outLists = new CopyOnWriteArrayList<Row>();
    private List<String> exceptionList = new CopyOnWriteArrayList<String>();
    private Map<String, String> mapkey = new LinkedHashMap<String, String>();
    private static int maxPageSize = 500; // 返回最大记录数
    private int pageSize = 0;
    private int pageIndex = 1;
    private static int searchPageNum = 5;
    private static int morePageNum = 2;
    private boolean hasMorePage = true;
    private int firstSearchPage = 0;
    private int morePage = 0;
    private boolean searchDataFromHbase = true;
    private static String sortType = "1"; // 只按 1：抓拍时间倒排序
    private List<Row> keyList = new ArrayList<Row>(); // 保存排序后的rowKey
    private List<Row> totalKeyList = new ArrayList<Row>(); // 缓存所有rowkey

    public MultiSearchByTime() {
        super("search");
    }

    @Override
    public String requestSearch(String jsonParamStr) throws Exception {
        // 解析json协议
        parseJsonParams(jsonParamStr); // 查询图片特征值
        // 解析查询条件
        String startTime = param.getStartTime(); // 开始时间
        String endTime = param.getEndTime(); // 结束时间
        String jsonstr = "";
        long t1 = System.currentTimeMillis();

        // 返回下一页
        if (pageIndex > 1 || totalKeyList.size() > 0) {
            // 批量获取下一页数据集
            List<FssOutputData> pageData = getNextPageRecord(); // 先获取判断条件，是否需要开始新一轮查询
            if (!searchDataFromHbase) {
                FssOutputDatas fssDatas = new FssOutputDatas();
                fssDatas.setSearchDatas(pageData);
                // 获取json格式结果
                jsonstr = getJsonResult(t1, fssDatas);
                if (jsonstr != null && !jsonstr.equals("")) {
                    return jsonstr;
                }
            }
        }

        // 获取cameraId过滤条件
        List<String> cameraIds = param.getCameraIds(); // 多个cameraId
        FilterList filterList1 = new FilterList(FilterList.Operator.MUST_PASS_ONE);
        final byte[] family = Bytes.toBytes("feature"); // 图片特征值列族 cameraId列
        if (cameraIds.size() > 0) {
            for (String cameraId : cameraIds) {
                BinaryComparator comp = new BinaryComparator(Bytes.toBytes(cameraId));
                SingleColumnValueFilter filter = new SingleColumnValueFilter(family, Bytes.toBytes("cameraId"),
                    CompareFilter.CompareOp.EQUAL, comp);
                filter.setFilterIfMissing(true);
                filterList1.addFilter(filter);
            }
        }

        // 清空上一次查询结果
        if (keyList != null && !keyList.isEmpty()) {
            keyList.clear();
        }
        while (keyList != null && keyList.size() < pageSize * (searchPageNum + 1)) {
            // 初始化countDown
            int poolnum = Integer.parseInt(HBaseConfig.getProperty(VConstants.HISTORY_SALT_BUCKETS));
            ExecutorService fixedThreadPool = Executors.newFixedThreadPool(poolnum);
            CountDownLatch threadSignal = new CountDownLatch(poolnum);
            outLists.clear();
            exceptionList.clear();

            for (int i = 10; i < 46; i++) {
                String key = String.format("%02d", i);
                byte[] lastId = null;
                // getLastRowKey();
                if (mapkey != null && !mapkey.isEmpty()) {
                    if (mapkey.containsKey(key)) {
                        String value = mapkey.get(key);
                        lastId = Bytes.toBytes(String.format("%s-%s", key, value + 1));
                    }
                }
                Thread t = new SearchByTime(threadSignal, key, startTime, endTime, filterList1, outLists, exceptionList,
                    lastId, pageSize * (searchPageNum + 1)); // 一次查询pageSize * (searchPageNum + 1)条，保证排序正确
                fixedThreadPool.execute(t);
                // t.start();

            }

            // 等待所有子线程执行完
            try {
                threadSignal.await(/*(long) (1000 * 60), TimeUnit.MILLISECONDS*/); // 设置60秒超时等待
            } catch (InterruptedException e) {
                log.error(e);
                Thread.currentThread().interrupt();
                fixedThreadPool.shutdownNow();
               // e.printStackTrace();
            }
            fixedThreadPool.shutdown();

            // 打印结束标记
            log.debug(Thread.currentThread().getName() + "结束.");
            // 按时间排序
            if (sortType.equals("1")) { // 为当次查询返回结果集排序
                getCatchTimeSortList(outLists);
            }
            // 保存各region的key
            saveRegionRowKey(); // keyList 获取前6页（排序后结果）

            // 新查询所有region查询结果< 每页行数，则表示查询已完全结束
            // 一次查询pageSize * (searchPageNum + 1)条，保证排序正确
            if (outLists.size() < pageSize * (searchPageNum + 1)) {
                break;
            }
        }
        // 缓存新的查询结果
        if (keyList != null && keyList.size() > 0) {
            // 批量获取6页数据的key
            totalKeyList.addAll(keyList);
            // 减少缓存
            keyList.clear();
        }
        outLists.clear();

        // 分批返回，获取一页数据
        List<FssOutputData> pageData = new ArrayList<FssOutputData>();
        pageData = getFirstPageRecord();
        FssOutputDatas fssDatas = new FssOutputDatas();
        fssDatas.setSearchDatas(pageData);
        // 获取json格式结果
        jsonstr = getJsonResult(t1, fssDatas);

        return jsonstr;
    }

    // 解析hbase表相关字段和相似度
    private List<FssOutputData> getHbaseResult(List<Get> listGets) {
        List<FssOutputData> outList = new ArrayList<FssOutputData>();
        try {
            Table table = HBaseConfig.getTable(hbaseTableName);
            Result[] rs = table.get(listGets);
            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Calendar cal = Calendar.getInstance();
            Base64 base64 = new Base64();
            for (Result r : rs) {
                FssOutputData info = new FssOutputData();
                Cell[] cells = r.rawCells();
                String row = Bytes.toString(r.getRow());
                String[] keys = row.split("-"); // salting-time-trackId-taskId-resultId
                String catchTime = keys[1];
                long dt = Long.MAX_VALUE - Long.parseLong(catchTime);
                cal.setTimeInMillis(dt);
                info.setTimeStamp((sdf1.format(cal.getTime())));
                // System.out.println("抓拍时间：" + (sdf1.format(cal.getTime()))); // test lq
                int len = cells.length;
                for (int i = 0; i < len; i++) {
                    Cell cell = cells[i];
                    String col = Bytes.toString(CellUtil.cloneQualifier(cell));
                    String value = Bytes.toString(CellUtil.cloneValue(cell));
                    byte[] value0 = CellUtil.cloneValue(cell);
                    switch (col) {
                        case "imageData":
                            String imageData = base64.encodeBase64String(value0);
                            info.setImageData(imageData);
                            break;
                        case "taskIdx":
                            info.setTaskIdx(value);
                            break;
                        case "trackIdx":
                            info.setTrackIdx(value);
                            break;
//                        case "resultIdx":
//                            info.setResultIdx(value);
//                            break;
                        case "cameraId":
                            info.setCameraId(value);
                            // System.out.println("cameraId :" + value); // test lq
                            break;
                        case "cameraName":
                            info.setCameraName(value);
                            break;
                        case "officeId":
                            info.setOfficeId(value);
                            break;
                        case "officeName":
                            info.setOfficeName(value);
                            break;
                        case "imgUrl":
                            info.setImg_url(value);
                            break;
                        case "pitch":
                            info.setPitch(Bytes.toInt(CellUtil.cloneValue(cell)) ^ 0x80000000);
                            break;
                        case "roll":
                            info.setRoll(Bytes.toInt(CellUtil.cloneValue(cell)) ^ 0x80000000);
                            break;
//                        case "frameTime":
//                            info.setFrameTime(value);
//                            break;
                        case "yaw":
                            info.setYaw(Float.parseFloat(value));
                            break;
//                        case "imageName": // 人脸图像名称
//                            info.setImageName(value);
//                            break;
                        case "gpsx":
                            info.setGpsx(Float.parseFloat(value));
                            break;
                        case "gpsy":
                            info.setGpsy(Float.parseFloat(value));
                            break;
                        case "qualityScore":
                            info.setQualityScore(Float.parseFloat(value));
                            break;
//                        case "age":
//                            info.setAge(Integer.parseInt(value));
//                            break;
                        case "beard":
                            info.setBeard(Integer.parseInt(value));
                            break;
                        case "emotion":
                            info.setEmotion(Integer.parseInt(value));
                            break;
                        case "eyeOpen":
                            info.setEyeOpen(Integer.parseInt(value));
                            break;
                        case "gender":
                            info.setGender(Integer.parseInt(value));
                            break;
                        case "glass":
                            info.setGlass(Integer.parseInt(value));
                            break;
                        case "imgHeight":
                            info.setImgHeight(Integer.parseInt(value));
                            break;
                        case "imgWidth":
                            info.setImgWidth(Integer.parseInt(value));
                            break;
                        case "left":
                            info.setLeft(Integer.parseInt(value));
                            break;
                        case "mask":
                            info.setMask(Integer.parseInt(value));
                            break;
                        case "mouthOpen":
                            info.setMouthOpen(Integer.parseInt(value));
                            break;
                        case "race":
                            info.setRace(Integer.parseInt(value));
                            break;
                        case "top":
                            info.setTop(Integer.parseInt(value));
                            break;
                        case "frameIndex":
                            info.setFrameIndex(Integer.parseInt(value));
                            break;
                        case "right":
                            info.setRight(Integer.parseInt(value));
                            break;
                        case "bottom":
                            info.setBottom(Integer.parseInt(value));
                            break;
                        default:
                            break;
                    }
                }
                outList.add(info);
            }

        } catch (IOException e) {
            log.error(e);
           // e.printStackTrace();
        }
        return outList;
    }

    private List<FssOutputData> getFirstPageRecord() {
        List<FssOutputData> pageList = new ArrayList<FssOutputData>();
        List<Get> listGets = new ArrayList<Get>();
        // 已经包含了第一页
        int startIndex = (pageIndex - 1) * pageSize;
        int stopIndex = pageIndex * pageSize;
        if (startIndex > totalKeyList.size()) {
            // 没有数据，返回空
            hasMorePage = false;
        } else {
            if (stopIndex <= (totalKeyList.size() - morePageNum * pageSize)) {
                hasMorePage = true;
                for (int idx = startIndex; idx < stopIndex; idx++) {
                    String rk = totalKeyList.get(idx).getSuspectKey();
                    Get get = new Get(Bytes.toBytes(rk));
                    listGets.add(get);
                }
            } else if (stopIndex > (totalKeyList.size() - morePageNum * pageSize)) {
                // 没有后两页
                hasMorePage = false;
                if (stopIndex > totalKeyList.size()) {
                    stopIndex = totalKeyList.size();
                }
                for (int idx = startIndex; idx < stopIndex; idx++) {
                    String rk = totalKeyList.get(idx).getSuspectKey();
                    Get get = new Get(Bytes.toBytes(rk));
                    listGets.add(get);
                }
            }
        }
        // 标记第一次查询的页数
        if (pageIndex <= 1) {
            if (totalKeyList.size() % pageSize == 0) {
                firstSearchPage = totalKeyList.size() / pageSize;
            } else {
                firstSearchPage = totalKeyList.size() / pageSize + 1;
            }
            if (firstSearchPage < (searchPageNum + 1)) {
                hasMorePage = false;
            }
        }
        // 标记最后还有几页
        if ((totalKeyList.size() - stopIndex) % pageSize == 0) {
            morePage = (totalKeyList.size() - stopIndex) / pageSize;
        } else {
            morePage = (totalKeyList.size() - stopIndex) / pageSize + 1;
        }
        if (morePage > morePageNum) {
            morePage = morePageNum;
        }
        if (morePage < 0) {
            morePage = 0;
        }
        if (!listGets.isEmpty()) {
            pageList = getHbaseResult(listGets);
        }
        return pageList;
    }

    private List<FssOutputData> getNextPageRecord() {
        List<FssOutputData> pageList = new ArrayList<FssOutputData>();
        List<Get> listGets = new ArrayList<Get>();
        // 已经包含了第一页
        int startIndex = (pageIndex - 1) * pageSize;
        int stopIndex = pageIndex * pageSize;
        if (startIndex > totalKeyList.size()) {
            // 没有数据，返回空
            searchDataFromHbase = false;
        } else {
            if (stopIndex <= (totalKeyList.size() - morePageNum * pageSize)) {
                searchDataFromHbase = false;
                for (int idx = startIndex; idx < stopIndex; idx++) {
                    String rk = totalKeyList.get(idx).getSuspectKey();
                    Get get = new Get(Bytes.toBytes(rk));
                    listGets.add(get);
                }
            } else if (stopIndex > (totalKeyList.size() - morePageNum * pageSize)) {
                if (!hasMorePage) { // 没有后两页
                    if (stopIndex > totalKeyList.size()) {
                        stopIndex = totalKeyList.size();
                    }
                    for (int idx = startIndex; idx < stopIndex; idx++) {
                        String rk = totalKeyList.get(idx).getSuspectKey();
                        Get get = new Get(Bytes.toBytes(rk));
                        listGets.add(get);
                    }
                    searchDataFromHbase = false;
                } else {
                    searchDataFromHbase = true;
                }
            }
        }

        if (!searchDataFromHbase) {
            // 标记第一次查询的页数
            if (pageIndex <= 1) {
                if (totalKeyList.size() % pageSize == 0) {
                    firstSearchPage = totalKeyList.size() / pageSize;
                } else {
                    firstSearchPage = totalKeyList.size() / pageSize + 1;
                }
                if (firstSearchPage < (searchPageNum + 1)) {
                    hasMorePage = false;
                }
            }
            // 标记最后还有几页
            if ((totalKeyList.size() - stopIndex) % pageSize == 0) {
                morePage = (totalKeyList.size() - stopIndex) / pageSize;
            } else {
                morePage = (totalKeyList.size() - stopIndex) / pageSize + 1;
            }
            if (morePage > morePageNum) {
                morePage = morePageNum;
            }
            if (morePage < 0) {
                morePage = 0;
            }
        }
        if (!listGets.isEmpty()) {
            pageList = getHbaseResult(listGets);
        }
        return pageList;
    }

    private void saveRegionRowKey() {
        for (Row regionList : outLists) {
            keyList.add(regionList);
            String[] rowKey = regionList.getSuspectKey().split("-");
            String key = rowKey[0];
            if (mapkey.containsKey(key)) {
                mapkey.remove(key);
            }
            // salting-time-trackId-taskId-resultId,salting严格为两字节，从第4字节开始是抓拍时间
            String value = regionList.getSuspectKey().substring(3, regionList.getSuspectKey().length());
            mapkey.put(key, value);

            if (keyList.size() >= (pageSize * (searchPageNum + 1))) {
                break; // 只获取前6页的结果
            }
        }

    }

    // Row中只保存rowkey
    private void getCatchTimeSortList(List<Row> list) {
        Collections.sort(list, new Comparator<Row>() {
            @Override
            public int compare(Row o1, Row o2) {
                String rowkey1 = o1.getSuspectKey();
                String rowkey2 = o2.getSuspectKey();
                String value1 = rowkey1.substring(3, rowkey1.length());
                String value2 = rowkey2.substring(3, rowkey2.length());
                return value1.compareTo(value2); // 天生倒排序
            }
        });
    }

    private String getJsonResult(long t1, FssOutputDatas fssDatas) {
        String jsonstr = "";
        if (exceptionList.size() > 0) {
            FssReportServiceOut serviceOut = new FssReportServiceOut();
            serviceOut.setId(HBaseManager.SearchId.SearchByTime.getId());
            serviceOut.setErrorCode(FssErrorCodeEnum.HBASE_SESSION_TIMEOUT.getCode());
            FssJsonOutput outData = new FssJsonOutput();
            outData.setReportService(serviceOut);
            Object jsonObject = JSON.toJSON(outData);
            jsonstr = JSON.toJSONString(jsonObject);
            return jsonstr;
        } else {
            long t2 = System.currentTimeMillis() - t1;
            System.out.println("耗时 = " + t2); //// todo test single search !
            FssReportServiceOut serviceOut = new FssReportServiceOut();
            serviceOut.setId(HBaseManager.SearchId.SearchByTime.getId());
            serviceOut.setType("request");
            serviceOut.setUsedTime(String.valueOf(t2));
            serviceOut.setFssSearchLists(fssDatas);
            serviceOut.setErrorCode(FssErrorCodeEnum.SUCCESS.getCode());
//            serviceOut.setMorePage(morePage);
//            serviceOut.setFirstSearchPage(firstSearchPage);
            FssJsonOutput outData = new FssJsonOutput();
            outData.setReportService(serviceOut);
            Object jsonObject = JSON.toJSON(outData);
            jsonstr = JSON.toJSONString(jsonObject);
            return jsonstr;
        }
    }

    // 解析web传入查询条件
    private void parseJsonParams(String jsonParamStr) {
        FssJsonInput inputParam = JSON.parseObject(jsonParamStr, FssJsonInput.class);
        FssReportServiceIn service = inputParam.getReportService();
        HBaseManager.SearchId id = HBaseManager.SearchId.getSearchId(service.getId());
        String type = service.getType();
        if (id.equals(HBaseManager.SearchId.SearchByTime) && type.equals("request")) { // 12003
            param = service.getFssSearch(); // 获取查询条件
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
        }
    }
}

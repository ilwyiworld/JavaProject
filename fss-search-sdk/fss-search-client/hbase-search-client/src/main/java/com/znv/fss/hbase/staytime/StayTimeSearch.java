package com.znv.fss.hbase.staytime;

import com.alibaba.fastjson.JSON;
import com.znv.fss.common.VConstants;
import com.znv.fss.common.errorcode.FssErrorCodeEnum;
import com.znv.fss.common.utils.FeatureCompUtil;
import com.znv.fss.hbase.MultiHBaseSearch;
import com.znv.fss.hbase.HBaseConfig;
import com.znv.fss.hbase.HBaseManager;
import com.znv.fss.hbase.JsonResultType;
import com.znv.hbase.client.staytimestat.StayTimeStatParam;
import com.znv.hbase.client.coprocessor.StayTimeStatClient;
import com.znv.hbase.client.coprocessor.StayTimeStatNewClient;
import com.znv.hbase.coprocessor.endpoint.staytimestat.StayTimeSearchOutData;
import com.znv.fss.hbase.utils.DateTimeFun;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.BinaryComparator;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.text.ParseException;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collections;
import java.util.Comparator;


/**
 * Created by ZNV on 2017/6/7.
 */
public class StayTimeSearch extends MultiHBaseSearch {
    private final String schemaName = HBaseConfig.getProperty(VConstants.FSS_PHOENIX_SCHEMA_NAME);
    private final String historyTableName = HBaseConfig.getProperty(VConstants.FSS_HISTORY_V113_TABLE_NAME);
    private String tableName = schemaName + ":" + historyTableName;
    private int size = 10;
    private int maxSize = 100;
    float simThreshold = 0.89F;
    private String analysis = "2"; //1:不分析,2,分析,3,兼容旧版本；默认2
    private String officeIds[] = null;
    private String cameraIds[] = null;
    private String startTime = null;
    private String endTime = null;
    private String reportType = "02"; // 01:摄像头ID,02局站ID,默认局站
    private static String attrFamily = "ATTR";
    private static String officeIdColumn = "OFFICE_ID";
    private static String cameraIdColumn = "CAMERA_ID";
    private List<String> exceptionList = new ArrayList<String>();
    private static byte[] phoenixGapChar = new byte[1];
    private static Log log = LogFactory.getLog(StayTimeSearch.class);
    private FeatureCompUtil fc = new FeatureCompUtil();

    public StayTimeSearch() {
        super("search");
    }

    /**
     * 获取查询结果
     */
    @Override
    public String requestSearch(String jsonParamStr) throws Exception {
        fc.setFeaturePoints(HBaseConfig.getFeaturePoints()); // [lq-add]
        long t1 = System.currentTimeMillis();
        try {
            exceptionList.clear();
            parseJsonParams(jsonParamStr);
        } catch (Exception e) {
            exceptionList.add(FssErrorCodeEnum.ES_INVALID_PARAM.getExplanation());
            return getJsonStr(t1, null);
        }
        this.phoenixGapChar[0] = (byte) 0xff;

        //旧sdk兼容旧的协处理器
        if (analysis.equals("3")) {
            return getStayTimeOResultOld();
        }

        //新的sdk兼容新web、旧web界面，对应新的协处理器
        Map<StayTimeSearchOutData, List<StayTimeSearchOutData>> statDataMap = null;
        statDataMap = getStatResult();
        if (exceptionList.size() > 0) {
            return getJsonStr(t1, new ArrayList<StayTimeOut>());
        }

        try {
            //获取leader信息
            List<StayTimeSearchOutData> leaderData = new ArrayList<>();
            if (statDataMap != null && statDataMap.size() > 0) {
                leaderData = getLeaderData(statDataMap);
            }

            //针对leader从hbase获取图片相关信息
            Map<String, StayTimeData> searchData = new LinkedHashMap<>();
            if (leaderData != null && leaderData.size() > 0) {
                searchData = getDataFromHbase(leaderData);
                if (exceptionList.size() > 0) {
                    return getJsonStr(t1, new ArrayList<StayTimeOut>());
                }
            }

            //对leader进行输出转换
            List<StayTimeOut> stayTimeOut = new ArrayList<>();
            if (searchData != null && searchData.size() > 0) {
                stayTimeOut = getStayTimeOutData(leaderData, searchData);
            }
            if (analysis.equals("2")) {
                //按摄像头对组员分类
                List<Analyses> analysesList = new ArrayList<>();
                if (statDataMap != null && statDataMap.size() > 0) {
                    analysesList = getAnalysisData(statDataMap);
                }
                //返回结果信息
                return getJsonStr(t1, stayTimeOut, analysesList);
            } else {
                //返回结果信息
                return getJsonStr(t1, stayTimeOut);
            }
        } catch (Exception e) {
            log.info(e);
            exceptionList.add("Error");
            return getJsonStr(t1, new ArrayList<StayTimeOut>());
        }
    }

    /**
     * 解析请求参数
     */
    private void parseJsonParams(String jsonParamStr) throws Exception {
        StayTimeJsonInput inputParam = JSON.parseObject(jsonParamStr, StayTimeJsonInput.class);
        StayTimeReportServiceIn service = inputParam.getReportService();
        HBaseManager.SearchId id = HBaseManager.SearchId.getSearchId(service.getId());
        String type = service.getType();
        if (id.equals(HBaseManager.SearchId.StayTimeSearch) && type.equals("request")) { // 12001
            size = service.getSize();
            if (size <= 0) {
                size = 10;
            }
            if (size > 100) {
                size = 100;
            }
            if (service.getAnalysis() == null) {
                analysis = "3";
            } else if (!service.getAnalysis().equals("")) {
                analysis = service.getAnalysis();
            } else {
                analysis = "2";
            }
            StayTimeInput stayTimeParam = service.getStayTime();
            validateArg(stayTimeParam);
            try {
//                int hbaseThreshold = stayTimeParam.getSimThreshold();
//                if (hbaseThreshold < 50) {
//                    hbaseThreshold = 50;
//                }
//                if (hbaseThreshold > 100) {
//                    hbaseThreshold = 100;
//                }
//                simThreshold = hbaseThreshold / 100.00f ; // [lq-modify]归一化
                int hbaseThreshold = 89;
                simThreshold = hbaseThreshold / 100.00f;
                simThreshold = fc.reversalNormalize(simThreshold /*/ 100.00f*/); // [lq-modify]协处理器传入反归一化阈值
                if (stayTimeParam.getReportType() != null && !stayTimeParam.getReportType().equals("")) {
                    reportType = stayTimeParam.getReportType();
                } else {
                    reportType = "02";
                }
                officeIds = stayTimeParam.getOfficeIds();
                cameraIds = stayTimeParam.getCameraIds(); // 获取查询条件
                startTime = stayTimeParam.getStartTime();
                endTime = stayTimeParam.getEndTime();
            } catch (Exception e) {
                throw new Exception();
            }
        }
    }

    private Map<StayTimeSearchOutData, List<StayTimeSearchOutData>> getStatResult() {
        Map<StayTimeSearchOutData, List<StayTimeSearchOutData>> statData = new LinkedHashMap<StayTimeSearchOutData, List<StayTimeSearchOutData>>();
        Scan newScan = new Scan();
        FilterList filterList = new FilterList(org.apache.hadoop.hbase.filter.FilterList.Operator.MUST_PASS_ONE);
        if (officeIds != null && officeIds.length > 0) {
            for (String officeId : officeIds) {
                if (officeId != null && !officeId.equals("")) {
                    BinaryComparator comp = new BinaryComparator(Bytes.toBytes(officeId));
                    SingleColumnValueFilter filter = new SingleColumnValueFilter(Bytes.toBytes(attrFamily),
                            Bytes.toBytes(officeIdColumn), CompareFilter.CompareOp.EQUAL, comp);
                    filter.setFilterIfMissing(true);
                    filterList.addFilter(filter);
                }
            }
        }
        if (cameraIds != null && cameraIds.length > 0) {
            for (String cameraId : cameraIds) {
                if (cameraId != null && !cameraId.equals("")) {
                    BinaryComparator comp = new BinaryComparator(Bytes.toBytes(cameraId));
                    SingleColumnValueFilter filter = new SingleColumnValueFilter(Bytes.toBytes(attrFamily),
                            Bytes.toBytes(cameraIdColumn), CompareFilter.CompareOp.EQUAL, comp);
                    filter.setFilterIfMissing(true);
                    filterList.addFilter(filter);
                }
            }
        }

        newScan.setFilter(filterList);
        newScan.addFamily(Bytes.toBytes(attrFamily));
        //必须在客户端添加需要过滤的条件列
        newScan.addColumn(Bytes.toBytes(attrFamily), Bytes.toBytes(officeIdColumn));
        newScan.addColumn(Bytes.toBytes(attrFamily), Bytes.toBytes(cameraIdColumn));
        newScan.setMaxVersions(1);
        newScan.setCacheBlocks(true);

        StayTimeStatParam searchParam = new StayTimeStatParam();
        searchParam.setSize(maxSize);
        searchParam.setStartTime(startTime);
        searchParam.setEndTime(endTime);
        searchParam.setThreshold(simThreshold);
        searchParam.setAnalysis(analysis);

        HTable table = null;
        try {
            table = HBaseConfig.getTable(tableName);
            StayTimeStatNewClient client = new StayTimeStatNewClient();
            statData = client.getStayTimeStatNew(table, searchParam, newScan); // 协处理器中已排序
        } catch (Throwable e) {
            exceptionList.add("Exception");
            log.error(e);
         //   e.printStackTrace();
        } finally {
            try {
                if (table != null) {
                    table.close();
                }
            } catch (IOException e) {
                exceptionList.add("IOException");
                log.error(e);
               // e.printStackTrace();
            }
        }
        return statData;
    }

    /**
     * 获取结果，rowKey(alarm_type, sub_type, enter_time, uuid)
     */
    private Map<String, StayTimeData> getDataFromHbase(List<StayTimeSearchOutData> statData) {

        Map<String, StayTimeData> outMap = new LinkedHashMap<String, StayTimeData>();
        int count = 0;

        if (statData != null && statData.size() > 0) {
            List<Get> listGets = new ArrayList<Get>();
            for (StayTimeSearchOutData tmp : statData) {
                Get get = new Get(tmp.getRowKey());
                listGets.add(get);
                count++;
                if (count == size) {
                    break;
                }
            }
            /*String phoenixGapCharStr = Bytes.toString(phoenixGapChar);
            byte[] phoenixGapChar = new byte[1];
            phoenixGapChar[0] = (byte) 0xff;
            String phoenixGapCharStr = Bytes.toString(phoenixGapChar);*/

            HTable table = null;
            try {
                table = HBaseConfig.getTable(tableName);
                Result[] rs = table.get(listGets);
                for (Result r : rs) {
                    StayTimeData info = new StayTimeData();
                    Cell[] cells = r.rawCells();

                    byte[] rowKey = r.getRow();
                    info.setRowKey(rowKey);

                    /*byte[] newRowKey = Bytes.copy(rowKey);
                    for(int i = 1; i < 20; i++){
                        newRowKey[i] = (byte)(~(int)newRowKey[i]);
                    }
                    String rowkeyinfo[] = Bytes.toString(newRowKey, 1).split(phoenixGapCharStr); // 盐值占一个字节*/


                    String rowkeyinfo[] = getRowKeyInfos(rowKey); // 盐值占一个字节*/
                    String uuid = "";
                    if (rowkeyinfo != null && rowkeyinfo.length >= 2) {
                        uuid = rowkeyinfo[1];
                    }

                    /*info.setEnter_time(rowkeyinfo[0]);*/

                    Base64 base64 = new Base64();
                    int len = cells.length;
                    for (int i = 0; i < len; i++) {
                        Cell cell = cells[i];
                        String col = Bytes.toString(CellUtil.cloneQualifier(cell));
                        String value = Bytes.toString(CellUtil.cloneValue(cell));
                        switch (col) {
                            case "CAMERA_ID":
                                info.setCamera_id(value);
                                break;
                            case "CAMERA_NAME":
                                info.setCamera_name(value);
                                break;
                            case "CAMERA_TYPE":
                                info.setCamera_type(Bytes.toInt(CellUtil.cloneValue(cell)));
                                break;
                            case "GPSX":
                                float gpsx = 0f;
                                byte[] bytegpsx = (CellUtil.cloneValue(cell));
                                if (bytegpsx != null && bytegpsx.length > 0) {
                                    gpsx = Float.intBitsToFloat(Bytes.toInt(bytegpsx) ^ 0x80000001);
                                }
                                info.setGpsx(gpsx);
                                break;
                            case "GPSY":
                                float gpsy = 0f;
                                byte[] bytegpsy = (CellUtil.cloneValue(cell));
                                if (bytegpsy != null && bytegpsy.length > 0) {
                                    gpsy = Float.intBitsToFloat(Bytes.toInt(bytegpsy) ^ 0x80000001);
                                }
                                info.setGpsy(gpsy);
                                break;
                            case "OFFICE_ID":
                                info.setOffice_id(value);
                                break;
                            case "OFFICE_NAME":
                                info.setOffice_name(value);
                                break;
                            case "RT_IMAGE_DATA3":
                                String imageData = base64.encodeBase64String(CellUtil.cloneValue(cell));
                                info.setRt_image_data(imageData);
                                break;
                            case "PERSON_ID":
                                info.setPerson_id(value);
                                break;
                            case "LIB_ID":
                                info.setLib_id(Bytes.toInt(CellUtil.cloneValue(cell)));
                                break;
                            case "LEAVE_TIME":
                                info.setLeave_time(value);
                                break;
                            case "IMG_URL":
                                info.setImg_url(value);
                                break;
                            default:
                                break;
                        }
                    }
                    outMap.put(uuid, info);
                }
            } catch (Exception e) {
                exceptionList.add("Exception");
                log.error(e);
               // e.printStackTrace();
            } catch (Error e) {
                exceptionList.add("Error");
                log.error(e);
               // e.printStackTrace();
            } finally {
                try {
                    if (table != null) {
                        table.close();
                    }
                } catch (IOException e) {
                    exceptionList.add("IOException");
                    log.error(e);
                   // e.printStackTrace();
                }
            }
        }
        return outMap;
    }

    /**
     * 获取json串
     */
    private String getJsonStr(long t1, List<StayTimeOut> tempList) {
        String jsonstr = "";
        // 捕获到异常，直接返回失败
        if (exceptionList.size() > 0) {
            StayTimeReportServiceOut serviceOut = new StayTimeReportServiceOut();
            if (exceptionList.contains("Error")) {
                serviceOut.setErrorCode(JsonResultType.ERROR.toString());
            } else if (exceptionList.contains(FssErrorCodeEnum.ES_INVALID_PARAM.getExplanation())) {
                serviceOut.setErrorCode(FssErrorCodeEnum.ES_INVALID_PARAM.getExplanation());
            } else {
                serviceOut.setErrorCode(JsonResultType.TIMEOUT.toString());
            }
            StayTimeJsonOut outData = new StayTimeJsonOut();
            outData.setReportService(serviceOut);
            Object jsonObject = JSON.toJSON(outData);
            jsonstr = JSON.toJSONString(jsonObject);
            return jsonstr;
        } else {
            long t2 = System.currentTimeMillis() - t1;
            StayTimeReportServiceOut serviceOut = new StayTimeReportServiceOut();
            if (tempList != null && !tempList.isEmpty()) {
                StayTimeOut[] res = new StayTimeOut[tempList.size()];
                serviceOut.setStayTimes(tempList.toArray(res));
                serviceOut.setCount(tempList.size());
            } else {
                serviceOut.setCount(0);
            }
            serviceOut.setId(HBaseManager.SearchId.StayTimeSearch.getId());
            serviceOut.setType("response");

            serviceOut.setTime(String.valueOf(t2));
            serviceOut.setErrorCode(JsonResultType.SUCCESS.toString());

            StayTimeJsonOut outData = new StayTimeJsonOut();
            outData.setReportService(serviceOut);

            Object jsonObject = JSON.toJSON(outData);
            jsonstr = JSON.toJSONString(jsonObject);
            return jsonstr;
        }
    }

    private static String convSpanToStr(long spanMs) {
        String rtn = "";
        int secUnit = 1000;
        int minUnit = 60 * secUnit;
        int hourUnit = 60 * minUnit;
        int dayUnit = 24 * hourUnit;
        long dayNum = 0L;
        long hourNum = 0L;
        long minNum = 0L;
        long secNum = 0L;
        long temp = spanMs;
        if (temp > dayUnit) {
            dayNum = temp / dayUnit;
            temp = temp - dayUnit * dayNum;
        }
        if (temp > hourUnit) {
            hourNum = temp / hourUnit;
            temp = temp - hourUnit * hourNum;
        }
        if (temp > minUnit) {
            minNum = temp / minUnit;
            temp = temp - minUnit * minNum;
        }
        secNum = temp / secUnit;
        if (dayNum != 0) {
            rtn += String.format("%s天", dayNum);
        }
        if (hourNum != 0) {
            rtn += String.format("%s小时", hourNum);
        }
        if (minNum != 0) {
            rtn += String.format("%s分", minNum);
        }
        if (secNum >= 0) {
            rtn += String.format("%s秒", secNum);
        }
        return rtn;
    }

    private List<StayTimeSearchOutData> getLeaderData(Map<StayTimeSearchOutData, List<StayTimeSearchOutData>> dataListMap) {
        List<StayTimeSearchOutData> leader = new ArrayList<StayTimeSearchOutData>();
        int count = 0;
        if (null != dataListMap && dataListMap.size() > 0) {
            Iterator<Map.Entry<StayTimeSearchOutData, List<StayTimeSearchOutData>>> scnItr = dataListMap.entrySet()
                    .iterator();

            while (scnItr.hasNext()) {
                Map.Entry<StayTimeSearchOutData, List<StayTimeSearchOutData>> entry = scnItr.next();
                if (null != entry) {
                    leader.add(entry.getKey());
                    count++;
                }
                if (count == size) {
                    break;
                }
            }
        }
        return leader;
    }

    /**
     * 按摄像头对组员分类
     */
    private List<Analyses> getAnalysisData(Map<StayTimeSearchOutData, List<StayTimeSearchOutData>> dataListMap) {

        List<Analyses> retDatas = new ArrayList<Analyses>();
        Map<String, Analyses> analysesMap = new LinkedHashMap<>();
        if (dataListMap != null && dataListMap.size() > 0) {
            Iterator<Map.Entry<StayTimeSearchOutData, List<StayTimeSearchOutData>>> scnItr = dataListMap.entrySet()
                    .iterator();
            int count = 0;
            while (scnItr.hasNext()) { //对每组循环
                Map.Entry<StayTimeSearchOutData, List<StayTimeSearchOutData>> entry = scnItr.next();
                if (null != entry && entry.getValue() != null && entry.getValue().size() > 0) {
                    List<StayTimeSearchOutData> personList = entry.getValue(); //每组组员
                    for (StayTimeSearchOutData personData : personList) { //对每组的组员循环
                        AnalysisData analysisData = new AnalysisData();
                        String rowkeyinfo[] = getRowKeyInfos(personData.getRowKey());
                        if (rowkeyinfo != null && rowkeyinfo.length >= 2) {
                            analysisData.setEnter_time(rowkeyinfo[0]);
                            analysisData.setUuid(rowkeyinfo[1]);
                        }
                        analysisData.setDuration_time(convSpanToStr(personData.getDurationTime() * 1000));
                        analysisData.setDuration_timel(personData.getDurationTime());
                        analysisData.setImg_url(personData.getImgUrl());

                        if (analysesMap.containsKey(personData.getCameraId())) {
                            analysesMap.get(personData.getCameraId()).getAnalysisData().add(analysisData);
                        } else {
                            Analyses analysesNew = new Analyses();
                            analysesNew.setCamera_id(personData.getCameraId());
                            analysesNew.setCamera_name(personData.getCameraName());

                            List<AnalysisData> analysisDataList = new ArrayList<AnalysisData>();
                            analysisDataList.add(analysisData);

                            analysesNew.setAnalysisData(analysisDataList);
                            analysesMap.put(personData.getCameraId(), analysesNew);
                        }
                    }
                    count++;
                }
                if (count == size) {
                    break;
                }
            }
        }

        Iterator<Map.Entry<String, Analyses>> retItr = analysesMap.entrySet().iterator();
        while (null != retItr && retItr.hasNext()) {
            Map.Entry<String, Analyses> retEntry = retItr.next();
            Analyses analyses = retEntry.getValue();
            List<AnalysisData> analysisDataList = analyses.getAnalysisData();
            // 按驻留时间排序
            Collections.sort(analysisDataList, new Comparator<AnalysisData>() {
                @Override
                public int compare(AnalysisData o1, AnalysisData o2) {
                    return Long.compare(o2.getDuration_timel(), o1.getDuration_timel());
                }
            });
            retDatas.add(analyses);
        }
        return retDatas;
    }

    /**
     * 获取json串
     */
    private String getJsonStr(long t1, List<StayTimeOut> tempList, List<Analyses> analysesList) {
        String jsonstr = "";
        // 捕获到异常，直接返回失败
        if (exceptionList.size() > 0) {
            StayTimeReportServiceOut serviceOut = new StayTimeReportServiceOut();
            if (exceptionList.contains("Error")) {
                serviceOut.setErrorCode(JsonResultType.ERROR.toString());
            } else {
                serviceOut.setErrorCode(JsonResultType.TIMEOUT.toString());
            }
            StayTimeJsonOut outData = new StayTimeJsonOut();
            outData.setReportService(serviceOut);
            Object jsonObject = JSON.toJSON(outData);
            jsonstr = JSON.toJSONString(jsonObject);
            return jsonstr;
        } else {
            long t2 = System.currentTimeMillis() - t1;
            StayTimeReportServiceOut serviceOut = new StayTimeReportServiceOut();
            if (tempList != null && !tempList.isEmpty()) {
                StayTimeOut[] res = new StayTimeOut[tempList.size()];
                serviceOut.setStayTimes(tempList.toArray(res));
                serviceOut.setCount(tempList.size());

                if (analysesList != null && analysesList.size() > 0) {
                    Analyses[] res2 = new Analyses[analysesList.size()];
                    serviceOut.setAnalyses(analysesList.toArray(res2));
                }
            } else {
                serviceOut.setCount(0);
            }
            serviceOut.setId(HBaseManager.SearchId.StayTimeSearch.getId());
            serviceOut.setType("response");

            serviceOut.setTime(String.valueOf(t2));
            serviceOut.setErrorCode(JsonResultType.SUCCESS.toString());

            StayTimeJsonOut outData = new StayTimeJsonOut();
            outData.setReportService(serviceOut);

            Object jsonObject = JSON.toJSON(outData);
            jsonstr = JSON.toJSONString(jsonObject);
            return jsonstr;
        }
    }

    private List<StayTimeOut> getStayTimeOutData(List<StayTimeSearchOutData> leaderData, Map<String, StayTimeData> searchData) {
        List<StayTimeOut> outData = new ArrayList<StayTimeOut>();
        int count = 0; //处理要考虑每个组员多算的问题
        for (StayTimeSearchOutData leader : leaderData) {
            String rowkeyinfo[] = getRowKeyInfos(leader.getRowKey()); // 盐值占一个字节*/
            String uuid = "";
            if (rowkeyinfo != null && rowkeyinfo.length >= 2) {
                uuid = rowkeyinfo[1];
            }

            if (searchData.containsKey(uuid)) {

                StayTimeData search = searchData.get(uuid);
                StayTimeOut info = new StayTimeOut();

                info.setRt_image_data(search.getRt_image_data());
                info.setPerson_id(search.getPerson_id());
                info.setLib_id(search.getLib_id());
                //设置合并之后的聚合时间
                long stayTime = leader.getDurationTime();
                info.setDuration_time(convSpanToStr(stayTime * 1000));

                info.setOffice_id("");
                info.setOffice_name("");
                //info.setCamera_id("");
                //info.setCamera_name("");
                info.setCamera_id(search.getCamera_id());
                info.setCamera_name(search.getCamera_name());
                info.setCamera_type(0);
                info.setGpsx(0);
                info.setGpsy(0);
                info.setGpsy(0);
                info.setImg_url(search.getImg_url());
                if (reportType.equals("02")) {
                    if (officeIds != null && officeIds.length == 1) { //局站单选，设置局站相关
                        info.setOffice_id(search.getOffice_id());
                        info.setOffice_name(search.getOffice_name());
                    }
                } else if (reportType.equals("01")) { //摄像头单选，设置摄像头相关
                    if (null != cameraIds && cameraIds.length == 1) {
                        info.setOffice_id(search.getOffice_id());
                        info.setOffice_name(search.getOffice_name());
                        info.setCamera_id(search.getCamera_id());
                        info.setCamera_name(search.getCamera_name());
                        info.setCamera_type(search.getCamera_type());
                        info.setGpsx(search.getGpsx());
                        info.setGpsy(search.getGpsy());
                    }
                }
                outData.add(info);
                count++;
                if (count >= size) {
                    break;
                }
            }
        }
        return outData;
    }

    private String[] getRowKeyInfos(byte[] rowkey) {
        String rowkeyinfo[] = new String[2];
        try {
            String phoenixGapCharStr = Bytes.toString(phoenixGapChar);
            byte[] newRowKey = Bytes.copy(rowkey);
            for (int i = 1; i < 20; i++) {
                newRowKey[i] = (byte) (~(int) newRowKey[i]);
            }
            rowkeyinfo = Bytes.toString(newRowKey, 1).split(phoenixGapCharStr); // 盐值占一个字节*/
        } catch (Exception e) {
            log.info(e);
        }
        return rowkeyinfo;
    }

    private void validateArg(StayTimeInput param) throws IOException {
        if (param == null) {
            throw new IOException("RelationshipParam Exception: Param is null");
        } else {
            if (StringUtils.isEmpty(param.getStartTime())) {
                throw new IllegalArgumentException("StartTime can't be empty！");
            }
            if (StringUtils.isEmpty(param.getEndTime())) {
                throw new IllegalArgumentException("EndTime can't be empty！");
            }
            String timeDelta = null;
            try {
                timeDelta = DateTimeFun.getTimeDelta(param.getStartTime(), param.getEndTime());
            } catch (ParseException e) {
                log.error(e);
                throw new IllegalArgumentException("Time format is wrong！");
            }
            if (timeDelta != null && Float.parseFloat(timeDelta) > 1 * 24 * 60) {
                throw new IllegalArgumentException("The range of time can't exceed 1 days！");
            }
        }
    }

    private String getStayTimeOResultOld() {
        long t1 = System.currentTimeMillis();
        List<StayTimeSearchOutData> statData = null;
        statData = getStatResultOld();
        if (exceptionList.size() > 0) {
            return getJsonStr(t1, new ArrayList<StayTimeOut>());
        }

        try {
            //针对leader从hbase获取图片相关信息
            Map<String, StayTimeData> searchData = new LinkedHashMap<>();
            if (statData != null && statData.size() > 0) {
                searchData = getDataFromHbase(statData);
                if (exceptionList.size() > 0) {
                    return getJsonStr(t1, new ArrayList<StayTimeOut>());
                }
            }

            //对leader进行输出转换
            List<StayTimeOut> stayTimeOut = new ArrayList<>();
            if (searchData != null && searchData.size() > 0) {
                stayTimeOut = getStayTimeOutData(statData, searchData);
            }
            //返回结果信息
            return getJsonStr(t1, stayTimeOut);
        } catch (Exception e) {
            log.info(e);
            exceptionList.add("Error");
            return getJsonStr(t1, new ArrayList<StayTimeOut>());
        }
    }

    private List<StayTimeSearchOutData> getStatResultOld() {
        List<StayTimeSearchOutData> statData = new ArrayList<>();
        Scan newScan = new Scan();
        FilterList filterList = new FilterList(org.apache.hadoop.hbase.filter.FilterList.Operator.MUST_PASS_ONE);
        if (officeIds != null && officeIds.length > 0) {
            for (String officeId : officeIds) {
                if (officeId != null && !officeId.equals("")) {
                    BinaryComparator comp = new BinaryComparator(Bytes.toBytes(officeId));
                    SingleColumnValueFilter filter = new SingleColumnValueFilter(Bytes.toBytes(attrFamily),
                            Bytes.toBytes(officeIdColumn), CompareFilter.CompareOp.EQUAL, comp);
                    filter.setFilterIfMissing(true);
                    filterList.addFilter(filter);
                }
            }
        }
        if (cameraIds != null && cameraIds.length > 0) {
            for (String cameraId : cameraIds) {
                if (cameraId != null && !cameraId.equals("")) {
                    BinaryComparator comp = new BinaryComparator(Bytes.toBytes(cameraId));
                    SingleColumnValueFilter filter = new SingleColumnValueFilter(Bytes.toBytes(attrFamily),
                            Bytes.toBytes(cameraIdColumn), CompareFilter.CompareOp.EQUAL, comp);
                    filter.setFilterIfMissing(true);
                    filterList.addFilter(filter);
                }
            }
        }

        newScan.setFilter(filterList);
        newScan.addFamily(Bytes.toBytes(attrFamily));
        //必须在客户端添加需要过滤的条件列
        newScan.addColumn(Bytes.toBytes(attrFamily), Bytes.toBytes(officeIdColumn));
        newScan.addColumn(Bytes.toBytes(attrFamily), Bytes.toBytes(cameraIdColumn));
        newScan.setMaxVersions(1);
        newScan.setCacheBlocks(true);

        StayTimeStatParam searchParam = new StayTimeStatParam();
        searchParam.setSize(maxSize);
        searchParam.setStartTime(startTime);
        searchParam.setEndTime(endTime);
        searchParam.setThreshold(simThreshold);
        //searchParam.setAnalysis(analysis);

        HTable table = null;
        try {
            table = HBaseConfig.getTable(tableName);
            StayTimeStatClient client = new StayTimeStatClient();
            statData = client.getStayTimeStat(table, searchParam, newScan); // 协处理器中已排序
        } catch (Throwable e) {
            exceptionList.add("Exception");
            log.error(e);
         //   e.printStackTrace();
        } finally {
            try {
                if (table != null) {
                    table.close();
                }
            } catch (IOException e) {
                exceptionList.add("IOException");
                log.error(e);
               // e.printStackTrace();
            }
        }
        return statData;
    }
}

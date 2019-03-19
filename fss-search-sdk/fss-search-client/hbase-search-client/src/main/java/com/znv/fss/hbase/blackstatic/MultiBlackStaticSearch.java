package com.znv.fss.hbase.blackstatic;

import com.alibaba.fastjson.JSON;
import com.google.protobuf.ByteString;
import com.znv.fss.common.VConstants;
import com.znv.fss.hbase.HBaseConfig;
import com.znv.fss.hbase.HBaseManager;
import com.znv.fss.hbase.MultiHBaseSearch;
import com.znv.fss.hbase.JsonResultType;
import com.znv.hbase.client.coprocessor.BlackStaticSearchClient;
import com.znv.hbase.client.featureComp.ImageSearchParam;
import com.znv.hbase.coprocessor.endpoint.searchByImage.ImageSearchOutData;
import com.znv.fss.hbase.searchbyimage.FssInPutParams;
import com.znv.fss.hbase.searchbyimage.FssJsonInput;
import com.znv.fss.hbase.searchbyimage.FssReportServiceIn;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by estine on 2017/2/20.
 */
public class MultiBlackStaticSearch extends MultiHBaseSearch {
    private final Log LOG = LogFactory.getLog(MultiBlackStaticSearch.class);
    private final String hbaseTableName = HBaseConfig.getProperty(VConstants.FSS_PERSONLIST_V113_TABLE_NAME); //
    private FssInPutParams param = new FssInPutParams();
    private List<ImageSearchOutData> suspectList = new ArrayList<ImageSearchOutData>();
    private List<BlackStaticOutputData> outList = new ArrayList<BlackStaticOutputData>();
    private BlackStaticOutputDatas blackStaticOutputDatas = new BlackStaticOutputDatas();
    private int pageIndex = 0;
    private int pageSize = 0;
    private int count = 0; // 总记录数
    private int totalPage = 0; // 总页数
    private String exception = "";
    private String jsonstr = "";
    private long t1 = System.currentTimeMillis();

    public MultiBlackStaticSearch() {
        super("search");
    }

    @Override
    public String requestSearch(String jsonParamStr) throws Exception {
        // 解析json协议
        parseJsonParams(jsonParamStr); // 查询图片特征值 + 相似度
        // 解析查询条件
        List<String> features = param.getSearchFeatures(); // 特征值
        float hbaseThrehold = param.getSimThreshold() / 100.00F;
        ImageSearchParam searchParam = new ImageSearchParam();
        searchParam.setThreshold(hbaseThrehold);
        searchParam.setSearchFeatures(features);
        searchParam.setSelType("1");

        // 获取下一页查询结果
        if (!suspectList.isEmpty()) {
            getNextPageData();
            if (exception.equals("Exception")) {
                getExceptionResult();
                return jsonstr;
            } else {
                getJsonResult();
                return jsonstr;
            }
        }
        // 黑名单静态比对协处理器，返回疑似图片特征值、相似度
        try {
            BlackStaticSearchClient client1 = new BlackStaticSearchClient();
            suspectList = client1.getBlackStaticSearchResult(HBaseConfig.getTable(hbaseTableName), searchParam); // 协处理器中已排序
        } catch (Throwable e) {
            LOG.error(e);
          //  e.printStackTrace();
        }
        count = suspectList.size(); // 总记录数
        totalPage = count % pageSize == 0 ? count / pageSize : count / pageSize + 1;

        getFirstPageData(); // 获取第一次查询数据BlackStaticOutDatas
        if (exception.equals("Exception")) {
            getExceptionResult();
            return jsonstr;
        } else {
            getJsonResult();
            return jsonstr;
        }

    }

    private String getExceptionResult() {
        BlackStaticReportServiceOut serviceOut = new BlackStaticReportServiceOut();
        serviceOut.setId(HBaseManager.SearchId.BlackStaticSearch.getId());
        serviceOut.setErrorCode(JsonResultType.TIMEOUT);
        BlackStaticJsonOutput outData = new BlackStaticJsonOutput();
        outData.setReportService(serviceOut);
        Object jsonObject = JSON.toJSON(outData);
        jsonstr = JSON.toJSONString(jsonObject);
        return jsonstr;
    }

    private String getJsonResult() {
        BlackStaticReportServiceOut serviceOut = new BlackStaticReportServiceOut();
        long t2 = System.currentTimeMillis() - t1;
        serviceOut.setId(HBaseManager.SearchId.BlackStaticSearch.getId());
        serviceOut.setType("request");
        serviceOut.setUsedTime(String.valueOf(t2));
        serviceOut.setBlackStaticSearchLists(blackStaticOutputDatas);
        serviceOut.setErrorCode(JsonResultType.SUCCESS);
        serviceOut.setCount(count);
        serviceOut.setTotalPage(totalPage);
        BlackStaticJsonOutput outData = new BlackStaticJsonOutput();
        outData.setReportService(serviceOut);
        Object jsonObject = JSON.toJSON(outData);
        jsonstr = JSON.toJSONString(jsonObject);
        return jsonstr;
    }

    private void getFirstPageData() {
        List<ImageSearchOutData> topSortList = new ArrayList<ImageSearchOutData>();
        List<Get> listGets;
        if (suspectList.size() > pageSize) {
            for (int idx = 0; idx < pageSize; idx++) {
                topSortList.add(suspectList.get(idx));
            }
            listGets = getHbaseRows(topSortList); // 批量获取rowkey
            outList = getHbaseResult(listGets, topSortList); // 批量获取图片相关信息
        } else if (suspectList.size() > 0) {
            listGets = getHbaseRows(suspectList);
            outList = getHbaseResult(listGets, suspectList);
        }
        blackStaticOutputDatas.setSearchDatas(outList);
    }

    private void getNextPageData() {
        List<ImageSearchOutData> topSortList = new ArrayList<ImageSearchOutData>();
        List<Get> listGets;
        int startIndex = (pageIndex - 1) * pageSize;
        if (startIndex >= suspectList.size()) { // 返回结果为空
            outList = null; // todo 有待验证是否可行
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
        blackStaticOutputDatas.setSearchDatas(outList);
    }

    private List<BlackStaticOutputData> getHbaseResult(List<Get> listGets, List<ImageSearchOutData> sortList) {
        List<BlackStaticOutputData> out = new ArrayList<BlackStaticOutputData>();
        // todo int型正确显示（高位取反）
        try {
            HTable table = HBaseConfig.getTable(hbaseTableName);
            Result[] rs = table.get(listGets);
            int idx = 0;
            if (rs.length > 0) {
                for (Result r : rs) {
                    BlackStaticOutputData info = new BlackStaticOutputData();
                    int sim = (int) (sortList.get(idx).getSuspectSim() * 100);
                    info.setSim(sim); // int型相似度
                    byte[] rowkey = r.getRow();
                    Integer row = Bytes.toInt(rowkey);
                    info.setFcPid((0 - row));
                    Cell[] cells = r.rawCells();
                    int len = cells.length;
                    for (int i = 0; i < len; i++) {
                        Cell cell = cells[i];
                        String col = Bytes.toString(CellUtil.cloneQualifier(cell));
                        byte[] value0 = CellUtil.cloneValue(cell);
                        String value = Bytes.toString(CellUtil.cloneValue(cell));
                        switch (col) {
                            case "IMAGEDATA":
                                Base64 base64 = new Base64();
                                String imageData = base64.encodeBase64String(value0);
                                info.setImageData(imageData);
                                break;
                            case "PERSONID":
                                info.setPersonId(value);
                                break;
                            case "PERSONNAME":
                                info.setPersonName(value);
                                break;
                            case "IMAGENAME":
                                info.setImageName(value);
                                break;
                            case "STARTTIME":
                                info.setStartTime(value);
                                break;
                            case "ENDTIME":
                                info.setEndTime(value);
                                break;
                            case "CONTROLLEVEL":
                                int intValue = Bytes.toInt(CellUtil.cloneValue(cell));
                                info.setControlLevel(0 - intValue);
                                break;
                            case "FLAG":
                                int intValue1 = Bytes.toInt(CellUtil.cloneValue(cell));
                                info.setFlag(0 - intValue1);
                                break;
                            case "AGE":
                                int intValue2 = Bytes.toInt(CellUtil.cloneValue(cell));
                                info.setAge(0 - intValue2);
                                break;
                            case "SEX":
                                int intValue3 = Bytes.toInt(CellUtil.cloneValue(cell));
                                info.setSex(0 - intValue3);
                                break;
                            default:
                                break;
                        }
                    }
                    out.add(info);
                    idx++;
                }
            }
        } catch (IOException e) {
            LOG.error(e);
          //  e.printStackTrace();
        }

        return out;
    }

    /**
     * @param sortList 批量获取hbase表相关行
     * @return List<Get>
     */
    private List<Get> getHbaseRows(List<ImageSearchOutData> sortList) {
        List<Get> listGets = new ArrayList<Get>();
        for (ImageSearchOutData sortRes : sortList) {
            ByteString rk = sortRes.getBytesRowKey();
            Get get = new Get(rk.toByteArray());
            listGets.add(get);
        }
        return listGets;
    }

    /**
     * @param jsonParamStr 解析web传入查询条件
     */
    private void parseJsonParams(String jsonParamStr) {
        FssJsonInput inputParam = JSON.parseObject(jsonParamStr, FssJsonInput.class);
        FssReportServiceIn service = inputParam.getReportService();
        HBaseManager.SearchId id = HBaseManager.SearchId.getSearchId(service.getId());
        String type = service.getType();
        if (id.equals(HBaseManager.SearchId.BlackStaticSearch) && type.equals("request")) { // 12002
            param = service.getFssSearch(); // 获取查询条件
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

}

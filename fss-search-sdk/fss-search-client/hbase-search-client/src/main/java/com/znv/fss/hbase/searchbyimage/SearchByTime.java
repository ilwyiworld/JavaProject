package com.znv.fss.hbase.searchbyimage;

import com.znv.fss.common.VConstants;
import com.znv.fss.hbase.HBaseConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.PageFilter;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Administrator on 2017/3/8.
 */
public class SearchByTime extends Thread {
    private final Log log = LogFactory.getLog(SearchByTime.class);
    private final String hbaseTableName = HBaseConfig.getProperty(VConstants.FSS_HISTORY_V113_TABLE_NAME);
    private CountDownLatch threadsSignal;
    private String key = null;
    private String startTime = null;
    private String endTime = null;
    private int pageSize = 11;
    private FilterList filterList = null;
    private List<Row> outLists = null;
    private List<String> exceptionList = null;
    private byte[] lastId = null;

    public SearchByTime(CountDownLatch threadSignal, String key, String startTime, String endTime,
                        FilterList filterList, List<Row> outLists, List<String> exceptionList, byte[] lastId, int pageSize) {
        this.threadsSignal = threadSignal;
        this.key = key;
        this.startTime = startTime;
        this.endTime = endTime;
        this.filterList = filterList;
        this.outLists = outLists;
        this.exceptionList = exceptionList;
        this.lastId = lastId;
        this.pageSize = pageSize;
    }

    public void run() {
        if (hbaseTableName != null && key != null) {
            Scan s = new Scan();
            setScan(s); // 设置scan 参数

            outLists = getHBaseResult(s); // 获取各region查询结果

            threadsSignal.countDown(); // 线程结束时计数器减1

        }

    }

    private void setScan(Scan s) {
        FilterList filterAll = new FilterList(FilterList.Operator.MUST_PASS_ALL);
        if (filterList != null && !filterList.getFilters().isEmpty()) {
            filterAll.addFilter(filterList);
        }
        Filter filter = new PageFilter(pageSize);
        if (filterAll != null && filterList != null && !filterList.getFilters().isEmpty()) {
            filterAll.addFilter(filter);
            s.setFilter(filterAll);
        } else { // cameraId为空则只设置pageFilter
            s.setFilter(filter);
        }
        s.setCaching(pageSize);
        s.setMaxVersions(1);
        s.setCacheBlocks(false);

        // hbase rowkey取值范围 [startkey, stopkey)
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            byte[] startRow = null;
            long endTimeLong = Long.MAX_VALUE - (sdf.parse(startTime).getTime() - 1);
            byte[] stopRow = (Bytes.add(Bytes.toBytes(key), Bytes.toBytes("-"),
                    Bytes.toBytes(Long.toString(endTimeLong))));
            if (lastId != null && lastId.length > 0) {
                startRow = lastId;
            } else {
                long startTimeLong = Long.MAX_VALUE - sdf.parse(endTime).getTime();
                startRow = (Bytes.add(Bytes.toBytes(key), Bytes.toBytes("-"),
                        Bytes.toBytes(Long.toString(startTimeLong))));
            }
            s.setStartRow(startRow);
            s.setStopRow(stopRow);
            s.addFamily(Bytes.toBytes("feature"));

        } catch (ParseException e) {
            log.error(e);
         //   e.printStackTrace();
        }
    }

    private List<Row> getHBaseResult(Scan s) {
        List<Row> out = new ArrayList<Row>();
        ResultScanner rs = null;
        HTable table = null;
        try {
            table = HBaseConfig.getTable(hbaseTableName);
            rs = table.getScanner(s);
            int index = 0;
            for (Result r : rs) {
                if (index > (pageSize - 1)) { // 只查询pageSize条数据
                    break;
                } else {
                    Row rowInfo = new Row();
                    String row = Bytes.toString(r.getRow());
                    rowInfo.setSuspectKey(row);
                    index++;
                    outLists.add(rowInfo);
                }
            }
        } catch (IOException e) {
            log.error(e);
           // e.printStackTrace();
        } catch (Exception e) {
            exceptionList.add("Exception");
            log.error(e);
            //e.printStackTrace();
        } catch (Error e) {
            exceptionList.add("Error");
            log.error(e);
          //  e.printStackTrace();
        } finally {
            if (rs != null) {
                rs.close();
            }
            try {
                if (table != null) {
                    table.close();
                }
            } catch (IOException e) {
                log.error(e);
            }
        }
        return out;
    }
}

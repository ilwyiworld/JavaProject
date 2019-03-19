package com.znv.fss.hbase.relationshipsearch;

import com.znv.fss.common.utils.FeatureCompUtil;
import com.znv.fss.hbase.HBaseConfig;
import com.znv.hbase.coprocessor.endpoint.searchByImage.ImageSearchOutData;
import com.znv.hbase.coprocessor.endpoint.staytimestat.StayTimeSearchOutData;
import com.znv.hbase.util.PhoenixConvertUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.BinaryComparator;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.util.Pair;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Administrator on 2017/6/7.
 */
public class PeerSearchThread extends Thread {
    private final Log LOG = LogFactory.getLog(PeerSearchThread.class);
    private CountDownLatch threadsSignal;
    private String tablename;
    private List<ImageSearchOutData> personList;
    private List<StayTimeSearchOutData> peerlist;
    private Map<String, Pair<StayTimeSearchOutData, Long>> peerMap = new HashMap<String, Pair<StayTimeSearchOutData, Long>>();
    private int peerInterval;
    private byte[] saltVal = new byte[1];
  //  private byte[] alarmType = Bytes.toBytes(3); // todo
  //  private int maxSubType = 3; // todo
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private byte[] phoenixGapbs = new byte[1];
    private final List<byte[]> searchFeature;
    private FeatureCompUtil fc = new FeatureCompUtil();
    private float simThreshold = 0.89f;

    public PeerSearchThread(CountDownLatch threadsSignal, String tablename, List<ImageSearchOutData> personList,
        List<StayTimeSearchOutData> peerlist, int peerInterval, int saltVal, final List<byte[]> searchFeature) {
        this.threadsSignal = threadsSignal;
        this.tablename = tablename;
        this.peerlist = peerlist;
        this.personList = personList;
        this.peerInterval = peerInterval;
        this.saltVal[0] = (byte) saltVal;
        this.phoenixGapbs[0] = (byte) 0xff; //时间降序，为xff; 升序则为x00
        this.searchFeature = searchFeature;
        simThreshold = fc.reversalNormalize(simThreshold);
    }

    private boolean isSamePerson(byte[] feature) {
        for (byte[] temp: searchFeature) { //只要有一个相似，即认为是同一人脸
            float sim = fc.Dot(feature, temp, 12); // 比对相似度
            if (sim >= simThreshold) { // 相似度超过阈值，认为是同一类人脸
                return true;
            }
        }
        return false;
    }

    private void getPeerInfo(ImageSearchOutData personinfo) {
        String enterTime = personinfo.getEnterTime();
        String leaveTime = personinfo.getLeaveTime();
        String cameraId = personinfo.getCameraId();

        String startT = null;
        String stopT = null;
        try {
            long enterstamp = sdf.parse(enterTime).getTime();
            long leavestamp;
            if (leaveTime != null && !leaveTime.isEmpty()) {
                leavestamp = sdf.parse(leaveTime).getTime();
            } else {
                long stamp = personinfo.getDurationTime();
                if (stamp > 1) {
                    leavestamp = enterstamp + stamp * 1000l;
                }else {
                    // 若无离开时间，用进入时间+驻留5s ？
                    leavestamp = enterstamp + 5 * 1000;
                }
            }
            long startTime = enterstamp - peerInterval * 1000;
            long stopTime = leavestamp + peerInterval * 1000;
            startT = sdf.format(startTime);
            stopT = sdf.format(stopTime);
        } catch (ParseException e) {
            LOG.info(e);
        }

        Scan s = new Scan();
        byte[] starttimeConvt = Bytes.add(PhoenixConvertUtil.convertDescField(Bytes.toBytes(stopT)),
                phoenixGapbs);
        byte[] stoptimeConvt = Bytes.add(PhoenixConvertUtil.convertDescField(Bytes.toBytes(startT)),
                phoenixGapbs);
        // 包含开始时间和结束时间
        byte[] startRow = Bytes.add(saltVal, starttimeConvt);
        byte[] stopRow = Bytes.add(saltVal, stoptimeConvt, phoenixGapbs);
        s.setStartRow(startRow);
        s.setStopRow(stopRow);
        s.addColumn(Bytes.toBytes("ATTR"), Bytes.toBytes("DURATION_TIME"));
        s.addColumn(Bytes.toBytes("FEATURE"), Bytes.toBytes("RT_FEATURE"));
        s.addColumn(Bytes.toBytes("ATTR"), Bytes.toBytes("CAMERA_ID"));
        s.addColumn(Bytes.toBytes("ATTR"), Bytes.toBytes("PERSON_ID"));
        s.addColumn(Bytes.toBytes("ATTR"), Bytes.toBytes("LIB_ID"));
        s.setCaching(100);
        s.setCacheBlocks(false);
        s.setMaxVersions(1);
        BinaryComparator comp = new BinaryComparator(Bytes.toBytes(cameraId));
        SingleColumnValueFilter filter = new SingleColumnValueFilter(Bytes.toBytes("ATTR"), Bytes.toBytes("CAMERA_ID"),
            CompareFilter.CompareOp.EQUAL, comp);
        filter.setFilterIfMissing(true);
        s.setFilter(filter);

        HTable historyTable = null;
        ResultScanner rs = null;
        try {
            historyTable = HBaseConfig.getTable(tablename);
            rs = historyTable.getScanner(s);
            for (Result r : rs) {
                byte[] feature = r.getValue(Bytes.toBytes("FEATURE"), Bytes.toBytes("RT_FEATURE"));
                if (isSamePerson(feature)) {
                    continue; //查询本人跳过
                }

                StayTimeSearchOutData peerInfo = new StayTimeSearchOutData();
                peerInfo.setDurationTime(Bytes.toLong(r.getValue(Bytes.toBytes("ATTR"), Bytes.toBytes("DURATION_TIME"))));
                peerInfo.setFeature(feature);
                peerInfo.setRowKey(r.getRow());
                peerInfo.setPersonId(Bytes.toString(r.getValue(Bytes.toBytes("ATTR"), Bytes.toBytes("PERSON_ID"))));
                peerInfo.setLibId(Bytes.toInt(r.getValue(Bytes.toBytes("ATTR"), Bytes.toBytes("LIB_ID"))));

                String peerrowkey = Bytes.toStringBinary(r.getRow());
                if (peerMap.containsKey(peerrowkey)) {
                    // 对于查询人脸的多条信息对应一条同行人信息的情况，对多条信息同行时长做合并
                    long personDuration = peerMap.get(peerrowkey).getSecond() + personinfo.getDurationTime();
                    peerMap.put(peerrowkey, new Pair<>(peerInfo, personDuration));
                } else {
                    peerMap.put(peerrowkey, new Pair<>(peerInfo, personinfo.getDurationTime()));
                }
            }

        } catch (IOException e) {
            LOG.info(e);
        } catch (Exception e) {
            LOG.info(e);
        } finally {
            if (rs != null) {
                rs.close();
            }
            try {
                if (historyTable != null) {
                    historyTable.close();
                }
            } catch (IOException e) {
                LOG.info(e);
            }
        }

    }

    @Override
    public void run() {
        for (ImageSearchOutData personinfo : personList) {
            if (personinfo.getCameraId() != null && !personinfo.getCameraId().isEmpty()) {
                getPeerInfo(personinfo);
            }
        }
        // 去除重复的数据
        for (Map.Entry<String, Pair<StayTimeSearchOutData, Long>> entry : peerMap.entrySet()) {
            Pair<StayTimeSearchOutData, Long> val = entry.getValue();
            StayTimeSearchOutData data = val.getFirst();
            data.setDurationTime(Math.min(data.getDurationTime(), val.getSecond())); // 同行时长取两人时长的最小值
            peerlist.add(data);
        }
        threadsSignal.countDown();
    }
}

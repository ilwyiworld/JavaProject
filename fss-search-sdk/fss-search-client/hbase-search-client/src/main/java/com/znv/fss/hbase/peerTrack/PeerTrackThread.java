package com.znv.fss.hbase.peerTrack;

import com.znv.fss.common.VConstants;
import com.znv.fss.common.utils.FeatureCompUtil;
import com.znv.fss.hbase.HBaseConfig;
import com.znv.hbase.coprocessor.endpoint.searchByImage.ImageSearchOutData;
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
import org.apache.commons.codec.binary.Base64;
import org.apache.hadoop.hbase.util.Bytes;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Administrator on 2017/10/23.
 */
public class PeerTrackThread extends Thread {
    private final Log LOG = LogFactory.getLog(PeerTrackThread.class);
    private CountDownLatch threadsSignal;
    private String tableName;
    private List<ImageSearchOutData> personList;
    private Map<String, PeerTrackOutputData> targetAndPeerMap = new ConcurrentHashMap<String, PeerTrackOutputData>();
    private int peerInterval;
    private byte[] saltVal = new byte[1];
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private byte[] phoenixGapbs = new byte[1];
    // private final byte[] searchFeature;
    private List<String> features = new ArrayList<String>();
    private FeatureCompUtil fc = new FeatureCompUtil();
    private float simThreshold = 0.89f;
    List<PeerTrackOutputData> outputList;

    public PeerTrackThread(CountDownLatch threadsSignal, String tableName, List<ImageSearchOutData> personList,
        int peerInterval, int saltVal, List<String> features, Map<String, PeerTrackOutputData> targetAndPeerMap) {
        this.threadsSignal = threadsSignal;
        this.tableName = tableName;
        this.personList = personList;
        this.peerInterval = peerInterval;
        this.saltVal[0] = (byte) saltVal;
        this.phoenixGapbs[0] = (byte) 0xff; // 时间降序，为xff; 升序则为x00
        // this.searchFeature = searchFeature;
        this.features = features;
        simThreshold = fc.reversalNormalize(simThreshold);
        this.outputList = outputList;
        this.targetAndPeerMap = targetAndPeerMap;
    }

    private boolean isSamePerson(byte[] feature) {
        for (String ff : features) {
            byte[] searchFeature = new Base64().decode(ff);
            float sim = fc.Dot(feature, searchFeature, 12); // 比对相似度
            if (sim >= simThreshold) {
                return true;
            }
        }
        return false;
    }

    private void getPeerTrackInfo(ImageSearchOutData personData) {

        String enterTime = "";
        String leaveTime = "";
        if (!personData.getEnterTime().isEmpty()) {
            enterTime = personData.getEnterTime();
        }
        if (!personData.getLeaveTime().isEmpty()) {
            leaveTime = personData.getLeaveTime();
        }

        String cameraId = personData.getCameraId(); // cameraId 与 officeId 为一对一关系，不需要设置officeID过滤条件

        String startT = null;
        String stopT = null;

        try {
            long enterStamp = sdf.parse(enterTime).getTime();
            long leaveStamp;
            if (leaveTime != null && !leaveTime.isEmpty()) { // 离开时间为空，处理方式
                leaveStamp = sdf.parse(leaveTime).getTime();
            } else {
                long stamp = personData.getDurationTime();
                if (stamp > 1) {
                    leaveStamp = enterStamp + stamp * 1000;
                } else {
                    // 若无离开时间，用进入时间+间隔15s ？
                    leaveStamp = enterStamp + 5 * 1000; // todo
                }

            }
            long startTime = enterStamp - peerInterval * 1000;
            long stopTime = leaveStamp + peerInterval * 1000;
            startT = sdf.format(startTime);
            stopT = sdf.format(stopTime);
        } catch (ParseException e) {
            LOG.info(e);
        }

        Scan s = new Scan();
        byte[] starttimeConvt = Bytes.add(PhoenixConvertUtil.convertDescField(Bytes.toBytes(stopT)), phoenixGapbs);
        byte[] stoptimeConvt = Bytes.add(PhoenixConvertUtil.convertDescField(Bytes.toBytes(startT)), phoenixGapbs);
        // 包含开始时间和结束时间
        byte[] startRow = Bytes.add(saltVal, starttimeConvt);
        byte[] stopRow = Bytes.add(saltVal, stoptimeConvt, phoenixGapbs);
        s.setStartRow(startRow);
        s.setStopRow(stopRow);
        // s.addColumn(Bytes.toBytes("ATTR"), Bytes.toBytes("DURATION_TIME"));
        s.addColumn(Bytes.toBytes("FEATURE"), Bytes.toBytes("RT_FEATURE"));
        s.addColumn(Bytes.toBytes("ATTR"), Bytes.toBytes("CAMERA_ID"));
        s.addColumn(Bytes.toBytes("ATTR"), Bytes.toBytes("LEAVE_TIME")); // 计算间隔时间用
        s.addColumn(Bytes.toBytes("ATTR"), Bytes.toBytes("IMG_URL"));
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

        List<PeerOutputData> peerList = new ArrayList<PeerOutputData>();
        String uuid = personData.getUuid();
        try {
            historyTable = HBaseConfig.getTable(tableName);
            rs = historyTable.getScanner(s);
            for (Result r : rs) {
                byte[] feature = r.getValue(Bytes.toBytes("FEATURE"), Bytes.toBytes("RT_FEATURE"));
                if (isSamePerson(feature)) {
                    continue; // 查询本人跳过
                }
                PeerOutputData peerData = new PeerOutputData();
                byte[] rowkey = r.getRow();
                byte[] enterTimeConv = Bytes.copy(rowkey, 1, VConstants.DATE_TIME_STR_LENGTH); // 盐值占一个字节
                String peerEnterTime = Bytes.toString(PhoenixConvertUtil.convertDescField(enterTimeConv));
                byte[] peerUuid = Bytes.copy(rowkey, VConstants.DATE_TIME_STR_LENGTH + 2,
                    rowkey.length - (VConstants.DATE_TIME_STR_LENGTH + 2)); // 连接符占一个字节

                // [LQ-ADD]
                String imgUrl = Bytes.toString(r.getValue(Bytes.toBytes("ATTR"), Bytes.toBytes("IMG_URL")));
                peerData.setImgUrl(imgUrl);

                peerData.setPeerEnterTime(peerEnterTime);
                peerData.setPeerUuid(Bytes.toString(peerUuid));

                long peerLeaveTime = Bytes.toLong(r.getValue(Bytes.toBytes("ATTR"), Bytes.toBytes("LEAVE_TIME")));
                try {
                    long enterStamp = sdf.parse(enterTime).getTime();
                    long leaveStamp = sdf.parse(leaveTime).getTime();
                    long peerEnterTimeStamp = sdf.parse(peerEnterTime).getTime();
                    // 三个时间取最小
                    long intervalTime1 = Math.abs(enterStamp - peerEnterTimeStamp);
                    long intervalTime2 = Math.abs(enterStamp - peerLeaveTime);
                    long intervalTime3 = Math.abs(leaveStamp - peerEnterTimeStamp);
                    long[] intervalList = { intervalTime1, intervalTime2, intervalTime3 };
                    long intervalTime = intervalTime1; // 三个间隔时间中的最小值
                    for (int i = 0; i < intervalList.length; i++) {
                        if (intervalList[i] < intervalTime) {
                            intervalTime = intervalList[i];
                        }
                    }
                    peerData.setIntervalTime(intervalTime / 1000);
                } catch (ParseException e) {
                    LOG.info(e);
                }
                peerList.add(peerData);
            }
            if (/*peerList.size() > 0*/ !peerList.isEmpty()) { // [lq-add]没有同行人时也返回目标人信息
                // 判断map中key是否存在，已存在则将查询结果中的同行人信息加入原数据中，不存在则组一个新的list
                if (targetAndPeerMap.containsKey(uuid)) {
                    // outData = targetAndPeerMap.get(uuid);
                    // List<PeerOutputData> peerDatas = outData.getPeerList();
                    // peerDatas.addAll(peerList);
                    // outData.setPeerList(peerDatas);
                    targetAndPeerMap.get(uuid).getPeerList().addAll(peerList);
                } else {
                    PeerTrackOutputData outData = new PeerTrackOutputData();
                    outData.setPeerList(peerList);
                    outData.setTargetData(personData);
                    targetAndPeerMap.put(uuid, outData);
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

    public void run() {
        for (ImageSearchOutData personData : personList) {
            // 获取同行人信息
            getPeerTrackInfo(personData);

            // 数据去重
            // for (Map.Entry<String, PeerTrackOutputData> entry : targetAndPeerMap.entrySet()) {
            // PeerTrackOutputData val = entry.getValue();
            // outputList.add(val);
            // }
        }
        threadsSignal.countDown();
    }

}

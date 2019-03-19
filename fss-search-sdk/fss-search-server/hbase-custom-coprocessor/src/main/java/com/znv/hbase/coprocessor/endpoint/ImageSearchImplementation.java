package com.znv.hbase.coprocessor.endpoint;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import com.google.protobuf.Service;
import com.znv.fss.common.VConstants;
import com.znv.fss.common.utils.FeatureCompUtil;
import com.znv.hbase.protobuf.generated.ImageSearchProtos;
import com.znv.hbase.util.PhoenixConvertUtil;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.Coprocessor;
import org.apache.hadoop.hbase.CoprocessorEnvironment;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.coprocessor.CoprocessorException;
import org.apache.hadoop.hbase.coprocessor.CoprocessorService;
import org.apache.hadoop.hbase.coprocessor.RegionCoprocessorEnvironment;
import org.apache.hadoop.hbase.filter.BinaryComparator;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.regionserver.InternalScanner;
import org.apache.hadoop.hbase.util.ByteStringer;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.util.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Estine on 2016/12/23.
 */
public class ImageSearchImplementation
extends ImageSearchProtos.ImageSearchService implements CoprocessorService, Coprocessor {
    protected static final Log LOG = LogFactory.getLog(ImageSearchImplementation.class);
    private RegionCoprocessorEnvironment env;

    public void getImageSearchResult(RpcController controller, ImageSearchProtos.ImageSearchRequest request,
        RpcCallback<ImageSearchProtos.ImageSearchResponse> done) {
        ImageSearchProtos.ImageSearchResponse response = null;
        List<ImageSearchProtos.ImageSearchOut> out = new ArrayList<>();
        List<String> features = request.getImageFeatureList(); // 图片特征
        String sel = request.getSelType(); // 交、并集可选
        float threshold = request.getThreshold(); // 相似度比对阈值
        String startTime = request.getStartTime();
        String endTime = request.getEndTime();
        String searchType = request.getSearchType();
        List<String> cameraIds = request.getCameraIdList();
        List<String> officeIds = request.getOfficeIdList();
        Base64 base64 = new Base64();

        byte[] saltedKey = getSaltedKey();

        // 获取查询结果
        if ((features != null) && features.size() > 0) {
            List<byte[]> decodeFeatures = new ArrayList<>(features.size()); // 长度
            for (String feature : features) {
                decodeFeatures.add(base64.decode(feature));
            }

            // （rowkey+SIM）（按图片查询）
//            out = getSuspectLists(searchType, startTime, endTime, cameraIds, officeIds, saltedKey, decodeFeatures, threshold, sel);
            out = getSuspectListsV2(searchType, startTime, endTime, cameraIds, officeIds, saltedKey, decodeFeatures, threshold, sel);
        } else if (features == null) {
            // rowkey （按时间查询）
            out = getSuspectListsWithoutFeature(startTime, endTime, cameraIds, saltedKey);
        }

        // 查询结果返回给客户端
        if (out.size() > 0) {
            ImageSearchProtos.ImageSearchResponse.Builder responseBuilder = ImageSearchProtos.ImageSearchResponse
                .newBuilder();
            responseBuilder.addAllPictureSet(out);
            response = responseBuilder.build();
        }

        done.run(response);
    }

    private List<ImageSearchProtos.ImageSearchOut> getSuspectLists(String searchType, String startTime, String endTime,
        List<String> cameraIds, List<String> officeIds, byte[] saltedKey, List<byte[]> features, float threshold,
        String sel) {
        List<ImageSearchProtos.ImageSearchOut> retList = new ArrayList<ImageSearchProtos.ImageSearchOut>();
        FeatureCompUtil fc = new FeatureCompUtil();
        // float reservalThreshold = fc.reversalNormalize(threshold); // 归一化前阈值
        float reservalThreshold = threshold; // [lq-modify-2018-05-21] 协处理器传入的是反归一化的阈值
        byte[] featqual = Bytes.toBytes("RT_FEATURE");
        byte[] cameraqual = Bytes.toBytes("CAMERA_ID");
        byte[] officequal = Bytes.toBytes("OFFICE_ID");
        byte[] leavequal = Bytes.toBytes("LEAVE_TIME");
        byte[] dutarionqual = Bytes.toBytes("DURATION_TIME");
        byte[] gpsxqual = Bytes.toBytes("GPSX");
        byte[] gpsyqual = Bytes.toBytes("GPSY");
        byte[] cameraname = Bytes.toBytes("CAMERA_NAME");
        byte[] phoenixGapbs = new byte[1];
        phoenixGapbs[0] = (byte) 0xff; // 时间降序，为xff; 升序则为x00
        String phoenixGapChar = Bytes.toString(phoenixGapbs);
        if (features.size() > 0) { // 原判断条件“reservalThreshold != 0f”不合理
            // 设置startRow、stopRow
            Scan s = new Scan();
            s.setMaxVersions(1);
            s.setCacheBlocks(true); // false
            s.addColumn(Bytes.toBytes("FEATURE"), featqual); // 指定特征值列族
            // s.addColumn(Bytes.toBytes("ATTR"), durationtime); // [estine] 指定durationTime列
            byte[] starttimeConvt = Bytes.add(PhoenixConvertUtil.convertDescField(Bytes.toBytes(endTime)),
                phoenixGapbs);
            byte[] stoptimeConvt = Bytes.add(PhoenixConvertUtil.convertDescField(Bytes.toBytes(startTime)),
                phoenixGapbs);
            // 包含开始时间和结束时间
            byte[] startRow = Bytes.add(saltedKey, starttimeConvt);
            byte[] stopRow = Bytes.add(saltedKey, stoptimeConvt, phoenixGapbs);
            s.setStartRow(startRow);
            s.setStopRow(stopRow);
            // cameraIds不为空，则添加过滤器
            FilterList filterList1 = new FilterList(FilterList.Operator.MUST_PASS_ONE);
            if (cameraIds.size() > 0) {
                for (String cameraId : cameraIds) {
                    BinaryComparator comp = new BinaryComparator(Bytes.toBytes(cameraId));
                    SingleColumnValueFilter filter = new SingleColumnValueFilter(Bytes.toBytes("ATTR"), cameraqual,
                        CompareFilter.CompareOp.EQUAL, comp);
                    filter.setFilterIfMissing(true);
                    filterList1.addFilter(filter);
                }
            }
            if (officeIds.size() > 0) {
                for (String officeId : officeIds) {
                    BinaryComparator comp = new BinaryComparator(Bytes.toBytes(officeId));
                    SingleColumnValueFilter filter = new SingleColumnValueFilter(Bytes.toBytes("ATTR"), officequal,
                            CompareFilter.CompareOp.EQUAL, comp);
                    filter.setFilterIfMissing(true);
                    filterList1.addFilter(filter);
                }
            }
            if (!filterList1.getFilters().isEmpty()) {
                s.setFilter(filterList1);
            }
            if (!searchType.equals("1") || !filterList1.getFilters().isEmpty()) {
                s.addColumn(Bytes.toBytes("ATTR"), cameraqual); // 指定cameraId列
            }

            if (searchType.equals("2")) { // 历史人物关系查询
                s.addColumn(Bytes.toBytes("ATTR"), officequal);
                s.addColumn(Bytes.toBytes("ATTR"), leavequal);
                s.addColumn(Bytes.toBytes("ATTR"), dutarionqual);
            }

            if (searchType.equals("3")) { // 轨迹
                s.addColumn(Bytes.toBytes("ATTR"), gpsxqual);
                s.addColumn(Bytes.toBytes("ATTR"), gpsyqual);
            }

            if (searchType.equals("4")) { // 同行人轨迹分析
                s.addColumn(Bytes.toBytes("ATTR"), officequal); // lq-add
                s.addColumn(Bytes.toBytes("ATTR"), gpsxqual);
                s.addColumn(Bytes.toBytes("ATTR"), gpsyqual);
                s.addColumn(Bytes.toBytes("ATTR"), leavequal);
                s.addColumn(Bytes.toBytes("ATTR"), dutarionqual);
                s.addColumn(Bytes.toBytes("ATTR"), cameraname);
            }

            List<Cell> results = new ArrayList<Cell>();
            InternalScanner scanner = null;
            try {
                boolean hasMoreRows = false;
                scanner = env.getRegion().getScanner(s);
                do {
                    hasMoreRows = scanner.next(results); // results以cell为单位
                    if (results.size() > 0) {
                        float sim = 0.0f; // 并集
                        if (sel.equals("1")) {// 交集
                            sim = 1.0f;
                        }
                        byte[] cameraId = null;
                        byte[] leaveTime = null;
                        byte[] durationTime = null;
                        byte[] gpsx = null;
                        byte[] gpsy = null;
                        byte[] cameraName = null;
                        for (Cell cell : results) {
                            byte[] qual = CellUtil.cloneQualifier(cell);
                            byte[] value = CellUtil.cloneValue(cell);
                            if (Bytes.equals(qual, featqual)) {
                                if (value != null && value.length > 0) {
                                    // // 归一化前，0.5924
                                    // sim = fc.Dot(feature, value, 12); // 去掉jna依赖包 //商汤offset 12
                                    // // sim = fc.Dot(feature, value, 0); //自研 offset 0
                                    if ("1".equals(sel)) {// 交集
                                        for (byte[] feature : features) {
                                            sim = Math.min(fc.Dot(feature, value, 12), sim);
                                            if (sim < reservalThreshold) {
                                                break;
                                            }
                                        }
                                    } else if ("2".equals(sel)) {// 并集
                                        for (int i = 0; i < features.size(); i++) {
                                            sim = Math.max(fc.Dot(features.get(i), value, 12), sim);
                                            if (sim >= reservalThreshold) {
                                                break;
                                            }
                                        }
                                    }
                                }
                                continue;
                            }
                            if (Bytes.equals(qual, cameraqual)) {
                                cameraId = value;
                                continue;
                            }
                            if (Bytes.equals(qual, leavequal)) {
                                leaveTime = value;
                                continue;
                            }
                            if (Bytes.equals(qual, dutarionqual)) {
                                durationTime = value;
                                continue;
                            }
                            if (Bytes.equals(qual, gpsxqual)) {
                                gpsx = value;
                                continue;
                            }
                            if (Bytes.equals(qual, gpsyqual)) {
                                gpsy = value;
                                continue;
                            }
                            if (Bytes.equals(qual, cameraname)) {
                                cameraName = value;
                                continue;
                            }
                        }
                        if (sim >= reservalThreshold) { // 相似度超过阈值，认为是同一类人脸
                            byte[] rowkey = CellUtil.cloneRow(results.get(0));
                            byte[] enterTimeConv = Bytes.copy(rowkey, 1, VConstants.DATE_TIME_STR_LENGTH); // 盐值占一个字节
                            ImageSearchProtos.ImageSearchOut.Builder builder = ImageSearchProtos.ImageSearchOut
                                .newBuilder();
                            builder.setEnterTime(Bytes.toString(PhoenixConvertUtil.convertDescField(enterTimeConv)));
                            if (searchType.equals("1")) {
                                // 保存疑似人的rowkey和相似度
                                builder.setRowKey(ByteStringer.wrap(rowkey));
                                // float normalizeSim = fc.Comp(feature, value);
                                //float normalizeSim = fc.Normalize(sim); // 0.89--0.4855442
                                float normalizeSim = sim; // [lq-modify-2018-05-21] 返回归一化前相似度
                                builder.setSim(normalizeSim);
                            } else if (searchType.equals("2")) {
                                builder.setCameraId(Bytes.toString(cameraId));
                                if (leaveTime != null) {
                                    builder.setLeaveTime(Bytes.toString(leaveTime));
                                }
                                if (durationTime != null) {
                                    builder.setDurationTime(Bytes.toLong(durationTime));
                                }
                            } else if (searchType.equals("3")) {
                                builder.setCameraId(Bytes.toString(cameraId));
                                if (gpsx != null) {
                                    builder.setGpsx(PhoenixConvertUtil.convertFloatField(gpsx));
                                }
                                if (gpsy != null) {
                                    builder.setGpsy(PhoenixConvertUtil.convertFloatField(gpsy));
                                }
                            } else if (searchType.equals("4")) {
                                // [estine] 同行人轨迹分析,输出疑似图片enter_time,uuid,leave_time,duration_time,gpsx,gpsy,camera_id
                                byte[] uuid = Bytes.copy(rowkey, VConstants.DATE_TIME_STR_LENGTH + 2,
                                        rowkey.length - (VConstants.DATE_TIME_STR_LENGTH + 2)); // 连接符占一个字节
                                builder.setUuid(Bytes.toString(uuid));

                                if (leaveTime != null) {
                                    builder.setLeaveTime(Bytes.toString(leaveTime));
                                }
                                if (durationTime != null) {
                                    builder.setDurationTime(Bytes.toLong(durationTime));
                                }
                                if (gpsx != null) {
                                    builder.setGpsx(PhoenixConvertUtil.convertFloatField(gpsx));
                                }
                                if (gpsy != null) {
                                    builder.setGpsy(PhoenixConvertUtil.convertFloatField(gpsy));
                                }
                                builder.setCameraId(Bytes.toString(cameraId));
                                if (cameraName != null) {
                                    builder.setCameraName(Bytes.toString(cameraName));
                                }
                            }
                            retList.add(builder.build());
                        }
                    }
                    results.clear();
                } while (hasMoreRows);

            } catch (IOException e) {
                LOG.info(e);
            } catch (Exception e) {
                LOG.info(e);
            } finally {
                if (scanner != null) {
                    try {
                        scanner.close();
                    } catch (IOException e) {
                        LOG.info(e);
                    }
                }
            }
            // LOG.info("[7]-count2 = " + count2);

        }
        return retList;
    }



    private List<ImageSearchProtos.ImageSearchOut> getSuspectListsV2(String searchType, String startTime, String endTime,
                                                                     List<String> cameraIds, List<String> officeIds, byte[] saltedKey, List<byte[]> features, float threshold,
                                                                     String sel) {
        FeatureCompUtil fc = new FeatureCompUtil();
        List<ImageSearchProtos.ImageSearchOut> retList = null;
        // float reservalThreshold = fc.reversalNormalize(threshold); // 归一化前阈值
        float reservalThreshold = threshold; // [lq-modify-2018-05-21] 协处理器传入的是反归一化的阈值
        byte[] featqual = Bytes.toBytes("RT_FEATURE");
        byte[] phoenixGapbs = new byte[1];
        phoenixGapbs[0] = (byte) 0xff; // 时间降序，为xff; 升序则为x00
        String phoenixGapChar = Bytes.toString(phoenixGapbs);
        if (features.size() > 0) { // 原判断条件“reservalThreshold != 0f”不合理
            // 设置startRow、stopRow
            Scan s = new Scan();
            s.setMaxVersions(1);
            s.setCacheBlocks(true); // false
            s.addColumn(Bytes.toBytes("FEATURE"), featqual); // 指定特征值列族
            // s.addColumn(Bytes.toBytes("ATTR"), cameraqual); // 指定cameraId列
            byte[] starttimeConvt = Bytes.add(PhoenixConvertUtil.convertDescField(Bytes.toBytes(endTime)),
                    phoenixGapbs);
            byte[] stoptimeConvt = Bytes.add(PhoenixConvertUtil.convertDescField(Bytes.toBytes(startTime)),
                    phoenixGapbs);
            // 包含开始时间和结束时间
            byte[] startRow = Bytes.add(saltedKey, starttimeConvt);
            byte[] stopRow = Bytes.add(saltedKey, stoptimeConvt, phoenixGapbs);
            s.setStartRow(startRow);
            s.setStopRow(stopRow);

            List<Cell> results = new ArrayList<Cell>();
            List<Pair<byte[], Float>> rowinfo = new ArrayList<Pair<byte[], Float>>();
            InternalScanner scanner = null;
            try {
                boolean hasMoreRows = false;
                scanner = env.getRegion().getScanner(s);
                do {
                    hasMoreRows = scanner.next(results); // results以cell为单位
                    if (results.size() > 0) {
                        float sim = 0.0f; // 并集
                        if (sel.equals("1")) {// 交集
                            sim = 1.0f;
                        }
                        for (Cell cell : results) {
                            byte[] qual = CellUtil.cloneQualifier(cell);
                            byte[] value = CellUtil.cloneValue(cell);
                            if (Bytes.equals(qual, featqual)) {
                                if (value != null && value.length > 0) {
                                    // // 归一化前，0.5924
                                    // sim = fc.Dot(feature, value, 12); // 去掉jna依赖包 //商汤offset 12
                                    // // sim = fc.Dot(feature, value, 0); //自研 offset 0
                                    if ("1".equals(sel)) { // 交集
                                        for (byte[] feature : features) {
                                            sim = Math.min(fc.Dot(feature, value, 12), sim);
                                            if (sim < reservalThreshold) {
                                                break;
                                            }
                                        }
                                    } else if ("2".equals(sel)) {// 并集
                                        for (int i = 0; i < features.size(); i++) {
                                            sim = Math.max(fc.Dot(features.get(i), value, 12), sim);
                                            if (sim >= reservalThreshold) {
                                                break;
                                            }
                                        }
                                    }
                                }
                                continue;
                            }
                        }

                        if (sim >= reservalThreshold) { // 相似度超过阈值，认为是同一类人脸
                            byte[] rowkey = CellUtil.cloneRow(results.get(0));
                            // float normalizeSim = fc.Normalize(sim); // 0.89--0.4855442
                            float normalizeSim = sim; // [lq-modify-2018-05-21] 返回归一化前相似度
                            Pair<byte[], Float> pair = new Pair<>(rowkey, normalizeSim);
                            rowinfo.add(pair);
                        }
                    }
                    results.clear();
                } while (hasMoreRows);

                retList = getResultAfterCompare(rowinfo, cameraIds, officeIds, searchType);

            } catch (IOException e) {
                LOG.info(e);
            } catch (Exception e) {
                LOG.info(e);
            } finally {
                if (scanner != null) {
                    try {
                        scanner.close();
                    } catch (IOException e) {
                        LOG.info(e);
                    }
                }
            }
            // LOG.info("[7]-count2 = " + count2);

        }
        return retList;
    }

    private List<ImageSearchProtos.ImageSearchOut> getResultAfterCompare(List<Pair<byte[], Float>> rows, List<String> cameraIds, List<String> officeIds, String searchType) {
        byte[] cameraqual = Bytes.toBytes("CAMERA_ID");
        byte[] officequal = Bytes.toBytes("OFFICE_ID");
        byte[] gpsxqual = Bytes.toBytes("GPSX");
        byte[] gpsyqual = Bytes.toBytes("GPSY");
        byte[] leavequal = Bytes.toBytes("LEAVE_TIME");
        byte[] dutarionqual = Bytes.toBytes("DURATION_TIME");
        byte[] cameraname = Bytes.toBytes("CAMERA_NAME");
        List<Cell> getCells = null;
        List<ImageSearchProtos.ImageSearchOut> retList = new ArrayList<ImageSearchProtos.ImageSearchOut>();

        // cameraIds不为空，则添加过滤器
        FilterList filterList1 = new FilterList(FilterList.Operator.MUST_PASS_ONE);
        if (cameraIds.size() > 0) {
            for (String cameraId : cameraIds) {
                BinaryComparator comp = new BinaryComparator(Bytes.toBytes(cameraId));
                SingleColumnValueFilter filter = new SingleColumnValueFilter(Bytes.toBytes("ATTR"), cameraqual,
                        CompareFilter.CompareOp.EQUAL, comp);
                filter.setFilterIfMissing(true);
                filterList1.addFilter(filter);
            }
        }
        if (officeIds.size() > 0) {
            for (String officeId : officeIds) {
                BinaryComparator comp = new BinaryComparator(Bytes.toBytes(officeId));
                SingleColumnValueFilter filter = new SingleColumnValueFilter(Bytes.toBytes("ATTR"), officequal,
                        CompareFilter.CompareOp.EQUAL, comp);
                filter.setFilterIfMissing(true);
                filterList1.addFilter(filter);
            }
        }

        try {
            for (Pair<byte[], Float> row : rows) {
                byte[] rowkey = row.getFirst();
                if (!filterList1.getFilters().isEmpty() || searchType.equals("2") || searchType.equals("3") || searchType.equals("4")) {
                    Get get = new Get(rowkey);
                    get.addColumn(Bytes.toBytes("ATTR"), cameraqual);
                    if (!filterList1.getFilters().isEmpty()) {
                        get.setFilter(filterList1);
                    }
                    if (searchType.equals("2")) { //历史人物关系查询
                        get.addColumn(Bytes.toBytes("ATTR"), officequal);
                        get.addColumn(Bytes.toBytes("ATTR"), leavequal);
                        get.addColumn(Bytes.toBytes("ATTR"), dutarionqual);
                    }
                    if (searchType.equals("3")) { // 轨迹
                        get.addColumn(Bytes.toBytes("ATTR"), gpsxqual);
                        get.addColumn(Bytes.toBytes("ATTR"), gpsyqual);
                    }
                    if (searchType.equals("4")) { // 同行人轨迹分析
                        get.addColumn(Bytes.toBytes("ATTR"), officequal);
                        get.addColumn(Bytes.toBytes("ATTR"), gpsxqual);
                        get.addColumn(Bytes.toBytes("ATTR"), gpsyqual);
                        get.addColumn(Bytes.toBytes("ATTR"), leavequal);
                        get.addColumn(Bytes.toBytes("ATTR"), dutarionqual);
                        get.addColumn(Bytes.toBytes("ATTR"), cameraname);
                    }
                    getCells = env.getRegion().get(get, true);
                    if (getCells.isEmpty()) continue;
                }

                ImageSearchProtos.ImageSearchOut.Builder builder = ImageSearchProtos.ImageSearchOut.newBuilder();
                byte[] enterTimeConv = Bytes.copy(rowkey, 1, VConstants.DATE_TIME_STR_LENGTH); // 盐值占一个字节
                builder.setEnterTime(Bytes.toString(PhoenixConvertUtil.convertDescField(enterTimeConv)));
                if (searchType.equals("1")) { //以脸搜脸
                    // 保存疑似人的rowkey和相似度
                    builder.setRowKey(ByteStringer.wrap(rowkey));
                    builder.setSim(row.getSecond());
                } else if (searchType.equals("2")) { //历史人物关系
                    for (Cell cell : getCells) {
                        String col = Bytes.toString(CellUtil.cloneQualifier(cell)).toLowerCase();
                        byte[] value0 = CellUtil.cloneValue(cell);
                        if (col.equals("camera_id")) {
                            builder.setCameraId(Bytes.toString(value0));
                        } else if (col.equals("leave_time")) {
                            builder.setLeaveTime(Bytes.toString(value0));
                        } else if (col.equals("duration_time")) {
                            builder.setDurationTime(Bytes.toLong(value0));
                        }
                    }
                } else if (searchType.equals("3")) { //以脸搜脸轨迹
                    for (Cell cell : getCells) {
                        String col = Bytes.toString(CellUtil.cloneQualifier(cell)).toLowerCase();
                        byte[] value0 = CellUtil.cloneValue(cell);
                        if (col.equals("camera_id")) {
                            builder.setCameraId(Bytes.toString(value0));
                        } else if (col.equals("gpsx")) {
                            builder.setGpsx(PhoenixConvertUtil.convertFloatField(value0));
                        } else if (col.equals("gpsy")) {
                            builder.setGpsy(PhoenixConvertUtil.convertFloatField(value0));
                        }
                    }
                } else if (searchType.equals("4")) { //同行人轨迹
                    byte[] uuid = Bytes.copy(rowkey, VConstants.DATE_TIME_STR_LENGTH + 2,
                            rowkey.length - (VConstants.DATE_TIME_STR_LENGTH + 2)); // 连接符占一个字节
                    builder.setUuid(Bytes.toString(uuid));
                    for (Cell cell : getCells) {
                        String col = Bytes.toString(CellUtil.cloneQualifier(cell)).toLowerCase();
                        byte[] value0 = CellUtil.cloneValue(cell);
                        if (col.equals("camera_id")) {
                            builder.setCameraId(Bytes.toString(value0));
                        } else if (col.equals("gpsx")) {
                            builder.setGpsx(PhoenixConvertUtil.convertFloatField(value0));
                        } else if (col.equals("gpsy")) {
                            builder.setGpsy(PhoenixConvertUtil.convertFloatField(value0));
                        } else if (col.equals("leave_time")) {
                            builder.setLeaveTime(Bytes.toString(value0));
                        } else if (col.equals("duration_time")) {
                            builder.setDurationTime(Bytes.toLong(value0));
                        } else if (col.equals("camera_name")) {
                            builder.setCameraName(Bytes.toString(value0));
                        }
                    }
                }
                retList.add(builder.build());
                getCells = null;
            }
        } catch (IOException e) {
            LOG.info(e);
        }

        return retList;
    }

    private List<ImageSearchProtos.ImageSearchOut> getSuspectListsWithoutFeature(String startTime, String endTime,
        List<String> cameraIds, byte[] saltedKey) {
        List<ImageSearchProtos.ImageSearchOut> retList = new ArrayList<ImageSearchProtos.ImageSearchOut>();
        final byte[] family = Bytes.toBytes("FEATURE"); // 图片特征值列族 cameraId列
        byte[] alarmType = Bytes.toBytes(3);
        int maxSubType = 3;
        byte[] phoenixGapbs = new byte[1];
        phoenixGapbs[0] = (byte) 0;
        if (saltedKey != null && saltedKey.length > 0) {
            for (int i = 0; i < maxSubType; i++) {
                Scan s = new Scan();
                s.setMaxVersions(1);
                s.setCacheBlocks(true); // false
                byte[] rowkeyPrefix = Bytes.add(saltedKey, alarmType, Bytes.toBytes(i));
                byte[] startRow = Bytes.add(rowkeyPrefix, Bytes.toBytes(startTime));
                byte[] stopRow = Bytes.add(rowkeyPrefix, Bytes.toBytes(endTime), phoenixGapbs);
                s.setStartRow(startRow);
                s.setStopRow(stopRow);
                // cameraIds不为空，则添加过滤器
                FilterList filterList1 = new FilterList(FilterList.Operator.MUST_PASS_ONE);
                if (cameraIds.size() > 0) {
                    for (String cameraId : cameraIds) {
                        BinaryComparator comp = new BinaryComparator(Bytes.toBytes(cameraId));
                        SingleColumnValueFilter filter = new SingleColumnValueFilter(family, Bytes.toBytes("CAMERA_ID"),
                            CompareFilter.CompareOp.EQUAL, comp);
                        filter.setFilterIfMissing(true);
                        filterList1.addFilter(filter);
                    }
                    s.setFilter(filterList1);
                }
                List<Cell> results = new ArrayList<Cell>();
                InternalScanner scanner = null;
                try {
                    boolean hasMoreRows = false;
                    scanner = env.getRegion().getScanner(s);
                    do {
                        hasMoreRows = scanner.next(results); // results以cell为单位
                        if (results.size() > 0) {
                            Cell res = results.get(0);
                            ImageSearchProtos.ImageSearchOut.Builder builder = ImageSearchProtos.ImageSearchOut
                                .newBuilder();
                            builder.setRowKey(ByteStringer.wrap(CellUtil.cloneRow(res)));
                            retList.add(builder.build());
                        }
                        results.clear();
                    } while (hasMoreRows);

                } catch (IOException e) {
                    LOG.info(e);
                } catch (Exception e) {
                    LOG.info(e);
                } finally {
                    if (scanner != null) {
                        try {
                            scanner.close();
                        } catch (IOException e) {
                            LOG.info(e);
                        }
                    }
                }
            }
        }
        return retList;
    }

    private byte[] getSaltedKey() {
        byte[] saltedkey = new byte[VConstants.PHOENIX_ROWKEY_SALTING_LENGTH];
        byte[] regionStartKey = env.getRegion().getRegionInfo().getStartKey(); // 获取region的startkey
        if (regionStartKey.length == 0) {
            saltedkey[0] = VConstants.STAY_TIME_EMPTY_REGION_START_KEY;
        } else {
            System.arraycopy(regionStartKey, 0, saltedkey, 0, VConstants.PHOENIX_ROWKEY_SALTING_LENGTH);
        }
        return saltedkey;
    }

    public Service getService() {
        return this;
    }

    @Override
    public void start(CoprocessorEnvironment env) throws IOException {
        if (env instanceof RegionCoprocessorEnvironment) {
            this.env = (RegionCoprocessorEnvironment) env;
        } else {
            throw new CoprocessorException("Must be loaded on a table region!");
        }
        // 创建线程池，控制并发数
        /*
         * final String n = Thread.currentThread().getName(); LOG.info("start 546"); imageSearchPool =
         * Executors.newFixedThreadPool(VConstants.FACE_SEARCH_ENDPOINT_POOL_NUM, new ThreadFactory() {
         * @Override public Thread newThread(Runnable r) { Thread t = new Thread(r); t.setName(n + "-faceSearch-" +
         * System.currentTimeMillis()); return t; } }); LOG.info("start 867");
         */
    }

    @Override
    public void stop(CoprocessorEnvironment env) throws IOException {
        // nothing to do
        // imageSearchPool.shutdown();
    }
}

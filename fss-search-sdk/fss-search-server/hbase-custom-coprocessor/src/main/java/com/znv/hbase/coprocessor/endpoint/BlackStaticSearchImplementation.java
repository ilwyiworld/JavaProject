package com.znv.hbase.coprocessor.endpoint;

import com.google.protobuf.ByteString;
import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import com.google.protobuf.Service;
import com.znv.fss.common.VConstants;
import com.znv.fss.common.utils.FeatureCompUtil;
import com.znv.hbase.coprocessor.endpoint.searchByImage.ImageSearchOutData;
import com.znv.hbase.protobuf.generated.BlackStaticSearchProtos;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.Coprocessor;
import org.apache.hadoop.hbase.CoprocessorEnvironment;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.coprocessor.CoprocessorException;
import org.apache.hadoop.hbase.coprocessor.CoprocessorService;
import org.apache.hadoop.hbase.coprocessor.RegionCoprocessorEnvironment;
import org.apache.hadoop.hbase.regionserver.InternalScanner;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by estine on 2017/2/20. 查询（94集群）FSS_BLACKLIST_TEST_0117，rowkey:fcpid (int); IMAGEDATA，FEATURE :二进制 ；
 */
public class BlackStaticSearchImplementation
        extends BlackStaticSearchProtos.BlackStaticSearchService implements CoprocessorService, Coprocessor {
    protected static final Log LOG = LogFactory.getLog(BlackStaticSearchImplementation.class);
    private RegionCoprocessorEnvironment env;

    public void getBlackStaticSearchResult(RpcController controller,
                                           BlackStaticSearchProtos.BlackStaticSearchRequest request,
                                           RpcCallback<BlackStaticSearchProtos.BlackStaticSearchResponse> done) {
        BlackStaticSearchProtos.BlackStaticSearchResponse response = null;
        List<BlackStaticSearchProtos.BlackStaticSearchOut> out = new ArrayList<>();
        List<ImageSearchOutData> suspectLists = null;
        String feature = request.getImageFeature(); // 图片特征
        float threshold = request.getThreshold(); // 相似度阈值
        Base64 base64 = new Base64();
        byte[] decodeFeature = base64.decode(feature);

        suspectLists = getSuspectLists(decodeFeature, threshold); // 获取rowkey和SIM

        if (suspectLists != null && suspectLists.size() > 0) {
            for (ImageSearchOutData temp : suspectLists) {
                BlackStaticSearchProtos.BlackStaticSearchOut.Builder builder = BlackStaticSearchProtos.BlackStaticSearchOut
                        .newBuilder();
                builder.setSim(temp.getSuspectSim());
                builder.setRowKey(temp.getBytesRowKey());
                out.add(builder.build());
            }
        }

        // 查询结果返回给客户端
        if (out.size() > 0) {
            BlackStaticSearchProtos.BlackStaticSearchResponse.Builder responseBuilder = BlackStaticSearchProtos.BlackStaticSearchResponse
                    .newBuilder();
            responseBuilder.addAllPictureSet(out);
            response = responseBuilder.build();
        }
        done.run(response);

    }

    private List<ImageSearchOutData> getSuspectLists(byte[] feature, float threshold) {
        List<ImageSearchOutData> retList = new ArrayList<ImageSearchOutData>();
        FeatureCompUtil fc = new FeatureCompUtil();
        // 特征值列 FEATURES:FEATURE
        byte[] family = Bytes.toBytes("FEATURES");
        byte[] featureColumn = Bytes.toBytes("FEATURE");
        float reservalThreshold = fc.reversalNormalize(threshold); // 归一化前阈值
        if (feature.length > 0) {
            Scan s = new Scan();
            s.setMaxVersions(1);
            s.setCacheBlocks(false);
            s.addColumn(family, featureColumn); // 指定特征值列
            List<Cell> results = new ArrayList<Cell>();
            InternalScanner scanner = null;
            try {
                boolean hasMoreRows = false;
                scanner = env.getRegion().getScanner(s);
                do {
                    hasMoreRows = scanner.next(results); // results以cell为单位
                    if (results.size() > 0) {
                        for (Cell cell : results) {
                            if (Bytes.equals(CellUtil.cloneQualifier(cell), featureColumn)) {
                                byte[] value = CellUtil.cloneValue(cell);
                                // float sim = STFeatureCompare.featureCompJava(fc, feature, value); // 归一化前，0.5924
                                float sim = fc.Dot(feature, value, 12); // 去掉jna依赖包
                                if (sim >= reservalThreshold) { // 相似度超过阈值，认为是同一类人脸
                                    // 保存疑似人的rowkey和相似度
                                    ImageSearchOutData suspectData = new ImageSearchOutData();
                                    suspectData.setBytesRowKey(ByteString.copyFrom(CellUtil.cloneRow(cell))); // Bytes.toInt
                                    // suspectData.setIntRowKey(0-Bytes.toInt(CellUtil.cloneRow(cell)));
                                    // //phoenix表中rowkey为int型
                                    float normalizeSim = fc.Normalize(sim);
                                    suspectData.setSuspectSim(normalizeSim);
                                    retList.add(suspectData);
                                }
                                break;
                            }
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

        }
        return retList;
    }

    private byte[] getSaltedKey() {
        byte[] saltedkey = new byte[VConstants.FACE_ROWKEY_SALTING_LENGTH];
        byte[] regionStartKey = env.getRegion().getRegionInfo().getStartKey(); // 获取region的startkey
        if (regionStartKey.length == 0) {
            System.arraycopy(VConstants.FACE_EMPTY_REGION_START_KEY, 0, saltedkey, 0,
                    VConstants.FACE_EMPTY_REGION_START_KEY.length); // VConstants.FACE_ROWKEY_SALTING_LENGTH);
        } else {
            System.arraycopy(regionStartKey, 0, saltedkey, 0, VConstants.FACE_ROWKEY_SALTING_LENGTH);
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

package com.znv.hbase.coprocessor.endpoint;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import com.google.protobuf.Service;
import com.znv.fss.common.utils.FeatureCompUtil;
import com.znv.hbase.protobuf.generated.FaceFeatureCompProtos;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
//import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.Coprocessor;
import org.apache.hadoop.hbase.CoprocessorEnvironment;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.coprocessor.CoprocessorException;
import org.apache.hadoop.hbase.coprocessor.CoprocessorService;
import org.apache.hadoop.hbase.coprocessor.RegionCoprocessorEnvironment;
import org.apache.hadoop.hbase.regionserver.HRegion;
import org.apache.hadoop.hbase.regionserver.InternalScanner;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.commons.codec.binary.Base64;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/12/27.
 */
public class FaceFeatureCompImplementation
    extends FaceFeatureCompProtos.FaceFeatureCompService implements CoprocessorService, Coprocessor {

    protected static final Log LOG = LogFactory.getLog(FaceFeatureCompImplementation.class);
    private RegionCoprocessorEnvironment env;

    public void getFaceFeatureCompResult(RpcController controller, FaceFeatureCompProtos.FaceFeatureCompRequest request,
        RpcCallback<FaceFeatureCompProtos.FaceFeatureCompResponse> done) {
        FaceFeatureCompProtos.FaceFeatureCompResponse response = null;
        String feature = request.getImageFeature();
        float threshold = request.getThreshold();
        boolean idExist = false; // 标记是否存在符合查询条件的ID
        HRegion region = (HRegion) env.getRegion();
        // BASE64Decoder decoder = new BASE64Decoder();
        Base64 base64 = new Base64();
        List<Cell> results = new ArrayList<Cell>();
        InternalScanner scanner = null;
        try {
            byte[] colfamily = Bytes.toBytes("FEATURES");
            byte[] col = Bytes.toBytes("FEATURE0");
            Scan s = new Scan();
            s.setMaxVersions(1);
            s.addColumn(colfamily, col);

            scanner = region.getScanner(s);
            byte[] decodeFeature = base64.decode(feature);
            // byte[] decodeFeature = decoder.decodeBuffer(feature);
            boolean hasMoreRows = false;
            List<FaceFeatureCompProtos.FaceFeatureCompOut> complist = new ArrayList<FaceFeatureCompProtos.FaceFeatureCompOut>();
            do {
                hasMoreRows = scanner.next(results); // results以cell为单位
                if (results.size() > 0) {
                    for (Cell cell : results) {
                        if (Bytes.equals(CellUtil.cloneQualifier(cell), col)) {
                            byte[] value = CellUtil.cloneValue(cell);
                            // float sim = STFeatureCompare.featureComp(decodeFeature, value);
                            FeatureCompUtil fc = new FeatureCompUtil();
                            float sim = fc.Dot(decodeFeature, value, 12);
                            if (sim > threshold) { // 相似度超过阈值，认为是同一类人脸
                                FaceFeatureCompProtos.FaceFeatureCompOut.Builder builder = FaceFeatureCompProtos.FaceFeatureCompOut
                                    .newBuilder();
                                builder.setId(String.valueOf(Bytes.toInt(CellUtil.cloneRow(cell))));
                                builder.setSim(sim);
                                complist.add(builder.build());
                                idExist = true;
                            }
                            // LOG.info("sim="+sim+ ", cnt="+complist.size());
                        }
                    }
                }
                results.clear();
            } while (hasMoreRows);
            FaceFeatureCompProtos.FaceFeatureCompResponse.Builder responseBuilder = FaceFeatureCompProtos.FaceFeatureCompResponse
                .newBuilder();
            responseBuilder.addAllFeatureCompOut(complist);
            responseBuilder.setIsIdExist(idExist);
            response = responseBuilder.build();
        } catch (IOException e) {
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

        done.run(response);
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
    }

    @Override
    public void stop(CoprocessorEnvironment env) throws IOException {
        // nothing to do
    }

}

package com.znv.hbase.client.coprocessor;

import com.znv.hbase.client.featureComp.FeatureCompOutData;
import com.znv.hbase.protobuf.generated.FaceFeatureCompProtos;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.client.coprocessor.Batch;
import org.apache.hadoop.hbase.ipc.BlockingRpcCallback;
import org.apache.hadoop.hbase.ipc.ServerRpcController;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Administrator on 2016/12/27.
 */
public class FaceFeatureCompClient {
    protected static final Log LOG = LogFactory.getLog(ImageSearchClient.class);

    public FeatureCompOutData getFeatureCompResult(final Table table, String feature, float threshold)
        throws Throwable {
        final FaceFeatureCompProtos.FaceFeatureCompRequest requestArg = validateArgAndGetPB(feature, threshold);
        /**
         * FaceFeatureCompCallBack
         */
        class FaceFeatureCompCallBack implements Batch.Callback<FaceFeatureCompProtos.FaceFeatureCompResponse> {
            List<FeatureCompOutData.FeatureCompId> list = new CopyOnWriteArrayList<FeatureCompOutData.FeatureCompId>();
            AtomicBoolean isIdExsit = new AtomicBoolean(false);

            // 查询结果按相似度升序排序
            public FeatureCompOutData getFeatureCompResult() {
                FeatureCompOutData out = new FeatureCompOutData();
                out.setFeatureIds(list);
                out.setIdExsit(isIdExsit.get());
                return out;
            }

            // 保存查询结果
            @Override
            public void update(byte[] region, byte[] row, FaceFeatureCompProtos.FaceFeatureCompResponse result) {
                if (result != null) {
                    if (result.getIsIdExist()) {
                        isIdExsit.set(true);
                        for (FaceFeatureCompProtos.FaceFeatureCompOut featureOut : result.getFeatureCompOutList()) {
                            FeatureCompOutData.FeatureCompId featureCompId = new FeatureCompOutData.FeatureCompId();
                            featureCompId.setSim(featureOut.getSim());
                            featureCompId.setId(featureOut.getId());
                            list.add(featureCompId);
                        }

                    }
                }
            }
        }

        // 查询结果返回
        FaceFeatureCompCallBack callBack = new FaceFeatureCompCallBack();
        table.coprocessorService(FaceFeatureCompProtos.FaceFeatureCompService.class, null, null,
            new Batch.Call<FaceFeatureCompProtos.FaceFeatureCompService, FaceFeatureCompProtos.FaceFeatureCompResponse>() {
                @Override
                public FaceFeatureCompProtos.FaceFeatureCompResponse call(
                    FaceFeatureCompProtos.FaceFeatureCompService instance) throws IOException {
                    ServerRpcController controller = new ServerRpcController();
                    BlockingRpcCallback<FaceFeatureCompProtos.FaceFeatureCompResponse> rpcCallback = new BlockingRpcCallback<FaceFeatureCompProtos.FaceFeatureCompResponse>();
                    instance.getFaceFeatureCompResult(controller, requestArg, rpcCallback);
                    FaceFeatureCompProtos.FaceFeatureCompResponse response = rpcCallback.get();
                    if (controller.failedOnException()) {
                        throw controller.getFailedOn();
                    }
                    // 获取服务端查询结果list
                    return response;
                }
            }, callBack);
        return callBack.getFeatureCompResult();
    }

    // 解析输入参数
    FaceFeatureCompProtos.FaceFeatureCompRequest validateArgAndGetPB(String feature, float threshold)
        throws IOException {
        if (feature == null || threshold < 0 || threshold > 1) {
            throw new IOException("FaceFeatureCompClient Exception: Param is invalid !!");
        }
        final FaceFeatureCompProtos.FaceFeatureCompRequest.Builder requestBuilder = FaceFeatureCompProtos.FaceFeatureCompRequest
            .newBuilder();
        requestBuilder.setImageFeature(feature);
        requestBuilder.setThreshold(threshold); // 相似度阈值
        return requestBuilder.build();
    }
}

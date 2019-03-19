package com.znv.hbase.client.coprocessor;

import com.znv.hbase.client.featureComp.ImageSearchParam;
import com.znv.hbase.coprocessor.endpoint.searchByImage.ImageSearchOutData;
import com.znv.hbase.protobuf.generated.BlackStaticSearchProtos;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.client.coprocessor.Batch;
import org.apache.hadoop.hbase.ipc.BlockingRpcCallback;
import org.apache.hadoop.hbase.ipc.ServerRpcController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by estine on 2017/2/15.
 */
public class BlackStaticSearchClient {
    protected static final Log LOG = LogFactory.getLog(BlackStaticSearchClient.class);

    public List<ImageSearchOutData> getBlackStaticSearchResult(final Table table, ImageSearchParam param)
        throws Throwable {
        final BlackStaticSearchProtos.BlackStaticSearchRequest requestArg = validateArgAndGetPB(param);
        /**
         * BlackStaticSearchCallBack
         */
        class BlackStaticSearchCallBack implements Batch.Callback<BlackStaticSearchProtos.BlackStaticSearchResponse> {
            List<ImageSearchOutData> list = new CopyOnWriteArrayList<ImageSearchOutData>();
            List<ImageSearchOutData> sortList = new ArrayList<ImageSearchOutData>();

            // 查询结果按相似度降序排序
            public List<ImageSearchOutData> getBlackStaticSearchResult() {
                // sortList赋值
                for (ImageSearchOutData res : list) {
                    sortList.add(res);
                }
                // 相似度降序排序
                Collections.sort(sortList, new Comparator<ImageSearchOutData>() {
                    @Override
                    public int compare(ImageSearchOutData o1, ImageSearchOutData o2) {
                        Float sim1 = o1.getSuspectSim();
                        Float sim2 = o2.getSuspectSim();
                        return sim2.compareTo(sim1);
                    }
                });
                return sortList;
            }

            @Override
            public void update(byte[] region, byte[] row, BlackStaticSearchProtos.BlackStaticSearchResponse result) {
                if (result != null) {
                    for (BlackStaticSearchProtos.BlackStaticSearchOut out : result.getPictureSetList()) {
                        ImageSearchOutData temp = new ImageSearchOutData();
                        temp.setBytesRowKey(out.getRowKey());
                        temp.setSuspectSim(out.getSim());
                        list.add(temp);
                    }
                }
            }
        }

        // 查询结果返回
        BlackStaticSearchCallBack callBack = new BlackStaticSearchCallBack();
        table.coprocessorService(BlackStaticSearchProtos.BlackStaticSearchService.class, null, null,
            new Batch.Call<BlackStaticSearchProtos.BlackStaticSearchService, BlackStaticSearchProtos.BlackStaticSearchResponse>() {
                @Override
                public BlackStaticSearchProtos.BlackStaticSearchResponse call(
                    BlackStaticSearchProtos.BlackStaticSearchService instance) throws IOException {
                    ServerRpcController controller = new ServerRpcController();
                    BlockingRpcCallback<BlackStaticSearchProtos.BlackStaticSearchResponse> rpcCallback = new BlockingRpcCallback<BlackStaticSearchProtos.BlackStaticSearchResponse>();
                    instance.getBlackStaticSearchResult(controller, requestArg, rpcCallback);
                    BlackStaticSearchProtos.BlackStaticSearchResponse response = rpcCallback.get();
                    if (controller.failedOnException()) {
                        throw controller.getFailedOn();
                    }
                    return response;
                }
            }, callBack);
        return callBack.getBlackStaticSearchResult();
    }

    // 解析输入参数
    BlackStaticSearchProtos.BlackStaticSearchRequest validateArgAndGetPB(ImageSearchParam param) throws IOException {
        if (param == null) {
            throw new IOException("ImageSearchClient Exception: Param List is null");
        }
        final BlackStaticSearchProtos.BlackStaticSearchRequest.Builder requestBuilder = BlackStaticSearchProtos.BlackStaticSearchRequest
            .newBuilder();

        if (StringUtils.isBlank(param.getSearchFeatures().get(0))) {
            throw new IllegalArgumentException("图片不能为空！");
        } else {
            requestBuilder.setImageFeature(param.getSearchFeatures().get(0));
        }
        if (StringUtils.isBlank(Float.toString(param.getThreshold()))) {
            throw new IllegalArgumentException("相似度阈值不能为空！");
        } else {
            requestBuilder.setThreshold(param.getThreshold()); // 相似度阈值,float
        }
        return requestBuilder.build();
    }
}

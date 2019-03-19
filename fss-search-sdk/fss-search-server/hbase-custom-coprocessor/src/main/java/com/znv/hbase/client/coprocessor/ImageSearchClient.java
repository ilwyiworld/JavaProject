package com.znv.hbase.client.coprocessor;

import com.znv.hbase.client.featureComp.ImageSearchParam;
import com.znv.hbase.coprocessor.endpoint.searchByImage.ImageSearchOutData;
import com.znv.hbase.protobuf.generated.ImageSearchProtos;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.client.coprocessor.Batch;
import org.apache.hadoop.hbase.ipc.BlockingRpcCallback;
import org.apache.hadoop.hbase.ipc.ServerRpcController;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by estine on 2016/12/23.
 */
public class ImageSearchClient {
    protected static final Log LOG = LogFactory.getLog(ImageSearchClient.class);

    public List<ImageSearchOutData> getSearchByImageResult(final Table table, ImageSearchParam param) throws Throwable {
        final ImageSearchProtos.ImageSearchRequest requestArg = validateArgAndGetPB(param);
        /**
         * ImageSearchCallBack
         */
        class ImageSearchCallBack implements Batch.Callback<ImageSearchProtos.ImageSearchResponse> {
            List<ImageSearchOutData> list = new CopyOnWriteArrayList<ImageSearchOutData>();
         //   List<ImageSearchOutData> sortList = new ArrayList<ImageSearchOutData>();

            // 以脸搜脸查询结果按相似度降序排序
            public List<ImageSearchOutData> getSearchByImageQueryResult() {
                /*if (param.getSearchType().equals("1")) {
                    // sortList赋值
                    sortList.addAll(list);
                    // 以脸搜脸-相似度降序排序
                    if (param.getSearchType().equals("1")) {
                        Collections.sort(sortList, new Comparator<ImageSearchOutData>() {
                            @Override
                            public int compare(ImageSearchOutData o1, ImageSearchOutData o2) {
                                Float sim1 = o1.getSuspectSim();
                                Float sim2 = o2.getSuspectSim();
                                return sim2.compareTo(sim1);
                            }
                        });
                    }
                    return sortList;
                }*/
                //先不排序，在查询客户端按条件排序 #hy 2017/08/10
                return list;
            }

            @Override
            public void update(byte[] region, byte[] row, ImageSearchProtos.ImageSearchResponse result) {
                if (result != null) {
                    for (ImageSearchProtos.ImageSearchOut out : result.getPictureSetList()) {
                        ImageSearchOutData temp = new ImageSearchOutData();
                        temp.setSuspectRowKey(out.getRowKey().toByteArray());
                        temp.setSuspectSim(out.getSim());
                        temp.setLeaveTime(out.getLeaveTime());
                        temp.setCameraId(out.getCameraId());
                        temp.setEnterTime(out.getEnterTime());
                        temp.setDurationTime(out.getDurationTime());
                        temp.setGpsx(out.getGpsx());
                        temp.setGpsy(out.getGpsy());
                        temp.setUuid(out.getUuid()); // [estine]
                        temp.setCameraName(out.getCameraName()); // [estine]
                        list.add(temp);
                    }
                }
            }

        }

        // 以脸搜脸查询结果返回
        ImageSearchCallBack callBack = new ImageSearchCallBack();
        table.coprocessorService(ImageSearchProtos.ImageSearchService.class, null, null,
                new Batch.Call<ImageSearchProtos.ImageSearchService, ImageSearchProtos.ImageSearchResponse>() {
                    @Override
                    public ImageSearchProtos.ImageSearchResponse call(ImageSearchProtos.ImageSearchService instance)
                            throws IOException {
                        ServerRpcController controller = new ServerRpcController();
                        BlockingRpcCallback<ImageSearchProtos.ImageSearchResponse> rpcCallback = new BlockingRpcCallback<ImageSearchProtos.ImageSearchResponse>();
                        instance.getImageSearchResult(controller, requestArg, rpcCallback);
                        ImageSearchProtos.ImageSearchResponse response = rpcCallback.get();
                        if (controller.failedOnException()) {
                            throw controller.getFailedOn();
                        }
                        return response;
                    }
                }, callBack);
        return callBack.getSearchByImageQueryResult();
    }


    // 解析输入参数
    ImageSearchProtos.ImageSearchRequest validateArgAndGetPB(ImageSearchParam param) throws IOException {
        if (param == null) {
            throw new IOException("ImageSearchClient Exception: Param List is null");
        }
        final ImageSearchProtos.ImageSearchRequest.Builder requestBuilder = ImageSearchProtos.ImageSearchRequest
                .newBuilder();

        // if (StringUtils.isBlank(param.getSearchFeature())) {
        // throw new IllegalArgumentException("图片不能为空！");
        // } else {
        // requestBuilder.setImageFeature(param.getSearchFeature());
        // }
        //requestBuilder.setImageFeature(param.getSearchFeature()); // 图片特征值可有可无

        if (param.getSearchFeatures().size() > 3) {
            throw new IllegalArgumentException("图片数量不能超过3张！");
        } else {
            requestBuilder.addAllImageFeature(param.getSearchFeatures());
        }

        if (StringUtils.isBlank(param.getSelType())) {
            throw new IllegalArgumentException("交并集类型不能为空！");
        } else {
            requestBuilder.setSelType(param.getSelType());
        }

        if (StringUtils.isBlank(param.getStartTime())) {
            throw new IllegalArgumentException("开始时间不能为空！");
        } else {
            requestBuilder.setStartTime(param.getStartTime());
        }
        if (StringUtils.isBlank(param.getEndTime())) {
            throw new IllegalArgumentException("结束时间不能为空！");
        } else {
            requestBuilder.setEndTime(param.getEndTime());
        }
        if (StringUtils.isBlank(param.getSearchType())) {
            throw new IllegalArgumentException("查询类型不能为空！");
        } else {
            requestBuilder.setSearchType(param.getSearchType());
        }
        // cameraId可选
        requestBuilder.addAllCameraId(param.getCameraIds());
        requestBuilder.addAllOfficeId(param.getOfficeIds()); // officeId可选
        requestBuilder.setThreshold(param.getThreshold()); // 相似度阈值
        return requestBuilder.build();
    }
}

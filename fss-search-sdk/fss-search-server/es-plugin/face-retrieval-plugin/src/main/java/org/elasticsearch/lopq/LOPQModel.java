package org.elasticsearch.lopq;

//import com.sun.xml.internal.bind.v2.runtime.output.SAXOutput;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.index.mapper.image.FeatureFieldMapper;
import org.elasticsearch.protobuf.generated.LOPQModelParameters;
import org.elasticsearch.util.FeatureCompUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LOPQModel {
    private static Logger logger = LogManager.getLogger(LOPQModel.class.getName());
    private static float[][][] csNorm = null; // 归一化后的聚类中心
    private static float[][][] csNormBefore = null; //未归一化的聚类中心
    private static float[][][][] Rs = null;// rotations - 2 * V of these, each size D/2 x D/2
    private static float[][][] Mus = null;// residual means - 2 * V of these, each size D/2
    private static float[][][][] Subs = null;// subquantizer centroids - M of these, each size num_subquantizers x (D/2))

    private static int numCoarseSplits = 1;
   // private static int numCoarseCenters = 3;
    private static int numFineCenters = 3;
    private static int numFineSplits = 2; //细分类分两类
    private static int M; // number of subvectors
    private static int V;  // number of coarse quantizer centroids
    private static int D; //dimensionality of original vectors
    private static int num_subquantizers; // number of subquantizer clusters

    private static FeatureCompUtil fc = new FeatureCompUtil();

    public static void loadProto(InputStream input) throws IOException {
        LOPQModelParameters.LOPQModelParams.Builder builder = LOPQModelParameters.LOPQModelParams.newBuilder();
        LOPQModelParameters.LOPQModelParams lopqParams = builder.build();

        lopqParams = lopqParams.getParserForType().parseFrom(input);
        input.close();

        numCoarseSplits = lopqParams.getCsCount();
        if (numCoarseSplits == 0) {
            throw new IllegalArgumentException("Malformed lopq model params");
        }

        V = lopqParams.getV();
        D = lopqParams.getD();
        M = lopqParams.getM();
        num_subquantizers = lopqParams.getNumSubquantizers();
        int splitLen = D / numCoarseSplits;

        csNorm = new float[numCoarseSplits][V][splitLen];
        csNormBefore = new float[numCoarseSplits][V][splitLen];
        Float[][][] cs = new Float[numCoarseSplits][V][splitLen];
        for (int i = 0; i < numCoarseSplits; i++) {
            LOPQModelParameters.Matrix mCs = lopqParams.getCs(i);
            assert mCs.getShapeCount() == 2;
            assert mCs.getShape(0) == V;
            assert mCs.getShape(1) == splitLen;
            reShapeMatrix(mCs, cs[i]);
//            reShapeMatrix(mCs, csNorm[i]);
            for (int j = 0; j < V; j++) {
                vectorNorm(cs[i][j], csNorm[i][j]);
//                vectorNorm(csNorm[i][j]);
                for (int k = 0; k < splitLen; k++) {
                    csNormBefore[i][j][k] = cs[i][j][k];
                }
            }
        }


        int size = D / numCoarseSplits; //D = 256
        int nums = V * numCoarseSplits; //V = 16

        //Rs
        Rs = new float[numCoarseSplits][V][size][size];
        Float[][][] rsTemp = new Float[nums][size][size];
        for (int i = 0; i < lopqParams.getRsCount(); i++) {
            LOPQModelParameters.Matrix rs = lopqParams.getRs(i);

            reShapeMatrix(rs, rsTemp[i]);
        }

        for (int i = 0; i < Rs.length; i++) {
            int count = V * i;
            for (int j = 0; j < Rs[i].length; j++) {
                for (int k = 0; k < rsTemp[j].length; k++) {
                    for (int n = 0; n < rsTemp[j][k].length; n++) {
                        Rs[i][j][k][n] = rsTemp[j + count][k][n];
                    }
                }
            }
        }

        //Mus
        Mus = new float[numCoarseSplits][V][size];
        Float[][] musTemp = new Float[nums][size];
        for (int i = 0; i < lopqParams.getMusCount(); i++) {
            LOPQModelParameters.Vector mus = lopqParams.getMus(i);
            int len = mus.getValuesCount();
            Float vals[] = new Float[size];
            vals = mus.getValuesList().toArray(vals);

            System.arraycopy(vals, 0, musTemp[i], 0, len);
        }
        for (int i = 0; i < Mus.length; i++) {
            int count = V * i;
            for (int j = 0; j < Mus[i].length; j++) {
                for (int k = 0; k < Mus[i][j].length; k++) {
                    Mus[i][j][k] = musTemp[j + count][k];
                }
            }
        }

        //Subs
        Subs = new float[numCoarseSplits][M / numCoarseSplits][num_subquantizers][D / M];
        Float[][][] subsTemp = new Float[M][num_subquantizers][D / M];
        for (int i = 0; i < lopqParams.getSubsCount(); i++) {
            LOPQModelParameters.Matrix subs = lopqParams.getSubs(i);

            reShapeMatrix(subs, subsTemp[i]);
        }

        for (int i = 0; i < Subs.length; i++) {
            int count = M * i / numCoarseSplits; //需要确认
            for (int j = 0; j < Subs[i].length; j++) {
                for (int k = 0; k < Subs[i][j].length; k++) {
                    vectorNorm(subsTemp[j + count][k], Subs[i][j][k]);
                }
            }
        }
        cs = null;
        rsTemp = null;
        musTemp = null;
        subsTemp = null;
    }

    public static int[] predictFine(float[] feature, int[] coarseCode) {
        int[] fineCode = new int[numCoarseSplits * numFineSplits];
        if (coarseCode == null) {
            coarseCode = predictCoarse(feature);
        }
        float[] px = prjectCode(feature, coarseCode);
        float[][] cx = iterateSplit(px, numCoarseSplits);

        if (cx != null) {
            for (int i = 0; i < cx.length; i++) {
                float[][] fx = iterateSplit(cx[i], numFineSplits);

                if (fx != null) {
                    for (int j = 0; j < fx.length; j++) {
                        vectorNorm(fx[j]);
                        fineCode[i * numFineSplits + j] = predictClusterNew(fx[j], Subs[i][j]);
                    }
                } else {
                    logger.error("fx is null!");
                }
            }
        } else {
            logger.error("cx is null!");
        }
        return fineCode;
    }

    public static int[][] predictFineOrder(float[] feature, int[] coarseCode) {
        int[][] fincodes = new int[numFineCenters][numCoarseSplits * numFineSplits];

        if (coarseCode == null) {
            coarseCode = predictCoarse(feature);
        }
        float[] px = prjectCode(feature, coarseCode);
        float[][] cx = iterateSplit(px, numCoarseSplits);

        if (cx != null) {
            for (int i = 0; i < cx.length; i++) {
                float[][] fx = iterateSplit(cx[i], numFineSplits);
                if (fx != null) {
                    for (int j = 0; j < fx.length; j++) {
                        vectorNorm(fx[j]);
                        int[] temp = predictClusterOrder(fx[j], Subs[i][j]);

                        for (int k = 0; k < numFineCenters; k++) {
                            fincodes[k][i * numFineSplits + j] = temp[k];
                        }
                    }
                } else {
                    logger.error("fx is null!");
                }
            }
        } else {
            logger.error("cx is null!");
        }
        return fincodes;
    }

    public static float[] prjectCode(float[] feature, int[] coarseCode) {
        // TODO something
        float[][] featureSplit = iterateSplit(feature);
        float[] pxArray = new float[feature.length];
        for (int i = 0; i < featureSplit.length; i++) {
            int cluster = coarseCode[i];
            float[] residual = computeResidual(featureSplit[i], csNormBefore[i][cluster]);
            float[] residualNorm = residual;
            if (Rs != null) {
                vectorNorm(residualNorm);
                float[][] rsCluster = Rs[i][cluster];//R[cluster]
                float[][] rotate = new float[rsCluster.length][rsCluster[0].length];//rsCluster归一化后
                vectorNorm1(rsCluster, rotate);
                float[] musCluster = Mus[i][cluster];
                float[] d = computeResidual(residualNorm, musCluster);
                vectorNorm(d);
                pxArray = dot(rotate, d);
             /*   float[] tempArray = dot(rotate,d);
                int len = tempArray.length;
                System.arraycopy(tempArray, 0, pxArray, i * len, len);*/
            }
        }
        return pxArray;
    }


    public static float[] dot(float[][] rs, float[] rmu) {
        if (rmu == null) {
            logger.error("array feature is null!");
            return null;
        }
        float[] dotArray = new float[rs.length];
        for (int i = 0; i < rs.length; i++) {
            if (rs[i].length != rmu.length) {
                logger.error("array length unequal!");
                return null;
            }

            float sum = 0.0f;
            for (int j = 0; j < rs[i].length; j++) {
                sum += rs[i][j] * rmu[j];
            }
            dotArray[i] = sum;
        }
        return dotArray;
    }

    public static float[] computeResidual(float[] featureSplit, float[] cx) {

        if (featureSplit == null ) {
            logger.error("feature array is null!");
            return null;
        }
        if (featureSplit.length != cx.length) {
            logger.error("array length unequal!");
            return null;
        }
        float[] r = new float[featureSplit.length];

        for (int i = 0; i < featureSplit.length; i++) {
            r[i] = featureSplit[i] - cx[i];
        }
        return r;
    }

    //返回距离最近的前numCoarseCenters个聚类中心
    public static int[][] predictCoarseOrder(float[] feature,int numCoarseCenters) {
        int[][] nearCorseCode = new int[numCoarseCenters][numCoarseSplits];
        float[][] featureSplit = iterateSplit(feature);

        for (int j = 0; j < numCoarseSplits; j++) {
            if (numCoarseSplits > 1) {
                vectorNorm(featureSplit[j]);//商汤的特征值已经归一化，所以如果numCoarseSplits=1（特征值没有划分）的话就不用归一化
            }
            int[] temp = predictClusterOrder(featureSplit[j], csNorm[j]);//

            for (int i = 0; i < numCoarseCenters; i++) {
                nearCorseCode[i][j] = temp[i];
            }
        }
        return nearCorseCode;
    }

    /*    return nearCoarseCode start     */
    public static int[] predictClusterOrder(float[] f, float[][] c) {
        float min = -1f;
        float max = Float.MAX_VALUE;
        int len = c.length;
        float cosinDistance[] = new float[len];
        for (int j = 0; j < len; j++) {
            cosinDistance[j] = fc.cosDistance(f, c[j], 0);
        }
        vectorNorm(cosinDistance);
        Map<String, String> map = new HashMap<>();
        float code[] = new float[len];
        int[] coarseCode = new int[len];
        for (int j = 0; j < len; j++) {
            float sim = (float) Math.sqrt(2 - 2 * cosinDistance[j]); //归一化后
            code[j] = sim;
            map.put(Float.toString(sim), Integer.toString(j));
        }
        Arrays.parallelSort(code);//正序排序
       /*//取最大的len个值
       for (int i = 1; i <= len; i++) {
            float sim = code[len - i];
            coarseCode[i - 1] = Integer.valueOf(map.get(Float.toString(sim)));
        }*/
        //取最小的len个值
        for (int i = 0; i < len; i++) {
            float sim = code[i];
            coarseCode[i] = Integer.valueOf(map.get(Float.toString(sim)));
        }
        return coarseCode;
    }

    /*    return nearCoarseCode end     */

    // 向量必须已经归一化
    public static int predictCluster(float[] f, float[][] c) {
        float min = -1f;
        int len = c.length;
        int code = -1;
        for (int j = 0; j < len; j++) {
            float sim = fc.Dot(f, c[j], 0);
            if (sim >= min) { //取余弦距离最大值
                min = sim;
                code = j;
            }
        }
        return code;
    }

    public static int[] predictCoarse(float[] feature) {
        float[][] featureSplit = iterateSplit(feature);
        int[] minCode = new int[numCoarseSplits];
        for (int i = 0; i < numCoarseSplits; i++) {
            if (numCoarseSplits > 1) {
                vectorNorm(featureSplit[i]);//商汤的特征值已经归一化，所以如果numCoarseSplits=1（特征值没有划分）的话就不用归一化
            }
            minCode[i] = predictClusterNew(featureSplit[i], csNorm[i]);
        }
        return minCode;
    }

    public static int predictClusterNew(float[] f, float[][] c) {
        float max = Float.MAX_VALUE;
        int len = c.length;
        int code = -1;
        float cosinDistance[] = new float[len];
        for (int j = 0; j < len; j++) {
            cosinDistance[j] = fc.cosDistance(f, c[j], 0);
        }
        vectorNorm(cosinDistance);
      /*  float fNorm = twoNorm(f); //归一化后为1
        float cNorm[] = new float[len];
        for(int i=0 ; i<len;i++){
            cNorm[i] =twoNorm(c[i]); //归一化后为1
        }
        float base[] = new float[len];
        for(int k=0;k<len ;k++){
            base[k] = fNorm*cNorm[k]; //归一化后为1
        }
        float fNorm2 = fNorm*fNorm;
        float front[] = new float[len];
        for(int h = 0;h < len ;h++){
            front[h] = fNorm2+cNorm[h]*cNorm[h]; //归一化后front[h]为2
        }*/
        for (int s = 0; s < len; s++) {
            //float sim = (float) Math.sqrt(front[s]-2*cosinDistance[s]*base[s]);
            float sim = (float) Math.sqrt(2 - 2 * cosinDistance[s]); //归一化后
            if (sim <= max) { //取最小值
                max = sim;
                code = s;
            }
        }
        return code; //聚类中心的下标
    }

    private static float twoNorm(float[] before) {
        float sum = 0f;
        int len = before.length;
        for (int i = 0; i < len; i++) {
            sum += before[i] * before[i];
        }
        sum = (float) Math.sqrt(sum);
        return sum;
    }


    private static float[][] iterateSplit(float[] feature, int splits) {
        if (feature == null) {
            return null;
        } else {
            int splitSize = feature.length / splits;
            float[][] featureSplit = new float[splits][splitSize];
            for (int i = 0; i < splits; i++) {
                float[] f = featureSplit[i];
                System.arraycopy(feature, i * splitSize, f, 0, splitSize);
            }
            return featureSplit;
        }
    }

    private static float[][] iterateSplit(float[] feature) {
        int splitSize = feature.length / numCoarseSplits;
        float[][] featureSplit = new float[numCoarseSplits][splitSize];
        for (int i = 0; i < numCoarseSplits; i++) {
            float[] f = featureSplit[i];
            System.arraycopy(feature, i * splitSize, f, 0, splitSize);
        }
        return featureSplit;
    }

    public static int[] predict(float[] feature) {
        // 计算粗分类中心
        int[] corseCode = predictCoarse(feature);
        //todo 计算细分类中心
        return corseCode;
    }

    private static void reShapeMatrix(LOPQModelParameters.Matrix before, Float[][] after) {
        int dataLen = before.getValuesCount();
        int r = before.getShape(0);
        int c = before.getShape(1);
        assert dataLen == r * c;
        assert after != null && after.length == r && after[0].length == c;
        Float vals[] = new Float[dataLen];
        vals = before.getValuesList().toArray(vals);

        for (int i = 0; i < r; i++) {
            System.arraycopy(vals, i * c, after[i], 0, c);
        }
    }

    private static void reShapeMatrix(LOPQModelParameters.Matrix before, float[][] after) {
        int dataLen = before.getValuesCount();
        int r = before.getShape(0);
        int c = before.getShape(1);
        assert dataLen == r * c;
        assert after != null && after.length == r && after[0].length == c;
        Float vals[] = new Float[dataLen];
        vals = before.getValuesList().toArray(vals);

        for (int i = 0; i < r; i++) {
            float[] tmp = after[i];
            for (int j = 0; j < c; j++) {
                int index = i * c + j;
                tmp[j] = before.getValues(index);
            }
        }
    }


    private static void vectorNorm(Float[] before, float[] after) {
        float sum = 0f;
        int len = before.length;
        for (int i = 0; i < len; i++) {
            sum += before[i] * before[i];
        }
        sum = (float) Math.sqrt(sum);
        for (int i = 0; i < len; i++) {
            after[i] = (float) before[i] / sum;
        }
    }

    //对每行数据分别进行归一化
    private static void vectorNorm1(float[][] before, float[][] after) {
        int len = before.length;
        float sum[] = new float[len];
        for (int i = 0; i < len; i++) {
            for (int j = 0; j < before[0].length; j++) {
                sum[i] += before[i][j] * before[i][j];
            }
            sum[i] = (float) Math.sqrt(sum[i]);
        }
        for (int i = 0; i < len; i++) {
            for (int j = 0; j < before[0].length; j++) {
                after[i][j] = (float) before[i][j] / sum[i];
            }
        }
    }


    private static void vectorNorm(float[] vec) {
        float sum = 0f;
        if (vec == null) {
            return;
        } else {
            int len = vec.length;
            for (int i = 0; i < len; i++) {
                sum += vec[i] * vec[i];
            }
            sum = (float) Math.sqrt(sum);
            for (int i = 0; i < len; i++) {
                vec[i] = (float) vec[i] / sum;
            }
        }
    }

    public static void printCode(int[] code) {
        for (int i = 0; i < code.length; i++) {
            if (i == 0) {
                System.out.print("[");
            }
            System.out.print(code[i]);
            if (i == (code.length - 1)) {
                System.out.println("]");
                return;
            }
            System.out.print(", ");
        }
    }

    public static void printCodeOrder(int[][] code) {
        for (int i = 0; i < code.length; i++) {
            if (i == 0) {
                System.out.print("[");
            }
            for (int j = 0; j < code[0].length; j++) {
                if (j == 0) {
                    System.out.print("[");
                }
                System.out.print(code[i][j]);
                if (j < code[0].length - 1) {
                    System.out.print(",");
                }
                if (j == code[0].length - 1) {
                    System.out.print("]");
                }

            }

            if (i == (code.length - 1)) {
                System.out.println("]");
                return;
            }
            System.out.print(", ");
        }
    }

    public static void main(String[] args) {
        try {
            LOPQModel.loadProto(FeatureFieldMapper.class.getResourceAsStream("/lopq/lopq_model_V1.0_D512_C36.lopq"));
            List<String> features = new ArrayList<String>();
            features.add("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");
            features.add("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");

            for (String f : features) {
                //只返回一个距离最近的聚类中心
                int[] corseCode = predict(fc.getFloatArray(new org.apache.commons.codec.binary.Base64().decode(f)));
                //System.out.println(corseCode[0] + "," + corseCode[1]);
                //返回numCoarseCenters个距离最近的聚类中心
                int[][] coarseCodeOrder = predictCoarseOrder(fc.getFloatArray(new org.apache.commons.codec.binary.Base64().decode(f)),3);
                System.out.println(coarseCodeOrder.length);
                int[] fineCode = predictFine(fc.getFloatArray(new org.apache.commons.codec.binary.Base64().decode(f)), corseCode);
                int[][] fineCodeOrder = predictFineOrder(fc.getFloatArray(new org.apache.commons.codec.binary.Base64().decode(f)), corseCode);
                printCode(corseCode);
                printCodeOrder(coarseCodeOrder);
                printCode(fineCode);
                printCodeOrder(fineCodeOrder);
            }
        } catch (Exception e) {
            logger.error(e);
        }
    }

}

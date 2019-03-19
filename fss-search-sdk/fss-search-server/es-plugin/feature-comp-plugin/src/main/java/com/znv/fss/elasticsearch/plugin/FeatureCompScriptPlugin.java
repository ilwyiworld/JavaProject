package com.znv.fss.elasticsearch.plugin;

import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.util.BytesRef;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.index.fielddata.ScriptDocValues;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.plugins.ScriptPlugin;
import org.elasticsearch.script.AbstractDoubleSearchScript;
import org.elasticsearch.script.AbstractSearchScript;
import org.elasticsearch.script.ExecutableScript;
import org.elasticsearch.script.NativeScriptFactory;
import org.elasticsearch.search.lookup.FieldLookup;

import java.util.*;

/**
 * Created by Administrator on 2017/2/22.
 */
public class FeatureCompScriptPlugin extends Plugin implements ScriptPlugin {
    @Override
    public List<NativeScriptFactory> getNativeScripts() {
        return Collections.singletonList(new FeatureCompNativeScriptFactory());
    }

    public static class FeatureCompNativeScriptFactory implements NativeScriptFactory {
        @Override
        public ExecutableScript newScript(@Nullable Map<String, Object> params) {
            return new FeatureCompNativeScript(params);
        }

        @Override
        public boolean needsScores() {
            return false;
        }

        @Override
        public String getName() {
            return "feature-comp";
        }
    }

    public static class FeatureCompNativeScript extends AbstractDoubleSearchScript {
        protected final Logger logger = LogManager.getLogger(FeatureCompNativeScript.class);
        private String filterType = null;
        private String featureName;
        // private List<Double> featureValue;
        private List<List<Double>> featureValues = new ArrayList<>();
        private List<byte[]> featureValuesBytes = new ArrayList<>();// new ArrayList();
        private int featureSize = 0;

        public FeatureCompNativeScript(Map<String, Object> params) {
            this.filterType = params.get("filterType").toString();
            this.featureName = params.get("featureName").toString();
            List<String> featureStr = (List<String>) params.get("featureValue");
            for (int i = 0; i < featureStr.size(); i++) {
//                byte[] feature_high = new Base64().decode(featureStr.get(i));
//                // this.featureValues.add(i, getDoubleList(feature_high, 12));
//                this.featureValuesBytes.add(i, feature_high);

                // featureStr中可能存在空字符串
                String tempFeature = featureStr.get(i);
                if (!tempFeature.isEmpty() && !"".equals(tempFeature) && !"null".equals(tempFeature)) {
                    byte[] feature_high = new Base64().decode(tempFeature);
                    // this.featureValues.add(i, getDoubleList(feature_high, 12));
                    //this.featureValuesBytes.add(i, feature_high);
                    this.featureValuesBytes.add(feature_high);
                }
            }
//            this.featureName = params.get("featureName").toString();
//            String featureStr = params.get("featureValue").toString();
//            byte[] feature_high = new Base64().decode(featureStr);
//            this.featureValue = getDoubleList(feature_high, 12);
            this.featureSize = featureValuesBytes.size();
        }

        @Override
        public double runAsDouble() {
            // FieldLookup storefield = (FieldLookup)(fields().get(featureName));
            // List<Object> featurelist = (storefield.getValues());

            BytesRef feature = ((ScriptDocValues.BytesRefs) doc().get(featureName)).get(0);
            //return Normalize(Dot(featureValue, feature));
            // 交集
            if (filterType.toLowerCase().equals("and")) {
                float min = 2f;// Float.MAX_VALUE; // max sim must less than 1
                for (int i = 0; i < featureSize/*featureValues.size()*/; i++) {
                    // float featureFloat = Dot(featureValues.get(i), feature);
                    float featureFloat = DotV2(featureValuesBytes.get(i), feature);
                    min = Math.min(featureFloat, min);
                }
                return min;
            }
            // 并集
            else if (filterType.toLowerCase().equals("or")) {
                float max = 0f;// Float.MIN_VALUE; // 0f < 1.4E-45
                for (int i = 0; i < featureSize /*featureValues.size()*/; i++) {
                    // float featureFloat = Dot(featureValues.get(i), feature);
                    float featureFloat = DotV2(featureValuesBytes.get(i), feature);
                    max = Math.max(featureFloat, max);
                }
                return max;
            } else {
                float max = Float.MIN_VALUE;
                for (int i = 0; i < featureSize /*featureValues.size()*/; i++) {
                    // float featureFloat = Dot(featureValues.get(i), feature);
                    float featureFloat = DotV2(featureValuesBytes.get(i), feature);
                    max = Math.max(featureFloat, max);
                }
                return max;
            }
        }

        private List<Double> getDoubleList(byte[] bytes, int offset) {
            if (0 != (bytes.length - offset) % 4) {
                this.logger.debug("feature dimension is incompeleted");
                return new ArrayList(1);
            }

            if (bytes.length < offset) {
                this.logger.debug("feature length is too short");
                return new ArrayList(1);
            }

            int len = (bytes.length - offset) / 4;
            List feature = new ArrayList(len);
            for (int i = 0; i < len; i++) {
                feature.add(Double.valueOf(Float.intBitsToFloat(GetInt(bytes, offset))));
                offset += 4;
            }
            return feature;
        }

        private float Dot(List<Double> f1, BytesRef feature) {
            int offset = feature.offset + 12; // 12 bytes head (support 253 model)
            int f2rawlength = feature.length - 12;
            if (0 != f2rawlength % 4) {
                // throw new Exception("feature dimension is incompeleted");
                logger.info("feature dimension is incompeleted");
                return 0f;
            }

            int dimCnt = f2rawlength / 4;
            // logger.info("f1 length = "+ f1.size() + ", f2 length= "+feature.length+ ", dimCnt="+dimCnt);//hy test
            if (f1.size() != dimCnt) {
                logger.info("feature length unmatch");
                return 0f;
            }

            float dist = 0.0f;
            byte[] f2 = feature.bytes;
            for (int i = 0; i < dimCnt; i++) {
                dist += f1.get(i).floatValue() * Float.intBitsToFloat(GetInt(f2, offset));
                offset += 4;
            }

            return dist;
        }


        //compare with quan value(max value 31), total 192bytes
        public float DotV2(byte[] fQuery, BytesRef fComp) {
            int offset = 12;
            if ((fQuery.length - offset) != fComp.length * 16 / 3) {
                // throw new Exception("feature length unmatch");
//            LOG.debug("feature length unmatch");
                return 0f;
            }
            int[] res = new int[4];
            int i = 0;
            float dist = 0.0f;
            byte[] fCompress = fComp.bytes;
            while (offset < fQuery.length) {
                res[0] = ((fCompress[i] >>> 3) & 0x1f) * ((fCompress[i] >>> 2 & 0x01) == 0 ? 1 : -1);
                res[1] = (((fCompress[i] & 0x03) << 3) | ((fCompress[i + 1] >>> 5) & 0x07)) * ((fCompress[i + 1] >>> 4 & 0x01) == 0 ? 1 : -1);
                res[2] = (((fCompress[i + 1] & 0x0f) << 1) | ((fCompress[i + 2] >>> 7) & 0x01)) * ((fCompress[i + 2] >>> 6 & 0x01) == 0 ? 1 : -1);
                res[3] = ((fCompress[i + 2] & 0x3e) >> 1) * ((fCompress[i + 2] & 0x01) == 0 ? 1 : -1);
                for (int j = 0; j < 4; j++) {
                    dist += Float.intBitsToFloat(GetInt(fQuery, offset)) * res[j];
                    offset += 4;
                }
                i += 3;
            }

            return dist / 128;
        }

        /*
         * private float Dot(List<Double> f1, List<Object> f2) { if (f1.size() != f2.size()) {
         * logger.info("feature length unmatch"); return 0f; } int dimCnt = f1.size(); float dist = 0.0f; for (int i =
         * 0; i < dimCnt; i++) { dist += f1.get(i).floatValue() * ((float)(f2.get(i))); } return dist; }
         */

        private int GetInt(byte[] bytes, int offset) {
            // return (0xff & bytes[offset]) | (0xff00 & (bytes[offset + 1] << 8)) | (0xff0000 & (bytes[offset + 2] <<
            // 16)) | (0xff000000 & (bytes[offset + 3] << 24));
            return (0xff & bytes[offset]) | ((0xff & bytes[offset + 1]) << 8) | ((0xff & bytes[offset + 2]) << 16)
                | ((0xff & bytes[offset + 3]) << 24);
        }

        private float Normalize(float score) {
            if (score <= src_points[0]) {
                return dst_points[0];
            } else if (score >= src_points[src_points.length - 1]) {
                return dst_points[dst_points.length - 1];
            }

            float result = 0.0f;

            for (int i = 1; i < src_points.length; i++) {
                if (score < src_points[i]) {
                    result = dst_points[i - 1] + (score - src_points[i - 1]) * (dst_points[i] - dst_points[i - 1])
                        / (src_points[i] - src_points[i - 1]);
                    break;
                }
            }

            return result;
        }

        private float[] src_points = { 0.0f, 0.128612995148f, 0.236073002219f, 0.316282004118f, 0.382878988981f,
            0.441266000271f, 0.490464001894f, 1.0f };
        private float[] dst_points = { 0.0f, 0.40000000596f, 0.5f, 0.600000023842f, 0.699999988079f, 0.800000011921f,
            0.899999976158f, 1.0f };

    }

}

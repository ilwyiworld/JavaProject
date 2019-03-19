package org.elasticsearch.index.query.image;

import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.util.BytesRef;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.hashing.PCADimReduction;
import org.elasticsearch.index.fielddata.ScriptDocValues;
import org.elasticsearch.index.mapper.image.FeatureFieldMapper;
import org.elasticsearch.script.AbstractDoubleSearchScript;
import org.elasticsearch.script.ExecutableScript;
import org.elasticsearch.script.NativeScriptFactory;
import org.elasticsearch.util.FeatureCompUtil;
import org.elasticsearch.util.TypeConvertUtil;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/10/13.
 */
public class FeatureCompNativeScriptFactory implements NativeScriptFactory {
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
        return "image-feature-retrieval";
    }

    public static class FeatureCompNativeScript extends AbstractDoubleSearchScript {
        private static Logger logger = LogManager.getLogger(FeatureHashQuery.class.getName());
        private String fieldName;
        private List<Float> featureValue;
        // private List<Float> featureValueLow;
        private boolean useFeatureComp = false;
        private boolean useHash = false;
        private int hmThreshold = -1;
        private String hashDataType = "short"; //short or String
        private float lowThreshold = 0.6f;
        private String featureFieldName;
        // private String featureFieldNameLow;
        private String hashFieldName;
        private int featureOffset;
        private int hashvalShort;
        private String hashvalStr;
        private static FeatureCompUtil fc = new FeatureCompUtil();

        //        public FeatureCompNativeScript(Map<String, Object> params) {
//            this.fieldName = params.get("featureName").toString();
//            if (params.get("useFeatureComp") != null) {
//                this.useFeatureComp = (boolean)params.get("useFeatureComp");
//                if (this.useFeatureComp) {
//                    this.featureValue = (List<Double>) params.get("featureValue");
//                    if (this.featureValue.size() == PCADimReduction.dimensionBefore) {
//                        this.featureFieldName = this.fieldName + "." + FeatureFieldMapper.FEATURE_HIGH;
//                        this.featureOffset = 12;
//                    } else if (this.featureValue.size() == PCADimReduction.dimensionAfter) {
//                        this.featureFieldName = this.fieldName + "." + FeatureFieldMapper.FEATURE_LOW;
//                        this.featureOffset = 0;
//                    } else {
//                        logger.warn("The length of param featureValue is incorrect.", "length: " + this.featureValue.size());
//                    }
//                }
//            }
//            if (params.get("useHash") != null) {
//                this.useHash = (boolean) params.get("useHash");
//                if (this.useHash) {
//                    this.hashFieldName = fieldName + "." + FeatureFieldMapper.HASH + ".LSH";
//                    this.hashDataType = params.get("hashDataType").toString();
//                    if (this.hashDataType.equals("String")) {
//                        this.hashvalStr = params.get("hashValue").toString();
//                        this.hashFieldName += FeatureFieldMapper.HASH_STR_SUFFIX;
//                    } else {
//                        this.hashvalShort = (int) params.get("hashValue");
//                        this.hashFieldName += FeatureFieldMapper.HASH_SHORT_SUFFIX;
//                    }
//                    this.hmThreshold = (int) params.get("hmDistance");
//                }
//            }
//        }
//
        @Override
        public double runAsDouble() {
            //   logger.info("query feature length:"+featureValue.size());
            //  logger.info("query feature :"+getFeatureValue(featureValue));
            double sim = -1f;
            int hm_d = 0;
            if (useHash) {
                if (this.hashDataType.equals("String")) {
                    ScriptDocValues.Strings hashDoc = (ScriptDocValues.Strings) doc().get(hashFieldName);
                    hm_d = getDistance(hashvalStr, hashDoc.getValue());
                } else {
                    ScriptDocValues.Longs hashDoc = (ScriptDocValues.Longs) doc().get(hashFieldName);
                    int hashShort = (int) hashDoc.getValue();
                    int xor = hashvalShort ^ hashShort;
                    hm_d = TypeConvertUtil.intOf1(xor);
                }
                //    logger.info("val1:"+hashQuery + ", val2:"+hashDoc.getValue()+", hanming "+hm_d + ", hmThreshold "+ hmThreshold);
                if (hmThreshold == -1) {
                    sim = hm_d;
                } else if (hm_d <= hmThreshold) {
                    if (useFeatureComp) {
                        BytesRef feature = ((ScriptDocValues.BytesRefs) doc().get(featureFieldName)).get(0);
                        sim = fc.Dot(featureValue, feature, featureOffset);
                    } else {
                        sim = hm_d;
                    }
                }
                //   logger.info("sim:"+sim);

            } else if (useFeatureComp) {
                BytesRef feature = ((ScriptDocValues.BytesRefs) doc().get(featureFieldName)).get(0);
                sim = fc.Dot(featureValue, feature, featureOffset);
            }
            return sim;
        }

        public FeatureCompNativeScript(Map<String, Object> params) {
            this.fieldName = params.get("featureName").toString();
            this.useFeatureComp = ((Boolean) params.get("useFeatureComp")).booleanValue();
            String featureStr = params.get("featureValue").toString();
            this.lowThreshold = (float) ((double) params.get("lowThreshold"));
            byte[] feature_high = new Base64().decode(featureStr);
            // byte[] feature_low =
            // PCADimReduction.double2Bytes(PCADimReduction.generateLowDimensions(fc.getDoubleArray(feature_high, 12)));
            this.featureValue = fc.getFloatList(feature_high, 12);
            // this.featureValueLow = fc.getFloatList(feature_low, 0);
            this.featureFieldName = this.fieldName + "." + FeatureFieldMapper.FEATURE_HIGH;
            // this.featureFieldNameLow = this.fieldName + "." + FeatureFieldMapper.FEATURE_LOW;
        }
/*
        @Override
        public double runAsDouble() {
            //   logger.info("query feature length:"+featureValue.size());
            //  logger.info("query feature :"+getFeatureValue(featureValue));
            BytesRef featurelow = ((ScriptDocValues.BytesRefs) doc().get(featureFieldNameLow)).get(0);
            float sim = fc.Dot(featureValueLow, featurelow, 0);
            if (sim >= lowThreshold) {
                BytesRef feature = ((ScriptDocValues.BytesRefs)doc().get(featureFieldName)).get(0);
                sim = fc.Dot(featureValue, feature, 12);
            } else {
                sim = 0f;
            }

            return sim;
        }*/

        private String getFeatureValue(List<Double> floatFeature) {
            StringBuffer featureValue = new StringBuffer();
            String featureValueReturn = "";
            try {
                featureValue.append("[");
                for (int i = 0; i < floatFeature.size() - 1; i++) {
                    featureValue.append(floatFeature.get(i));
                    featureValue.append(",");
                }
                featureValue.append(floatFeature.get(floatFeature.size() - 1));
                featureValue.append("]");
                featureValueReturn = featureValue.toString();
            } catch (Exception e) {
                logger.error(e);
            }
            return featureValueReturn;
        }

        /**
         * calculate Hamming Distance between two strings
         *
         * @param str1 the 1st string
         * @param str2 the 2nd string
         * @return Hamming Distance between str1 and str2
         * @author
         */
        private int getDistance(String str1, String str2) {
            int distance;
            if (str1.length() != str2.length()) {
                distance = -1;
            } else {
                distance = 0;
                for (int i = 0; i < str1.length(); i++) {
                    if (str1.charAt(i) != str2.charAt(i)) {
                        distance++;
                    }
                }
            }
            return distance;
        }
    }

}

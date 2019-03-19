package com.znv.fss.common.utils;

public class GolombRiceUtil {
    private int qMulti;
    private int golombRiceLen;
    private int golombRiceM;
    private java.util.Base64.Encoder base64encoder = java.util.Base64.getEncoder();
    private java.util.Base64.Decoder base64decoder = java.util.Base64.getDecoder();

    public GolombRiceUtil(int qk, int golombRiceLength) {
        qMulti = 1 << qk;
        golombRiceLen = golombRiceLength;
        golombRiceM = 1 << golombRiceLength;
    }

    public String[] golombRiceEncode(float[] features) {
        int featuresLength = features.length; // 源特征向量个数
        String[] golombFeatures = new String[featuresLength]; // 量化、编码后特征向量
        for(int i = 0; i < featuresLength; i++) {
            // Q-k量化
            float qFeature = features[i] * qMulti;

            // Golomb-Rice编码
            int signedFeature = Math.round(qFeature);

            // 有符号 转 无符号
            int unsignedFeature;
            if(signedFeature <= 0) {
                unsignedFeature = Math.abs(signedFeature) << 1;
            }
            else {
                unsignedFeature = (signedFeature << 1) - 1;
            }

            // 求商、求余
            int q = unsignedFeature >> golombRiceLen;
            int r = unsignedFeature & (golombRiceM - 1);

            // 商，一元编码
            StringBuilder strFeature = new StringBuilder();
            for(int j = 0; j < q; j++) {
                strFeature.append(1);
            }
            strFeature.append(0);
            int offset = strFeature.length();

            // 余数，二进制
            int rBin[] = new int[golombRiceLen];
            for(int k = 0, rBinLength = rBin.length, rIndex; k < rBinLength; k++) {
                rIndex = rBinLength - 1 - k;
                if (r != 0) {
                    rBin[rIndex] = r & 1;
                    r = r >> 1;
                }
                else {
                    rBin[rIndex] = 0;
                }
                strFeature.insert(offset, rBin[rIndex]);
            }

            // System.out.println("有符号：" + signedFeature + " -- 无符号：" +unsignedFeature+ " -- Golomb-Rice编码：" + strFeature);
            golombFeatures[i] = strFeature.toString();
        }
        return golombFeatures;
    }

    public byte[] golombRIceDecode(String[] golombFeatures) {
        int golombFeaturesLength = golombFeatures.length; // 量化、编码特征向量个数
        byte[] arr = new byte[golombFeaturesLength << 2];
        byte[] qFeatures;
        for(int i = 0; i < golombFeaturesLength; i++) {
            String strFeature = golombFeatures[i];
            int strFeatureLength = strFeature.length();

            // 商解码
            int qReverse = strFeatureLength - golombRiceLen;

            // 余数解码
            int multiParam = 1;
            int multilevel = 0;
            int rReverse = 0;
            for(int j = strFeatureLength - 1; j >= qReverse ; j--) {
                rReverse += ((strFeature.charAt(j) - '0') << multilevel);
                multiParam = multiParam << 1;
                multilevel++;
            }

            int unsignedFeature = ((qReverse - 1) << golombRiceLen) + rReverse;

            // 无符号 转 有符号
            int signedFeature = 0;
            if((unsignedFeature & 1) == 1) {
                signedFeature = (unsignedFeature >> 1) + 1;
            }
            else {
                if((unsignedFeature & 1) == 0 && unsignedFeature != 0) {
                    signedFeature = -(unsignedFeature >> 1);
                }
            }

            // System.out.println("Golomb-Rice编码：" + strFeature + " -- 量化值：" + signedFeature);
            arr[(i << 2) + 3] = (byte) ((signedFeature >> 24) & 0xff);
            arr[(i << 2) + 2] = (byte) ((signedFeature >> 16) & 0xff);
            arr[(i << 2) + 1] = (byte) ((signedFeature >> 8) & 0xff);
            arr[i << 2] = (byte) (signedFeature & 0xff);
        }
        qFeatures = base64encoder.encode(arr);
        return qFeatures;
    }
}

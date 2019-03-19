package org.elasticsearch.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.util.BytesRef;

import java.util.ArrayList;
import java.util.List;

/**
 * FeatureCompUtil
 */
public class FeatureCompUtil {
    private static Logger log = LogManager.getLogger(FeatureCompUtil.class);

    /**
     * As an example
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        FeatureCompUtil fc = new FeatureCompUtil();

        // similarity should be 0.00466856;
        String f1 = "VU4AAAAAAAAMAgAALr/vPRVuwTuECL07hZ5tPPBTeb1mdnw9DTwDvgZjSr0PzhQ+uNXCvSYrFL09PF09pokrvKqnzb1uXY89hWO0PafOR70A93c92wfDPQlgzb2SClg8QExePuIA8b3SBhc+mLJfPRT4vbwRvRQ9ryqQPVqW1L0dLFM+SL6zPSjm+TxHNBY6jw1kvjZuvTwtaL28sZVIvcpQ6jxXJiI9qVy3vSVgdTvFiRO9RHEWvnX94jqjcI+9+hjYPRmDlL38lRs9M2vtPUzx4727nB89R19MPfbSsb3WwWi9nh7avJfI9D0LBn6+H5eMPTaqAz7mHbU9QKhaPQO8HD6Tmki+tXsEPdCfRr1nOo299gfnvb3TaT1HNlc+tnprvQO/cb1qwhS+Us/yPYEPCjwxnXY8/1+5PUbkrT3fCCC+rrVIPAds7LwbvG48VOCsPbfqAz6zJKu9udbkPdpwCr2zfEA95hhsvfudVDsRfgM+N3NAPf5wWbyqcCa867w1vVIO6Ty7nLA90f2ZvZaCHj2I66k83Xd0vaZLGb0Thse6vvKyvOrf0jwgzgE9XJZJPfbj87w9Bhg9iewrPmtqHD28bqY8Z3xpPRcfZzusy7q8K7GfPZ8o8jvs39+9fNUtPfM0cL0aj9k92J/vvWAd6T0YWUe+WIQKvktStLxiAAS8OiEJPCBS2Tw=";
        String f2 = "VU4AAAAAAAAMAgAAd6r4vWKkUzzcaXO9zoECPc5Ghr2ca2e+UKkivm/1vrwdyoG9RW0JPhuUxb0nIIc8WZOkPGGP6ryJgog8zI1AvR1gHz1E7ak9rx+TPWLfLr4RXNi9eGpYPR+0zb3dgrE8CFb3PcHGsb0i+yi8xw+mveyK1TsUSpM7oAR9vbpsybzxZWW98jwyvUuW3T392Rm++Cv3PR3H0z3Sf408Nd6/vJtFyj00aRw+TbkWPhvEJr78wg+8CHkaPXOQEz4q5XQ9JL6dPWV+mr2pSoy9cyRhPBBWGr0FiQ4+sRykPTKyzj1Z/ms+8CnPvTHQwr25oMO9ETz9vHPDZj6MohC+b2m4vGMppT3R1p29wY3lPYIE3b0/T5y9Q8SGvE72U70GP0s9QVHQPATkDD59koE7WuUWvuZFBTwBavE9zJc3vXUrKb4t+JU9Yeo/PSHPjj3Fhy69A1SEvWz4ED6/UZ08Y4ddPNK24bzBVaS9yTj6PTkikr0ExO09rH9EPEBGqT3E4+A8txmDvdk/K70m6cS9dwavPHZtRbzFh8k8phOSvfA2sD2kNze8gH6WvfCNMr1FOtM6hH6KuymG7D2kueM8jVLEveD10D2+xzW9xQQjPccpmr358Mo4LleQvSyeXbyta4I7uHmau3Ygn72cqRu+VYMsvnIDvT0PBO08Ab2rPVFolD0=";

        // similarity should be 0.341673;
        // String f1 =
        // "7qpXQtViAACAAAAAPdyRPaXIGr5Zkv49GO9xPiidST39ruI9gW8UvXJnar7wdwo+cQtEPN6US7351A48vzPTvCMkZL3yAZW9ZDS9PR9Udj1SHgS96pWnvOpv5rvm1sC9/0oWPV4YEzvqvsg9kIOdvLLWP73VJ9Q8CR0dO2dfsr01UZ08tV6QvDj3mb1ij7g9rOQxPVbfOb4wcns8Mn5yPhhhxD0h/rs9zfL1vb8g7z0IzwC+OC2RvZnN7L0/YXU9DSqxPdBNCT16DpK96LbhOq3ztDuzyJ089asBvJBYSL3lGK09GCUFvqiXpD1hy5+9gExGvQMalz0WyBU9bGbdPdmJrz26csG9dfqgPcPelb0kyMW9NSKhvQpn7TzSWzA871N4vcuxfD1wnL29dgZmPRbBGT1zLg0+uZ7QvSPyB77dd5A9pKsSPQGmmb1h+mG9j+y1uvVeAT4Qh4g6jg1ivoF2Uj1AEbm9fKdavWoQb7wdLwA97sHvPW2SNL1JG+w8VKeWvScQZL2eoeG8kbzXvNtWn71GLVU9Iiq8vXHfuz1YL7k9yU4KPsPEBT5KQYq9rR8HvfEosz2wnbC8cG2+vUDk1j3Wyp699DLlvVd4lz2Tfoc8WzQePshexzy8/t+9aMimPLq31b3X2Ko8R+hgumhs/z1g04E95uZ6vhehGz0Go8k85fxxPUKLOz0=";
        // String f2 =
        // "7qpXQtViAACAAAAAxStDvrwopT1PxDk+xL5cOXL0Qr1uej29lKbpPXe7C70/h9W89i+nOqn1Ez3/ODi96+oUvXZNu72td/s8pOeZveYpljxo2lM8otIOPUzi8T3B94q7xvMLu5Ga7D2ZDT4+vFqJvZmTxDy0Db68d7UdvbG2Xz3CM4Y80NJ/PRMsNz3HKYi9rELBPANjDL3tHg89dYIqvWp5yL2zMUS9l5L0PNiFszzr9N08AAXDPRyiCL6BqxE9vxibPGIbg73nWtE8u24EvSldm710ude8tT9yPfTzZjyI9om9UJrtPfoDIb0zRv29+D+WvTTkLz6Ovxk9zOWAvUejiDy2qdg8Tz2zPS/xyT2GoGQ8DL+hPGbOqr2HZXm7qsf+va65473llzG9MByNvYnv+D09WjC9TgROPbnofTxrsEw+4GITPJ771j3iIKg8fJGlvWrUczyDeMe8TWvQvXWfA7yC9sq9VoblPGPFhT17oti8i7U1PRr8AD2uGgu+RxEOvlJNkL6D5xu+TdGGvZlVcj5LY1q9mKzGPFD+gz3Ohio+HXHbuy+ntjzSusQ9UOBaPiiOWz0Z5TW+p0/9vJLDRT57/ta9DGYIvbFf2rs1PAo+vhEEPg9+872BmsW9I11tPf7AFD3SRJu7M7RhPHmz9DvKnps9AJ0Fvhlc/L0rDr89iLKZPApLwb0=";

        float sim = 0.0f;

        // compare base64
        System.out.println("compare base64");
        sim = fc.Comp(f1, f2);
        System.out.println(String.format("sim=%f", sim));

        long beginTime = 0;
        long endTime = 0;

        int loop = 10000000;
        // compare binary
        java.util.Base64.Decoder base64decoder = java.util.Base64.getDecoder();
        byte[] bin1 = base64decoder.decode(f1);
        byte[] bin2 = base64decoder.decode(f2);

        System.out.println("compare binary");
        beginTime = System.currentTimeMillis();
        for (int i = 0; i < loop; i++) {
            sim = fc.Comp(bin1, bin2);
        }
        endTime = System.currentTimeMillis();
        System.out.println(String.format("sim=%f, time=%dms", sim, endTime - beginTime));

        // compare raw
        byte[] raw1 = base64decoder.decode(f1.substring(16));
        byte[] raw2 = base64decoder.decode(f2.substring(16));
        int len = raw1.length / 4;
        System.out.println("raw length:" + len);
        int offset = 0;
        int fraw1[] = new int[len];
        int fraw2[] = new int[len];
        for (int i = 0; i < len; i++) {
            fraw1[i] = fc.GetInt(raw1, offset);
            fraw2[i] = fc.GetInt(raw2, offset);
            offset += 4;
        }

        System.out.println("compare raw");
        beginTime = System.currentTimeMillis();
        for (int i = 0; i < loop; i++) {
            sim = fc.CompRawFeature(raw1, raw2);
        }
        endTime = System.currentTimeMillis();
        System.out.println(String.format("sim=%f, time=%dms", sim, endTime - beginTime));
    }

    /**
     * Compare two feature (support 253 model)
     *
     * @param f1 base64 encode feature
     * @param f2 base64 encode feature
     * @return similarity
     * @throws Exception
     */
    public float Comp(String f1, String f2) {

        if (f1.length() != f2.length()) {
            // throw new Exception("feature size unequal");
            log.debug("feature size unequal");
            return 0f;
        }

        byte[] bin1 = base64decoder.decode(f1);
        byte[] bin2 = base64decoder.decode(f2);

        return Normalize(Dot(bin1, bin2, 12));
    }

    /**
     * Compare two binary feature with 12 bytes head (support 253 model)
     *
     * @param f1 binary feature with 12 bytes head
     * @param f2 binary feature with 12 bytes head
     * @return similarity
     * @throws Exception
     */
    public float Comp(byte[] f1, byte[] f2) {
        int m1 = GetInt(f1, 0);
        int m2 = GetInt(f2, 0);
        int v1 = GetInt(f1, 4);
        int v2 = GetInt(f2, 4);
        int dim1 = GetInt(f1, 8);
        int dim2 = GetInt(f2, 8);

        if (v1 != v2) {
            // throw new Exception("version unmatch");
            log.debug("version unmatch");
            return 0f;
        }

        if (0x4257aaee != m1) {
            dim1 = (dim1 - 12) / 4;
        }

        if (0x4257aaee != m2) {
            dim2 = (dim2 - 12) / 4;
        }

        if (dim1 != dim2) {
            // throw new Exception("feature dimension unmatch");
            log.debug("feature dimension unmatch");
            return 0f;
        }

        // System.out.printf("m1=0x%x, m2=0x%x, v1=%d, v2=%d, dim1=%d, dim2=%d\n", m1, m2, v1, v2, dim1, dim2);

        return Normalize(Dot(f1, f2, 12));
    }

    /**
     * Compare two raw feature with no head (support 253 model)
     *
     * @param f1 binary raw feature with no head
     * @param f2 binary raw feature with no head
     * @return similarity
     * @throws Exception
     */
    public float CompRawFeature(byte[] f1, byte[] f2) {
        return Normalize(Dot(f1, f2, 0));
    }

    /**
     * Compare two raw feature with no head (support 253 model)
     *
     * @param f1 float raw feature with no head
     * @param f2 float raw feature with no head
     * @return similarity
     * @throws Exception
     */
    public float CompRawFeature(float[] f1, float[] f2) {
        return Normalize(Dot(f1, f2, 0));
    }

    /**
     * Compare two binary feature
     *
     * @param f1     binary feature
     * @param f2     binary feature
     * @param offset
     * @return similarity
     * @throws Exception
     */
    public float Comp(byte[] f1, byte[] f2, int offset) {
        return Normalize(Dot(f1, f2, offset));
    }

    /**
     * Compare two binary feature
     *
     * @param f1     float feature
     * @param f2     float feature
     * @param offset
     * @return similarity
     * @throws Exception
     */
    public float Comp(float[] f1, float[] f2, int offset) {
        return Normalize(Dot(f1, f2, offset));
    }

    public float Dot(float[] f1, float[] f2, int offset) {

        if (f1.length != f2.length) {
            // throw new Exception("feature length unmatch");
            log.debug("feature length unmatch");
            return 0f;
        }

        if (f1.length < offset) {
            // throw new Exception("feature length is too short");
            log.debug("feature length is too short");
            return 0f;
        }

        int dimCnt = (f1.length - offset);

        float dist = 0.0f;
        for (int i = offset; i < dimCnt; i++) {
            dist += f1[i] * f2[i];
        }

        return dist;
    }

    public float Dot(byte[] f1, byte[] f2, int offset) {

        if (f1.length != f2.length) {
            // throw new Exception("feature length unmatch");
            log.debug("feature length unmatch");
            return 0f;
        }

        if (0 != (f1.length - offset) % 4) {
            // throw new Exception("feature dimension is incompeleted");
            log.debug("feature dimension is incompeleted");
            return 0f;
        }

        if (f1.length < offset) {
            // throw new Exception("feature length is too short");
            log.debug("feature length is too short");
            return 0f;
        }

        int dimCnt = (f1.length - offset) / 4;

        float dist = 0.0f;
        for (int i = 0; i < dimCnt; i++) {
            dist += Float.intBitsToFloat(GetInt(f1, offset)) * Float.intBitsToFloat(GetInt(f2, offset));
            offset += 4;
        }

        return dist;
    }

    public float cosDistance(double[] f1, double[] f2, int offset) {

        if (f1.length != f2.length) {
            // throw new Exception("feature length unmatch");
            log.debug("feature length unmatch");
            return 0f;
        }

        if (f1.length < offset) {
            // throw new Exception("feature length is too short");
            log.debug("feature length is too short");
            return 0f;
        }

        int dimCnt = (f1.length - offset);

        float dist = 0.0f;
        float d1 = 0.0f;
        float d2 = 0.0f;
        for (int i = 0; i < dimCnt; i++) {
            dist += f1[i] * f2[i];
            d1 += f1[i] * f1[i];
            d2 += f2[i] * f2[i];
        }
        float d = (float) Math.sqrt(d1 * d2);

        return dist / d;
    }

    public float cosDistance(float[] f1, float[] f2, int offset) {

        if (f1.length != f2.length) {
            // throw new Exception("feature length unmatch");
            log.debug("feature length unmatch");
            return 0f;
        }

        if (f1.length < offset) {
            // throw new Exception("feature length is too short");
            log.debug("feature length is too short");
            return 0f;
        }

        int dimCnt = (f1.length - offset);

        float dist = 0.0f;
        float d1 = 0.0f;
        float d2 = 0.0f;
        for (int i = 0; i < dimCnt; i++) {
            dist += f1[i] * f2[i];
            d1 += f1[i] * f1[i];
            d2 += f2[i] * f2[i];
        }
        float d = (float) Math.sqrt(d1 * d2);

        return dist / d;
    }

    public float Dot(double[] f1, double[] f2, int offset) {

        if (f1.length != f2.length) {
            // throw new Exception("feature length unmatch");
            log.debug("feature length unmatch");
            return 0f;
        }

        if (f1.length < offset) {
            // throw new Exception("feature length is too short");
            log.debug("feature length is too short");
            return 0f;
        }

        int dimCnt = (f1.length - offset);

        float dist = 0.0f;
        for (int i = offset; i < dimCnt; i++) {
            dist += f1[i] * f2[i];
        }

        return dist;
    }

    public float Dot(List<Double> f1, List<Double> f2) {

        if (f1.size() != f2.size()) {
            // throw new Exception("feature length unmatch");
            log.debug("feature length unmatch");
            return 0f;
        }

        int dimCnt = f1.size();

        float dist = 0.0f;
        for (int i = 0; i < dimCnt; i++) {
            dist += f1.get(i) * f2.get(i);
        }

        return dist;
    }

    public float cosDistance(List<Double> f1, List<Double> f2) {

        if (f1.size() != f2.size()) {
            // throw new Exception("feature length unmatch");
            log.debug("feature length unmatch");
            return 0f;
        }

        int dimCnt = f1.size();

        float dist = 0.0f;
        float d1 = 0.0f;
        float d2 = 0.0f;
        for (int i = 0; i < dimCnt; i++) {
            dist += f1.get(i) * f2.get(i);
            d1 += f1.get(i) * f1.get(i);
            d2 += f2.get(i) * f2.get(i);
        }
        float d = (float) Math.sqrt(d1 * d2);

        return dist / d;
    }

    public float DotAsDouble(List<Double> f1, BytesRef feature, int offset) {
        int f2rawlength = feature.length - offset;// 12 bytes head (support 253 model)
        if (0 != f2rawlength % 4) {
            // throw new Exception("feature dimension is incompeleted");
            log.debug("feature dimension is incompeleted");
            return 0f;
        }

        int dimCnt = f2rawlength / 4;
        // logger.info("f1 length = "+ f1.size() + ", f2 length= "+feature.length+ ", dimCnt="+dimCnt);//hy test
        if (f1.size() != dimCnt) {
            log.debug("feature length unmatch");
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

    public float Dot(List<Float> f1, BytesRef feature, int offset) {
        int f2rawlength = feature.length - offset;// 12 bytes head (support 253 model)
        if (0 != f2rawlength % 4) {
            // throw new Exception("feature dimension is incompeleted");
            log.debug("feature dimension is incompeleted");
            return 0f;
        }

        int dimCnt = f2rawlength / 4;
        // logger.info("f1 length = "+ f1.size() + ", f2 length= "+feature.length+ ", dimCnt="+dimCnt);//hy test
        if (f1.size() != dimCnt) {
            log.debug("feature length unmatch");
            return 0f;
        }

        float dist = 0.0f;
        byte[] f2 = feature.bytes;
        for (int i = 0; i < dimCnt; i++) {
            dist += f1.get(i) * Float.intBitsToFloat(GetInt(f2, offset));
            offset += 4;
        }

        return dist;
    }

    public float cosDistance(List<Double> f1, BytesRef feature) {
        int offset = 0;
        int f2rawlength = feature.length;
        if (0 != f2rawlength % 4) {
            // throw new Exception("feature dimension is incompeleted");
            log.debug("feature dimension is incompeleted");
            return 0f;
        }

        int dimCnt = f2rawlength / 4;
        // logger.info("f1 length = "+ f1.size() + ", f2 length= "+feature.length+ ", dimCnt="+dimCnt);//hy test
        if (f1.size() != dimCnt) {
            log.debug("feature length unmatch");
            return 0f;
        }

        float dist = 0.0f;
        float d1 = 0.0f;
        float d2 = 0.0f;
        byte[] f2 = feature.bytes;
        for (int i = 0; i < dimCnt; i++) {
            float ftemp = Float.intBitsToFloat(GetInt(f2, offset));
            dist += f1.get(i).floatValue() * ftemp;
            d1 += f1.get(i) * f1.get(i);
            d2 += ftemp * ftemp;
            offset += 4;
        }
        float d = (float) Math.sqrt(d1 * d2);

        return dist / d;
    }

    public int GetInt(byte[] bytes, int offset) {
        // return (0xff & bytes[offset]) | (0xff00 & (bytes[offset + 1] << 8)) | (0xff0000 & (bytes[offset + 2] << 16))
        // | (0xff000000 & (bytes[offset + 3] << 24));
        return (0xff & bytes[offset]) | ((0xff & bytes[offset + 1]) << 8) | ((0xff & bytes[offset + 2]) << 16)
                | ((0xff & bytes[offset + 3]) << 24);
    }

    /**
     * Convert feature bytes to float array with no head
     *
     * @param bytes binary feature with 12 bytes head
     * @return float feature array
     */
    public float[] getFloatArray(byte[] bytes) {
        int offset = 12;
        if (0 != (bytes.length - offset) % 4) {
            // throw new Exception("feature dimension is incompeleted");
            log.debug("feature dimension is incompeleted");
            return new float[1];
        }

        if (bytes.length < offset) {
            // throw new Exception("feature length is too short");
            log.debug("feature length is too short");
            return new float[1];
        }

        int len = (bytes.length - offset) / 4;
        float feature[] = new float[len];
        for (int i = 0; i < len; i++) {
            feature[i] = Float.intBitsToFloat(GetInt(bytes, offset));
            offset += 4;
        }
        return feature;
    }

    /**
     * Convert feature bytes to double array with no head
     *
     * @param bytes binary feature with 12 bytes head
     * @return double feature array
     */
    public double[] getDoubleArray(byte[] bytes, int offset) {
        if (0 != (bytes.length - offset) % 4) {
            // throw new Exception("feature dimension is incompeleted");
            log.debug("feature dimension is incompeleted");
            return new double[1];
        }

        if (bytes.length < offset) {
            // throw new Exception("feature length is too short");
            log.debug("feature length is too short");
            return new double[1];
        }

        int len = (bytes.length - offset) / 4;
        double feature[] = new double[len];
        for (int i = 0; i < len; i++) {
            feature[i] = Float.intBitsToFloat(GetInt(bytes, offset));
            offset += 4;
        }
        return feature;
    }

    public List<Double> getDoubleList(byte[] bytes, int offset) {
        if (0 != (bytes.length - offset) % 4) {
            // throw new Exception("feature dimension is incompeleted");
            log.debug("feature dimension is incompeleted");
            return new ArrayList<Double>(1);
        }

        if (bytes.length < offset) {
            // throw new Exception("feature length is too short");
            log.debug("feature length is too short");
            return new ArrayList<Double>(1);
        }

        int len = (bytes.length - offset) / 4;
        List<Double> feature = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            feature.add((double) Float.intBitsToFloat(GetInt(bytes, offset)));
            offset += 4;
        }
        return feature;
    }

    public List<Float> getFloatList(byte[] bytes, int offset) {
        if (0 != (bytes.length - offset) % 4) {
            // throw new Exception("feature dimension is incompeleted");
            log.debug("feature dimension is incompeleted");
            return new ArrayList<Float>(1);
        }

        if (bytes.length < offset) {
            // throw new Exception("feature length is too short");
            log.debug("feature length is too short");
            return new ArrayList<Float>(1);
        }

        int len = (bytes.length - offset) / 4;
        List<Float> feature = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            feature.add(Float.intBitsToFloat(GetInt(bytes, offset)));
            offset += 4;
        }
        return feature;
    }

    public float Normalize(float score) {
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

    public float reversalNormalize(float score) {

        if (score <= dst_points[0]) {
            return src_points[0];
        } else if (score >= dst_points[dst_points.length - 1]) {
            return src_points[src_points.length - 1];
        }

        float result = 0.0f;
        for (int i = 1; i < (dst_points.length); i++) {
            if (score < dst_points[i]) {
                result = src_points[i - 1] + (score - dst_points[i - 1]) * (src_points[i] - src_points[i - 1])
                        / (dst_points[i] - dst_points[i - 1]);
                break;
            }
        }
        return result;
    }

    private java.util.Base64.Decoder base64decoder = java.util.Base64.getDecoder();
//    private float[] src_points = {0.0f, 0.128612995148f, 0.236073002219f, 0.316282004118f, 0.382878988981f,
//            0.441266000271f, 0.490464001894f, 1.0f};
//    private float[] dst_points = {0.0f, 0.40000000596f, 0.5f, 0.600000023842f, 0.699999988079f, 0.800000011921f,
//            0.899999976158f, 1.0f};

    //    int version = 24201;
    private float[] src_points = {-1.0f, 0.4f, 0.42f, 0.44f, 0.48f, 0.53f, 0.58f, 1.0f};
    private float[] dst_points = {0.0f, 0.4f, 0.5f, 0.6f, 0.7f, 0.85f, 0.95f, 1.0f,};

}

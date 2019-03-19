package org.elasticsearch.hashing;

import Jama.Matrix;
import org.apache.logging.log4j.LogManager;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

/**
 * Created by Administrator on 2017/10/27.
 */
public class PCADimReduction {
    private static org.apache.logging.log4j.Logger logger = LogManager.getLogger(PCADimReduction.class.getName());
    private static int dimensionBefore = 256;
    private static int dimensionAfter = 16;
    private static Matrix matrix = null;
    private static Matrix mean = null;

    /**
     * Reads a file from disk and sets the matrix and mean.
     *
     * @return
     * @throws IOException
     * @see PCADimReduction#readPCAMeanAndMatrix(InputStream inMean, InputStream inMatrix)
     */
    public static void readPCAMeanAndMatrix(InputStream inMean, InputStream inMatrix) throws IOException {
        //read mean
        ObjectInputStream ois1 = new ObjectInputStream(new GZIPInputStream(inMean));
        dimensionBefore = ois1.readInt();
        int meanM = ois1.readInt();
        assert meanM == 1;
        double[] meanArray = new double[dimensionBefore];
        for (int i = 0; i < dimensionBefore; i++) {
            meanArray[i] = ois1.readFloat();
        }
        ois1.close();
        inMean.close();
        mean = new Matrix(meanArray, meanM);
        //read matrix
        ObjectInputStream ois2 = new ObjectInputStream(new GZIPInputStream(inMatrix));
        dimensionAfter = ois2.readInt();
        int matrixM = ois2.readInt();
        assert matrixM == dimensionBefore;
        double[][] matrixArray = new double[dimensionBefore][dimensionAfter];
        for (int i = 0; i < dimensionBefore; i++) {
            double[] matrixper = matrixArray[i];
            for (int j = 0; j < dimensionAfter; j++) {
                matrixper[j] = ois2.readFloat();
            }
        }
        ois2.close();
        inMatrix.close();
        matrix = new Matrix(matrixArray);
    }

    /**
     * Generates the low dimensions by the given matrix and normalize.
     *
     * @param featureHigh
     * @return
     */
    public static double[] generateLowDimensions(double[] featureHigh) {
        if (featureHigh.length != mean.getColumnDimension()) {
            logger.debug("The feature length is error !! length:" + featureHigh.length);
            return null;
        }

        Matrix featureM = new Matrix(featureHigh, 1); // 1xdimensionBefore
        Matrix featureS = featureM.minus(mean); // 查询的特征值减去平均值
        Matrix featureLow = featureS.times(matrix);
        // 归一化
        double[] f = featureLow.getColumnPackedCopy();
        double d1 = 0.0f;
        for (int i = 0; i < f.length; i++) {
            d1 += f[i] * f[i];
        }
        d1 = Math.sqrt(d1);
        for (int i = 0; i < f.length; i++) {
            f[i] /= d1;
        }
        return f;
    }

    public static byte[] double2Bytes(double[] d) {
        byte[] byteRet = new byte[4 * d.length];
        int offset = 0;
        for (int n = 0; n < d.length; n++) {
            float valf = (float) d[n];
            int vali = Float.floatToRawIntBits(valf);

            for (int i = 0; i < 4; i++) {
                byteRet[offset++] = (byte) ((vali >> 8 * i) & 0xff);
            }
        }
        return byteRet;
    }


}

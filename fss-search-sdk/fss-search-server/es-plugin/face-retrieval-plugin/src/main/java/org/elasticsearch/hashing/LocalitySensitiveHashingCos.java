/*
 * This file is part of the LIRE project: http://lire-project.net
 * LIRE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * LIRE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LIRE; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * We kindly ask you to refer the any or one of the following publications in
 * any publication mentioning or employing Lire:
 *
 * Lux Mathias, Savvas A. Chatzichristofis. Lire: Lucene Image Retrieval –
 * An Extensible Java CBIR Library. In proceedings of the 16th ACM International
 * Conference on Multimedia, pp. 1085-1088, Vancouver, Canada, 2008
 * URL: http://doi.acm.org/10.1145/1459359.1459577
 *
 * Lux Mathias. Content Based Image Retrieval with LIRE. In proceedings of the
 * 19th ACM International Conference on Multimedia, pp. 735-738, Scottsdale,
 * Arizona, USA, 2011
 * URL: http://dl.acm.org/citation.cfm?id=2072432
 *
 * Mathias Lux, Oge Marques. Visual Information Retrieval using Java and LIRE
 * Morgan & Claypool, 2013
 * URL: http://www.morganclaypool.com/doi/abs/10.2200/S00468ED1V01Y201301ICR025
 *
 * Copyright statement:
 * ====================
 * (c) 2002-2013 by Mathias Lux (mathias@juggle.at)
 *  http://www.semanticmetadata.net/lire, http://www.lire-project.net
 *
 * Updated: 02.06.13 10:27
 */

package org.elasticsearch.hashing;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.BitSet;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * <p>Each feature vector v with dimension d gets k hashes from a hash bundle h(v) = (h^1(v), h^2(v), ..., h^k(v)) with
 * h^i(v) = (a^i*v + b^i)/w (rounded down), with a^i from R^d and b^i in [0,w) <br/>
 * If m of the k hashes match, then we assume that the feature vectors belong to similar images. Note that m*k has to be bigger than d!<br/>
 * If a^i is drawn from a normal (Gaussian) distribution LSH approximates L2. </p>
 * <p/>
 * Note that this is just to be used with bounded (normalized) descriptors.
 *
 * @author Mathias Lux, mathias@juggle.at
 *         Created: 04.06.12, 13:42
 */
public class LocalitySensitiveHashingCos {
    private static Logger logger = LogManager.getLogger(LocalitySensitiveHashingCos.class.getName());
    private static String name = "lshHashFunctionsCos_64.obj";
    private static int dimensions = 32; // max d
    private static int numFunctionBundles = 16; // k
    private static  double binLength = 10; // w

    private static double[][] hashA = null;      // a
    private static double[] hashB = null;        // b
    private static double dilation = 1d;         // defines how "stretched out" the hash values are.

    /**
     * Writes a new file to disk to be read for hashing with LSH.
     *
     * @throws java.io.IOException
     */
    public static void generateHashFunctions() throws IOException {
        File hashFile = new File(name);
        if (!hashFile.exists()) {
            try (
                    ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(hashFile)));
            ) {
                oos.writeInt(dimensions);
                oos.writeInt(numFunctionBundles);
                for (int c = 0; c < numFunctionBundles; c++) {
                    for (int j = 0; j < dimensions; j++) {
                        float rand = 1.0f;
                        if (drawNumber() < 0.0f) {
                            rand = -1.0f;
                        }
                        oos.writeFloat(rand);
                    }
                }
                oos.close();
            }
        } else {
            logger.error("Hashes could not be written: " + name + " already exists");
        }
    }

    public static void generateHashFunctions(String name) throws IOException {
        File hashFile = new File(name);
        if (!hashFile.exists()) {
            try (ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(hashFile)));
            ) {
                oos.writeInt(dimensions);
                oos.writeInt(numFunctionBundles);
                for (int c = 0; c < numFunctionBundles; c++) {
                    for (int j = 0; j < dimensions; j++) {
                        float rand = 1.0f;
                        if (drawNumber() < 0.0f) {
                            rand = -1.0f;
                        }
                        oos.writeFloat(rand);
                    }
                }
                oos.close();
            }
        } else {
            logger.error("Hashes could not be written: " + name + " already exists");
        }
    }

    /**
     * Reads a file from disk and sets the hash functions.
     *
     * @return
     * @throws IOException
     * @see LocalitySensitiveHashingCos#generateHashFunctions()
     */
    public static double[][] readHashFunctions() throws IOException {
        return readHashFunctions(new FileInputStream(name));
    }

    public static double[][] readHashFunctions(InputStream in) throws IOException {
        ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(in));
        dimensions = ois.readInt();
        numFunctionBundles = ois.readInt();
        double[][] hashFunctions = new double[numFunctionBundles][dimensions];
        for (int i = 0; i < hashFunctions.length; i++) {
            double[] functionBundle = hashFunctions[i];
            for (int j = 0; j < functionBundle.length; j++) {
                functionBundle[j] = ois.readFloat();
            }
        }
        ois.close();
        in.close();
        LocalitySensitiveHashingCos.hashA = hashFunctions;
        return hashFunctions;
    }

    /**
     * Generates the hashes from the given hash bundles.
     *
     * @param features
     * @return
     */
    public static int[] generateHashes(double[] features) {
        double product;
        int[] result = new int[numFunctionBundles];
        for (int k = 0; k < numFunctionBundles; k++) {
            product = 0;
            for (int i = 0; i < features.length; i++) {
                product += features[i] * hashA[k][i];
            }
            result[k] = product > 0 ? 1 : 0;
        }
        return result;
    }

    public static byte[] generateHashesAsBytes(double[] features) {
        double product;
        byte[] result = new byte[numFunctionBundles];
        for (int k = 0; k < numFunctionBundles; k++) {
            product = 0;
            for (int i = 0; i < features.length; i++) {
                product += features[i] * hashA[k][i];
            }
            result[k] = (byte) (product > 0 ? 1 : 0);
        }
        return result;
    }

    public static long generateHashesAsLong(double[] features) {
        BitSet bs = new BitSet(Long.SIZE);
        assert numFunctionBundles == Long.SIZE;
        double product;
        for (int k = 0; k < numFunctionBundles; k++) {
            product = 0;
            for (int i = 0; i < features.length; i++) {
                product += features[i] * hashA[k][i];
            }
            if (product > 0)
                bs.set(k);
        }
        return bs.toLongArray()[0];
    }

    public static short generateHashesAsShort(double[] features) {
        BitSet bs = new BitSet(Long.SIZE);
        assert numFunctionBundles == Long.SIZE;
        double product;
        for (int k = 0; k < numFunctionBundles; k++) {
            product = 0;
            for (int i = 0; i < features.length; i++) {
                product += features[i] * hashA[k][i];
            }
            if (product > 0)
                bs.set(k);
        }
        return (short) bs.toLongArray()[0];
    }

    public static String generateHashesAsString(double[] features) {
        double product;
        StringBuilder result = new StringBuilder(numFunctionBundles);
        for (int k = 0; k < numFunctionBundles; k++) {
            product = 0;
            for (int i = 0; i < features.length; i++) {
                product += features[i] * hashA[k][i];
            }
            result.append((product > 0 ? 1 : 0));
        }
        return result.toString();
    }

    /**
     * Returns a random number distributed with standard normal distribution based on the Box-Muller method.
     *
     * @return
     */
    private static double drawNumber() {
        double u, v, s;
        do {
            u = Math.random() * 2 - 1;
            v = Math.random() * 2 - 1;
            s = u * u + v * v;
        } while (s == 0 || s >= 1);
        return u * Math.sqrt(-2d * Math.log(s) / s);
        // return Math.sqrt(-2d * Math.log(Math.random())) * Math.cos(2d * Math.PI * Math.random());
    }

    private static void generateHashFunctionsFromExistFunctions(String name) throws IOException {
        String LSH_HASH_FILE = "/hash/lshHashFunctionsCos_64.obj";
        LocalitySensitiveHashingCos
                .readHashFunctions((LocalitySensitiveHashingCos.class.getResourceAsStream(LSH_HASH_FILE)));
        double[][] hashFunctions = new double[numFunctionBundles * 2][dimensions];
        int startp = 3;
        int stopp = startp + dimensions / 2;
        for (int k = 0; k < numFunctionBundles; k++) {
            System.arraycopy(hashA[k], 0, hashFunctions[k], 0, dimensions);
            System.arraycopy(hashA[k], 0, hashFunctions[k + numFunctionBundles], 0, dimensions);
            double[] functionBundle = hashFunctions[k + numFunctionBundles];
            for (int i = startp; i < stopp; i++) {
                functionBundle[i] = -functionBundle[i];
            }
        }

        for (int k = 0; k < numFunctionBundles * 2 - 1; k++) {
            for (int j = k + 1; j < numFunctionBundles * 2; j++) {
                //判断是否有重复
                if (Arrays.equals(hashFunctions[k], hashFunctions[j])) {
                    System.out.println(String.format("function %d and %d is same. ", k, j));
                }
                //判断是否正交基
                if (!isOrthogonal(hashFunctions[k], hashFunctions[j])) {
                    System.out.println(String.format("function %d and %d is not orthogonal. ", k, j));
                }
            }

        }

    }

    private static boolean isOrthogonal(double[] function1, double[] function2) {
        double innerProduct = 0;
        for (int k = 0; k < dimensions; k++) {
            innerProduct += function1[k] * function2[k];
        }
        if (innerProduct == 0) {
            return true;
        }
        return false;
    }

    public static int getNumFunctionBundles() {
        return numFunctionBundles;
    }

    public static void main(String[] args) {
        try {
            // generateHashFunctions();
            String LSH_HASH_FILE = "/hash/lshHashFunctionsCos_16x16.obj";
            LocalitySensitiveHashingCos
                    .readHashFunctions((LocalitySensitiveHashingCos.class.getResourceAsStream(LSH_HASH_FILE)));
            for (int k = 0; k < numFunctionBundles - 1; k++) {
                for (int j = k + 1; j < numFunctionBundles; j++) {
                    //判断是否有重复
                    if (Arrays.equals(hashA[k], hashA[j])) {
                        System.out.println(String.format("function %d and %d is same. ", k, j));
                    }
                    //判断是否正交基
                    if (!isOrthogonal(hashA[k], hashA[j])) {
                        System.out.println(String.format("function %d and %d is not orthogonal. ", k, j));
                    }
                }

            }
            System.out.println(hashA.length);
            //         generateHashFunctionsFromExistFunctions(LSH_HASH_FILE);
        } catch (IOException e) {
            logger.error(e);
        }
    }

}

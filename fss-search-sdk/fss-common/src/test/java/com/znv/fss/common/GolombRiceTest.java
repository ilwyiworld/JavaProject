package com.znv.fss.common;

import com.znv.fss.common.utils.GolombRiceUtil;

import java.io.*;
import java.util.Scanner;

public class GolombRiceTest {
    private static final int qk = 7;    // 量化位数
    private static final int golombRiceLen = 3; // Golomb-Rice编码位数
    private static final String filePath = "H://feature//"; // 文件路径 //home//xz// H://feature//
    private static final String readFileName = "pca_lee_500000.txt"; // 读取文件名 pca_mix_200w_yc_history_property.txt pca_lee_500000.txt feature_yinchuan_500000.txt
    private java.util.Base64.Decoder base64decoder = java.util.Base64.getDecoder();

    private void testEncodeAndDecodeSpeed() throws IOException {
        // String writeFileName = "pca_lee_500000_res.txt"; // 写入文件名

        // 读取文件
        FileInputStream fileInputStream = new FileInputStream(filePath + readFileName);
        BufferedReader bufferedReader;
        bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream, "UTF-8"));

        // 写入文件
        // File file = new File(filePath + writeFileName);
        // file.createNewFile();
        // BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));

        // String strTitle = "-- Golomb-Rice 编码、解码过程（ Q"+ qk + " 量化，m = " + golombRiceLen + "） --\n";
        // bufferedWriter.write(strTitle);
        // System.out.print(strTitle);

        // int i = 1;
        long encodeSum = 0;
        long decodeSum = 0;

        String strLineText;
        GolombRiceUtil golombRiceUtil = new GolombRiceUtil(qk, golombRiceLen);
        while ((strLineText = bufferedReader.readLine()) != null) {
            String[] sFeatures = strLineText.split(" ");

            int sFeaturesLength = sFeatures.length;
            float[] arr = new float[sFeaturesLength];
            for(int i = 0; i < sFeaturesLength; i++) {
                float f = Float.parseFloat(sFeatures[i]);
                int fi = Float.floatToIntBits(f);
                arr[i] = fi;
            }

            // 编码
            long startTime = System.currentTimeMillis();
            String[] golombFeatures = golombRiceUtil.golombRiceEncode(arr);
            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            encodeSum += totalTime;

            // 解码
            startTime = System.currentTimeMillis();
            byte[] qFeatures = golombRiceUtil.golombRIceDecode(golombFeatures);
            endTime = System.currentTimeMillis();
            totalTime = endTime - startTime;
            decodeSum += totalTime;

            // System.out.println(new String(arr1));

            // for(int j = 0; j < qFeatures.length; j++) {
            //     String strContent = "第" + i + "条特征向量 - 第" + (j + 1) + "个特征值：" + sFeatures[j] + "，编码值：" + gFeatures[j] + "，解码值：" + qFeatures[j] + "\n";
            //     bufferedWriter.write(strContent);
            //     System.out.print(strContent);
            // }

            // i++;
        }
        System.out.println("编码总用时：" + (encodeSum / (float)1000)  + "s");
        System.out.println("解码总用时：" + (decodeSum / (float)1000) + "s");
        // bufferedWriter.flush();
        bufferedReader.close();
        // bufferedWriter.close();
    }

    private float[] base64ToFloat(String fs0) {
        byte[] feature = base64decoder.decode(fs0);
        float[] floatFeatures = new float[256];
        for(int i = 12, loop = 12 + (256 << 2), j = 0; i < loop; i += 4, j++) {
            int l;
            l = feature[i];
            l &= 0xff;
            l |= ((long) feature[i + 1] << 8);
            l &= 0xffff;
            l |= ((long) feature[i + 2] << 16);
            l &= 0xffffff;
            l |= ((long) feature[i + 3] << 24);
            float temp = Float.intBitsToFloat(l);
            floatFeatures[j] = temp;
        }
        return floatFeatures;
    }

    private float matrixDot(float[] f0, String[] features) {
        float sum = 0.0f;
        int loop = f0.length;
        for(int i = 0; i < loop; i++) {
            sum += f0[i] * Float.parseFloat(features[i]);
        }
        return sum;
    }

    private float matrixDot(float[] f0, int[] features) {
        float sum = 0.0f;
        int loop = f0.length;
        for(int i = 0; i < loop; i++) {
            sum += f0[i] * features[i];
        }
        return sum;
    }

    private float matrixDot(float[] f0, float[] features) {
        float sum = 0.0f;
        int loop = f0.length;
        for(int i = 0; i < loop; i++) {
            sum += f0[i] * features[i];
        }
        return sum;
    }

    private byte[] golombRiceEncodeAndDecode(String[] features, GolombRiceUtil golombRiceUtil) {
        int sFeaturesLength = features.length;
        float[] arr = new float[sFeaturesLength];
        for(int i = 0; i < sFeaturesLength; i++) {
            float f = Float.parseFloat(features[i]);
            int fi = Float.floatToIntBits(f);
            arr[i] = fi;
        }

        String[] golombFeatures = golombRiceUtil.golombRiceEncode(arr);
        return golombRiceUtil.golombRIceDecode(golombFeatures);
    }

    private float qDot(float[] f0, String[] f, GolombRiceUtil golombRiceUtil) {
        // ArrayList<Float> fq = golombRiceEncodeAndDecode(f, golombRiceUtil);
        byte[] fqBytes = golombRiceEncodeAndDecode(f, golombRiceUtil);
        float[] fq = base64ToFloat(new String(fqBytes));
        float d1 = matrixDot(f0, f);
        float d1_q = matrixDot(f0, fq) / 128;
        return d1_q - d1;
    }

    private void testSimilarityError() throws IOException {
        String[] fs = {
                "7qpXQsFdAAAAAQAAtS8fvRkaV7zwvhG9/i0uPBvNgb0S2aU9hd2vPVyERr77+4O9yqDQvXKGBz7w+nK931vHvLlbib3OGTc8v6A/Pd/KeL0iL1U7oOw1PPtU67zuSj09C/oyPi3EkT03tyA9nO0uPt4zD71saZQ9EoGEPI6ntT0TtZ48vdcUPhUmCzw7pva85YVwPQSgQryP76S9pyi5vc0NWb1lmNW8uaoZPaOlFr1es889scWQPY0wBb2y4z+8Bc/ovUcw8bxvqi09rpMSOgspgTxNWYC9qEe4vS16aD1jP9k99G2uPOKKk7zvB8q9uy6PPeMX8byZQBE92faMPagi5ryaSe48lrCZPEqL4js31Ak9oMAHvSMr7Lxm3w881HVMPX5gsL07jTg95Nr3PI6HjT02ySe91GAvPRzYFb3Prqg96fOOPBzpMj6h+sm9sFOvvb8KVju1TEa9eEWQvFaKg72UKSo9Yx6XvZjqBzx/wxU9g9wHvYbPlD0BdPE8WWeoOxc1bLxhqhK83uhKu/fDQT2CAhm9YEuyPOtY7z3uXL27YFcEPea0jzwZZIQ8KZ8LPZUhAzwZbas9sbEUPls2pjsr6pW84hTMPRoge72V35S9hXM9PW4AVb38VwG+XbHFPIV+ljtViFq96TvrPFhKr704PIw7zWLAPct2hbsANaI9FKiqPWpAWT3ts8S6+wghvCjYN7345qO9qtogPectCj3lcX+9xuPMvO7qvzzSWBa8aDaLPMoflj2wA3e9Ph6UPV/EIb1qSRS9mAcQvRf4aD3fxUO94kL1PMY4xr0ILLo8ZOoOu66xN73cShM8pJOsPflNPL0AzCq9D9ctveeprL3obUc97h22vG0Qnbn2dKU9KsmIvG2wNr0SapA9PmspvcRfaDxL/Ii9QTsSPAaNZr1P48E7oCnsPSfVvb3h/lw96lrwPdp0fz167yE9ySNnPWzwiz37PdQ9kTIBvLx3vz3hYDY+5bxfPPSMjbwte9m7QiqAPIlt5b3eqns9OYM9vbSKuDzIdYe8Uza5PBl+Jz02RiS97aVAvTttjz1hqXU98RezPMFzv73F0LQ9fJ6cPZksZbyIiSS9H0L+vLqkgb3NBl88N7+2Pcujhb3nHKs9F0QevvoGkDznraU9GdKqPAXvqzw1wbg8O2YaOzu6ND1twFy9K+kWvW5XObzk3yg9Dnyzu9q7trt24uw8eayqPXXtCz1Jgik93sygPR3aCz0mLQW9/LdjvLQ5Kb0/ZR09yuEBviq/m7wpccy8miGTvYx6NTpcqms8OIQAPtsRzr10B6g8YiNVvTKi9L2IIj69akWDPZiTb73W/le9tJxQvfHykb3EMYm9KseAPVFZ7rnu6I4903eBvQ==",
                "7qpXQsFdAAAAAQAAyichvMDdor36pa48FT+JPRfdq7qG5Nq9l0NkPEAQlz1sute90L2AvaecMz0s6u69VfWBPRtXWj1WsQE9rwR8PKCgVT3aY1w9GTbwvRS6IL3Vm8K80PU7vMeoZb23w0s7gaQ2PUNP9zzUVBE947GAu8vlM7267IG8T9ztu/weND2zgAC7lGIzPWPTJr7Hqns9UyEUvbfyA76/v9Y8B7Zyve8pAz1oPhw+WXaivaQwyz18gZm8f9spO+DeIL3BAGM624SivODRq7xUgLK90wuGPToTIjzVXQc9Bd7SvImzwT1wdsi81g5mvJe4Kju64gY93m37PLMUPz3WaJ487AYMvu2wS7ynStG94n+Rvd/LDbvM84a9Fpuku7//N70J+ic9biTEO3FH6T3AXoW9QCiMPGUoMDsx2mE9UWHAO4rAk7xZC629pAGhvRmMiT2fmtM94BYcPbzR0713nbQ8T2xAPtRWHj05K9i80BmsPQhsVT3MOR+6IybrvfZ3n7xsHYg8T9zYuw4bDz0o3Iq9G5L0PAfkCD5IXT0+VfO1vdb43L2MyJA8KG21vCf0ez3fmpK9FqzoOWBoZ71t5KW6E1T4u0jxyD25vXm9CAAWvI/Fpb1x13A9U/iyu6zi8jos09S91sEyvb0lOLz1/7I9VGaEPUjzkD1FAbw80JJYPDDjPb3YvjW8G53+PJ92zLszhso9tTnrO6bdET4/9Rw8zcGrvcmR971EeUS9vxqXPY71BD3l1G+83vJ5vR5m171IQa297o1TvQ4ShT1uT+C9/SauvJ9zP710YY08AHJ4Pdq9SrrKPg0+enlyPK3a47xWkA29ICY7usS33LxH8Kg9x/oXvUlXBz4C8iE7p0t+PMN9m73uqCG7tduGO7GtRj1vzDq9LYMKPUzeAD0NFxw9H8Q3PFRbbrycJzE8NJWoPV9fhT0U7qc82dS8vOosqD1AEWO7FlWePKlX2b1ebha9Ja9YPWh2Fb3GJ4c9jVW4vKsPGr4dCLE8tdnyveROCD286A+8hsnEve+0cr3o8Go8ujrrOntUcb2hYAi8OjKXPQcAaT1OJgY8mIIVvWdDe729jJ09NsGgvYKhib2MIQg7Mb6MvGIomj3FEb07MLWsvT/3pz1rXhO9qd2oPXdcUbxdrfK85ZMMPljFmLpzxRE8Ky9FPVRsk72zyaK9wQDZvQaNFTxXzZI9OOsUveMmQb1UdIa9O/dvvK7o4Lwi+oo5ZEQEvhz+kr3dUfI8J1yJvMVu3DzctP07FOeUPTfekD0G8209tYs6PYGSsj0lZP28vHmuvTtoAz0xowE9/YA7vdRnID0wyKo8Ou5VPVJHdbzhiys9xWxAPb6yWr3vMsc6y9yYPQ==",
                "7qpXQsFdAAAAAQAA7scpPKFWhb0c+SE+HvzGvHQdLz0mQ4o8wHa7vcMvg725ksu9MZYzvpX8YjzKRlI9jsTXuXJhgL3YtZI8GSMXvi8Up73cH5g9yDfXvTWZ0zzC/vg8iD8CvVAgMb6MP8k8e05tPDzvhz0Ld/28NsyAvdfoyj1QfxG9LXm9vScX+jyClpk6Dreduu1Oiz0jZQ29QMADvUu5eruLaac6bd8RPeM8qD1PhSE9IQNjPYfCTrt84kI8kQypvcJfkb1+w849HejNvTs2E73zUHi9DqrpvLppmr02Pjo98cRQPffRBr6YJsc7JAFWveZxtDvQMbu7k+BsPoMCPT2mcgA9ozlovN/B4zwOTNS9OEfxvBDECb2o8Mq8qRghPT2w5D0GBYO8ImUkvaJZjj1Z3V09HZSkvS60u727wAo+m9BdvROb3DywY5K9zF8svewJfbxU0hO97HCOvA9Nlr3aqgu8k+2nvKHds7xgRJ+8kbYCvdEMdz2OlNY9XHRpvdZHWr0Stqo900p0PdET2DwnSDy9NVCKPXhzCj3FvAE9memzPaoApT1utfa8DW2QvMRovjrMdEU9TOZwu098ez3vgNa9pvtVvWHpLb0C+/Y8IyohPaE3pjzjykG8cTDuPHFpYz2fk408UuDKvCAhID3GMuW8450ju1XVX7yiOuC7qdxcvE6BcbwB0iC8KCNavUfvJ7y84hU+qRQrOVb75LyZfJs86siVvVJiw7y7jvM9i0DFPTk3iz3q2Y29jF/DvGU5yD2vn8k8E9uUPbmDZj0tSuM8cgH9PIPmsTt8gPW7GeAQvZumNb2Mqiu9wKgzvUU/1z3LD4e9/1GGvDjvkDtvU689XtyIvAOpLzw+/pI864eMOztvubw/J768w4kFvKct6r2wd7O97aaJvUwblrw0eD+8yrR2PT3OBb68ZuC8ZEeevJIKmD2P7Dy8bBYwvWCclbxF6D4921q1vN8O9ju3mIY8nW7pPW29yL2rUhs9KUFJPdVUmL3kE8M8wJ+vvXrxgD1E/Iy7lkROvOIrZL1ec5a9RWnmPYJabT0NBjC9wN0xvEdVxj0ZhCg9jVxqPYhdCDzROos9oV90veypLr1Y2Um93SM4PVwCtbs4bcK9ZZKevZDV4jkb18W8M7mTvQRZU73Ej3M94l2NvTQ7hb1yYeI8GhkUPXw1qrt0y6e9MHDZPZEadD1dIOO99uZ1veC/vL34qy69Qb6KvT4kMb1lvju9qArhu2zGrb0F8k28gQlevZS5pz1gP+m9w6sJPXvkjr16NTU9L5qfvc5h+T0It9A9axm3PE3+47wDo8S7wzspvfxotL3iGCS9Nz/ZPQeUIT1fMn88//AevZrrvr1o6ru8HAw/vA==",
                "7qpXQsFdAAAAAQAAZUBjPQO4Dz07fOQ99gKHPeKIxD2Hakq9wEjcPfH3fz3uQrM8VDhUPJ4jVb3pKQi++VPPPP5u5Dy/V1g9TLz9vb+s6r3px7k7d1y6vcdBB76L8lE9riWnPd6Aqb0Gn4Q9Y3IoPJ1qXr2Ml2K914zVvTtpfD2itb+9pMK5vd8DhzwEUJU8YkhIPW+6pb2iFdI8++YCvQzGu7zju6O8k/iDPWzDlT1h95A9G6PVutbE/zu8NgY8/AYavXX0FbxuKIa9HuarvQmBOLzd6Bc9S8KpvT8Ajz013UO9RaxGvY1WfT3RrXw71rglvcMgCD4utdy9DGegva2G1zyfe7s97B4+PAvAyzsJ6029kyTZPVUcNj0bKwg+ABcdvR2Ugj0ITtA9rrtIvf6Jwriepvi8yeDAPZiH4zsJGZC9ID+OvUTDUD1wRJS9dNlHPSQmXb07/IY9mwo6vbBr4zzXRQ88eIOqvWgefby6uP+8UgWSvfNKDj3224u9/8ecvbENILyLMha9M5khvVYgeDzIK9G9Vg0KPSRp5Ds+z8Q9izkKvnUIBr0aFqE9a8Tlu+XHdr0Dz4Y9jIqKPQcu3Txm67U9M0PlvKQVDLoGn/q8p0NBOhmt0ruYCKO9irdVvVQPCT3vObo7gBkePXFwHbulayq94UDVPGlxu7wBNKq9vzs+vdcinT0JYdA97ahxOyc9yDyBOyq+BRqJvYOlyjzaCcu8YQH6vLfDwj3Spsu94ROKPeXILL0IFag8ZAkkvTqTWLwQ01E98rCNPLe5qT2cySo65FrRvC5wQjwAPpQ8vGHpu/bUA725RwK9eS6ivdeq+72eSs88JdyJPIA5O718++S7wPU8PdcjQj0yN5M9VfkzPbxv7LyroE09R8wSu6OBxjxKjxO9lG2vvWIAHz1xEqW8KsxOvQvOybnNNbs9WOggPqv6rDtjpGG93nbCvUaqDzzsBZq9GE5jPFbqmLsYk6o8byixvOf4db24/tU8220KPaDsorxZjxE9CHW6O01G7D3p2Sy9QWc5PFfJ4bzVAoE9bUgUPWZUdDyqIDq92+1bPERsKr3UJ8Q81bkwvMGuCD3czte8zitGPfFA8L1mcpk8OqrzvUlbS73iMIc8M+SIvV8xnzwbv7S9QpPdPU23WblAUGi90ntgvK9UKL2LzYm9emcbvRpYRr3eEIO9Rs9TPDJtvL3GMpE8ahJEPUGOFj15xrI96futPRRfTT1QwME9E7ekvQWbAL2FjAO7Utt9PK9i5r3QkZY9tjcSvtazEL5v6Ag9lf00PRxehr3xW8q8e8m9vSOCqz16Oyo96pFbvN+tDz5ZCY28szLNPO1rTjydnrE8dH6mvdHAQzxxFAA9yHApPg==",
                "7qpXQsFdAAAAAQAAhPnnvWWwVj16TKm8oTSAvZUfhL3YXOQ9BA1BvMIiAD0KLhG9IwNAvkiBDr3PEYY9UdxhvU3G2b3xIgY7AtgCvvLLcb18P0m97MslvSUsfL1+Bai8dILvvI64Dj0q/5o9DiqGvRILH75N7408UjoJPLhRj72kJby9UcW0vV27uD3teB8+VyAzPbQDvLytyca9MYcBvO/ODj3Qh766vHuYvDGdmr1Hqac7HfD5vNvbrj361w6915IgPXt867yYxE+90D3Gvb9FUjxVsRs8RpdBvBr14DwaHfU9+J6/vT4fyLyJSMe7aCFOvajZhDt+lB89hQR9vSf3yr2JppM9XNcOvsq2iDx1hEW9lcuFvVpqUjxb+ne9HOKLPMcR3jrYRwA6hAOdu02hJD0hc4O8lcGhPf4Hmr32JQG9dUjcPO3j2r2cp9M8yjVJvc3+RLt5VH89GR5CPXaKTb2+Mpi9hfmcO8KjRj3H0F+9sthBPLxnAz1Dpww8aQ4qPd2KiL0BpJw9ukSUvXwvcT2IMR698IM0PecNjT2O4Cq8oHmqPWvagL2euR68SD4vvOS5ij3nSl+8Y2KdvS6lB7xNtSo9c7zZvNBkJr5I+xo9yp/lPb1TSD1snle90VSAvfy5cr3S72y99wbbPZj7xj3llSq9e+tAPbgFxj3v5ok9KeX1vcI+gDyTvLC9FlWCPdkaqjwqrma98fdOvkRoiD0st4s90ZizPUvIgj2lfIE9BCD6u18ADz1mcf88QX8uvBmWZDxklL49dzhHvMrj1LwT0EM86YptvPptPjzcN4o8DWJDu1jMrryrzQe+UiphvdvkDr2Sc968W64/vZNtB71W8li9OS+SvIZbgD2Oyqs9ctazPVLT1LwtNoM8m6lWPPsoL730RjA8+pJgvXqpgL1IgoE9mryRvcnZUb0I+8Q9v2uEPd3V2TxXRZS6ifd/vPWm2L0BBUc9JP8Evc0DLDv+vgy9qCf9PNmT9D2xbC29Zt+tPRc2KL0QFAm+u7ARPN5nIj0HRou5jjjJPKRjsb2dq288/251vVgxcL3MsZs9c06PPbd4JrqrTVo9+dyWPPctnr0dKPq8kSWTPGHRbL2fZ/O89NOFPQtJFb3FrAo8FfkHvrx/tLySLQK98zazvZj2TzwPv6O9uEcCvr5DFb3x5Hy96xWfPX1h7D13+J68gqHOu8iXA7wUBa+83sYkPE0Jrr2Pook9AF2evb7Ejj1mpim9aaRKPaQlLz1b/Oy8PqpnvUhBGr096ca8TYjHvQOAiL29/3y9KDGmPMnoEjxWnsS8mDuBvJBOt70H6nI8jZptvOwzez3yFdk8/7vIPOFJ1r0TaJi8QUdzPX55mb1A7bk72yKOvQ==",
                "7qpXQsFdAAAAAQAAfiyyPQby3LxZcHE9T7cHPJ8mET08fo49TU5bvaY26Lw/MvY7TphDvUI7AL1/egy+W59KPrDq6r1Zk4I9IoqlPMF8771zQxs94rCrvag9WL0/wj89pCXPPasgE7tlwDa80Ks9vdH+z7wvApA9Y/dbPW2UID6zEr+9XBNsvVnzsz1VNAY96TmFPZj1u70YUSK+IKPgPOHM3r3crQi+6unJvD9/Qj23p4G81I6FvJVx17tM1ow9NuxxvdhlCj0wg3m9KeQfPL1zAb5h6tU8JK8gvXL3D75DZI+9dRPCPUrjTbyiv6+7dhSvPYBZ47ykd6C9BaqZvNa0Vr1z33s7MxacvUp9LL1uSCe9TEn/PWYIJL3+C4S8cP2rvY6KcL1ACS09LFOxvcoJTDwO2BM8ldaHvJ8SXz0bCZ894bzWvCekBL2Mr0U83JXLPZt3cL2Hh8A7smTbPGTFG73SJs49FMlgPa/HYzz9OZ48UCmXPd9rlzyqD3C6LVY2PdqHLD1bWYE92x3RvZArCDz1uGW90tWLPXo4yru4i209+UeivTGElj3Zoj299783upkYKT4R2Ei85pOcO0FERb1Imyq97t4JvgDFir3XdJW9MKUWPvEuFj09zFe9MYGmPEC7pj01G1+96xqDPEYR5bnKbUG8IA+MPHpZ87vxUkM9NebTPVHo2LxhFva8c3efPd4qHL5pYdG70aM9PSvN4zy/AoM9OIV3vBRuebn0T9S6aUGsvCV0UT2krdA9powOO2T1GD2GMnS9wXeIvfg1nbtO9pW9wkXUu9mzwr3Nriy9rxDKvfJ52Lw+RtE97s2bPPrSNz17Avk7tsqKvazJ4TwrDgm9cM4EuwcOSjypb6O7PqS6vYoTdz2pCNW82c6uPc9Lrb2Q6pG9aRSbvGAar7zZYDu9685GPXugXT35aS29uQerPBZAKj1A9Ri9oMz3PNF8Fz0Qexg8ZPPPPGV1vDznfLC7eP94PexoJL6bQDU95TXZvVMXnz1/bzu9YyKtPEGD+rxLi6S8L5NFPE75Iz3u3Xg85BfkOo+kTzsV06a9PJGAPd84Sr0eoKA9pUaGPQ7H0725vwe923iEuy+5Lj718Z68M7VYPYHvhb2lZ5c9ZFAfu0O5H71tAyO9YJawPdzSKT2QMBK9gyk5vVnUlruT0R+9ofsQvRFmX71TSjm8Zz/3vO2WkzwgbKq9qEZXvUYIgjtlaMG9LX9fPKzWlDnEWus9unaQvOROoz0Xqgu9ossJPHRgu7wlJ6A9HXRmugz1XT2xvTe9pl5+O9J0kL31fbe7NtzQvXrF8DxZdSi8a1k5PZ53GL2hv6U6I8V8PQ53/LzMM7i9F9AXvHkjlL17boW9OsouPQ==",
                "7qpXQsFdAAAAAQAANOpHPS7HEr0NoLe9ghsAvjBZ2D0SLLQ99E9gPF8DmDx5CSS9EkIUvvfBrL0cUjy96Z97PQDmBb2cJCm9g0HNvaloIr1Upxq9dPAJvmnOtLsPBhs9jMAgPSNogr2zowc92PatPHo/gb3mK3S9StKPvQjlu7zeoGC9gak8PMtmRTlR6s28h5kyPXb2Bb7qAeG80G/iPfMyPTxfSGY7SEinPb8iFr0Qpbw9anjLu4Xs2jwkP7Q9L2MEvDQBpz1dkBG+d16XPPdEoTyrT4O9zdK1PB0wj7tpFpQ9SwiYO07iozyoLys9jc8IvIkZcjz/la29fpIgvlBclT1QOII9aOE6PTMJf71pvyg9Xe9wPZii5Ly12Jw97vxLPUivir22/4i9gQP5PLTQaTwBLYe9BwZIvcWwcTza/Aw+AtiQve4ivryDHFi9snPOvE1qsb37p9G8kYKevAtSpLxAkSe8QZxRPoOI3zxILvA8431RPb8zbrxwyZQ9kXDoPJXoZr3O1t88WhlRvTmRDb2Hc1G7HN5Mu9zK+Dxf8sA98tljveEZgb1xTAm+4Yj/O9xl2z2poZc96/ANvRzuQr35vy+8KNErvT8QuTyvbAm9AZwjPUXv5rz24bc8Rrq9vSoUwroRsI69fd9yvHsM2jvm+bE8JW8kPR+sSr3Yl4K99wglu555qT2X8Rc9ozWFPRLvuDtL6fa7+IShvcfPhLwxIZ69iFjsuyuqkL1fUt48J+l7PfkGg73TOjC9xL1jvR9DBz3kb9y9ISl1veAlTT1eo168cXrhPWtJBb1zghW7BKcPPbRKRj1FSU88PlzyPHMyID0y3Bm9zJmYvYFMpb366bQ9XSPVvPTNmz0Fwc87dMDNvCGWCD0pPp69Sh2VPaIdQTxlGou9i5B/PV4PiDx9obm87EgwvchmDb4Se4y9R/PGPbuqvr1YObM9VA+7vUMiqLxCDDC9ITDRvCKrhr0hhzM99XhIPfp/yDtW8eA8esyDvFKKUT0TUc67OWyOPZb82L3A0gW8ciZavI5iIz2t5FM9QAZgPVaZ1j3XhZi9pJyNuTa8qD22gyi83gfKPaAgjL2aKAM9vHzEPeutjL06tPG8m2mWPCeHDD1DJTm95WQ5PSHOlD04J4S7xIK/PdywEjxNRhK9w1XMPEwnTL1kRXK9vbeavOn4ML7F8pK7riVkPVZDnz0tuP48CB+DvJmQk728Oom9bfTzvddXgL3XZIY95+QFvpsFfrs8aGe9shPiPa2y8r35nK89uLYavd4mgLwKU5a8UDwMvOE7mT3CpmC9449DvSxRiL1wSM87pW4AvUYOebzEqrQ9YhMIvvyJFD0Q5uI8S8XFvTPqib11HEK99crcPA==",
                "7qpXQsFdAAAAAQAAVABOvQIp572enQ0+k3bvvbWFRbw8f6w8Huz2vW8j7bxR5Hm82+TqPFMGebzosiG9HUzAvZ7+czwoHJ69aT2RvOcOcb2KLsY9HDpOvcVOSD2zo648tp1zva4aaLy9wRA+sETZPG9hqb1c5ly9ENalPRBnMr0nluC9UWHfvRSgPbzxPms9BvYgvPnQfj1arb69Na8HvvB1BT2S9kE9UXmvvPk1tr0s6XW92a9avX/FZT2lywY9zlgKvZxTG72swyO+BPKlPch4Gz2coZq9k9yAPUr5M70FoKG8isQDuxJ1Yryzo3q9u6nivJuYML0+ppA8kgITvtWpej1ri7o7NJuUPMBKDTy9tNi9UJZ/Pe/ppr2uoPi877PSvdx5oDs1hmY9WdMnvPvkkjy6XC+9eBpIPIr8hboMHbe8uu/iuzRDyT0o1JY9zPGEvTaRMr1IUGg9BSjdvIFaBL12rhA+AeqIPVnmMr13IHk934hkPY4GU7ysD309quzVPYqEc70b99q6rtMGvBBQKjvRTBc+69odPVBs8byCnTM8DSPFvY30pD2jVUY8LyfbvE9/4r1JWAW9A/2ivYXxi724hEG984hjuZQGmDzFxkI9FhCgPWn4BL7TItS8uw66PNirNzydkLa9ZNo+vTAMG7vLhqk9GJysO+csqDze+Fc8QCg6PqSVpL0eGpU9oFjiPF8UiDwlAXq9QFURPTtJyTyQd8A94dh4PFYSkrsxUbU7VCFoPbNdQDwCEhu9F9lIPVaYtT0QGR28QV2dPCKvLT1Bt469ho4UviNlAD6L5kg9XUERvl5uA7wnAKE9hfiSvLp82DsnrsY6q8SPPTfrTj1G86E9WmVlPb1mOT06CFC9bODhPObdtz1M7dq8ngw/PQyuFr0GSoY9lqrYPVC3uTyX9Ry8DNqHPXGnKjtAkfG8BV09vX3C6bpjPIk9m603Pfa2Nbxogzi9L78RvWhfWTxr+gQ9Sb+gPVkEEb6rjIy9TAPGPBESpTzfhxq91RLcvB0rpb1nefu8OQaEvK/fxj1bvVu8a5XUPUr+kL3CBga9aFbDPSGzp7x2ajw+erP3u7L7nb3ssLe9jtfFPD8YGbw2w+e8831aPOI+ML0ne5a8fYUEPasomL3H2LE7B5iCPVYgmj2KR1Y9zbUDvL7DTr1rROy85iwbvNT5XL0irIk9A+9MvTx7Uzw/SXC9xWA5PcmPpruHPKS9Q5wLvWtHpz1SnyY9JDwtO+sUfbx38EE9fWgKPXVpBzwj3/O8ZVq5vLHh9j20eaG8o5bOvOtoPr0pzSA9Ayr+PORj7D1aJ109z/KQPTo0qrtsxQy9iDrnvBZNsr1LRs+8j+B9PV82Tj3BGFq9/7vLvA==",
                "7qpXQsFdAAAAAQAABbJcPcOd1DyTSJU8BYt0vJH+2bpVGFG9HiOvOrYvOD2mh+k5fnQqvnrQqr3fkUI8+4CnPKcSQb14Rik9HcqUPa8QUb1kbFO9/SMZvRGZeL0gZk0942P6vVV5ejzlMhw+cGDCu1GLoLysqHq8aac9vBj6wbxBv7K9FFVuPZ7Alj30ulC97aTOvE/uYD3LxZ29j4WDPSIYK73WRaY9Kh11vE3s1TyYb5I9wpRKvBgZ9T13Zs89Ca+VPe/DlT39rYa8ncKxvZ9mQTuIb+m8oSGJvMDtIz3g62O98wFuvWzJYj1Zi4m93RmjucWtHLw6DKu819n/vPVeA740MpY9tJuSvR4nr72O8Bq9xgdoPYBjiz2nKJi8hTsvPdigdD1Xoma7pMHtux1jir2ypDK9pitePYyXeb0uFxG9UfdrvZJgkjxtQ4W9OcmMPeO3ib0VIBI+TfJhvXs8Pz3CUgk+NPWQPfS7qj2dee+9nmQzu08StD2Of449ucu0vRK03725Ple7dpg4Pdw7dz2OU4G8Gc5nPeCGJ72d3Ja8sjRLvHiObjzvfYs8SSJGPYjqS701kNW9464IvVJ1VD2m88I93BoqvWuzmzoPQu48Sth3Oyc+hb21+fG8KAIRvTFkJT2DRfs8ZndwPT2M7L13J9g87yRgva2unTpl6k68PIkfPXzOG7wesTu9rZWKPSbWBD5vgNM7NZCyPBcZkLzBAWI9vzHRPKwc0L2FMjC7Kc03Ox0bMz1vv2496pKMPcPPwTxB4So9ehU5vU9gsD1/Ug+9tEjuvMsxgD1aktU77bTQvZAJDj2z04i7R+D9vSL7n73pKVq9mPmHPdP/Pz1IhYk9BmF0uxmbCj1eu8A9SYxEO792Lr5sI8C7/Pq/PL9mDz2MDpo9JD1DvTOJmz2DdiI9lk6lPSU0kr0dHrK81G6CvUX7kr0WxRa8sXK+vMJGaz0Py3a71vHwvds9ujwZn+Q8vhXevagd77yp0kq9NXSWvTKgi7yctH49W1XhvRJn5jv4+0g9qF/ePC+55rzjNl08vsB1vJYCCT70MbA9pJ8LPgK8ZL3bdNG8XgbIvBCijr2vffO7dCAePT509DzR0p49e+aYPcl9ZD1Lu6Y878qjPQQcg72Dqh09mjrDvS8Lu72KXHw7dS5/vV/Q3T1oQYG9M/R+PUKizD2juj+9Ku2bvZgf0z0P8cS9hfSAPb90urzwNXm9MHCfPY63g7wxtRc9h8HDvP5mAD2p4UG9MK03PXwb0T2mBo+89MmfOWh3s7kF4ru9NpvEvb1qLb3r+0G9MlVsPHC9JT2JMLm8rPy1vYoVYD1PuDo+UmETvKA1e7184Qy9Cj36POeRrT1Tvzi9QwsJvQ=="};

        float[] f0 = base64ToFloat(fs[0]);

        // 读取文件
        FileInputStream fileInputStream = new FileInputStream(filePath + readFileName);
        BufferedReader bufferedReader;
        bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream, "UTF-8"));

        // 写入文件
        String writeFileName = "pca_lee_500000_error.txt"; // 写入文件名
        File file = new File(filePath + writeFileName);
        file.createNewFile();
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));

        String strLineText;
        long q_time = 0;
        while ((strLineText = bufferedReader.readLine()) != null) {
            String[] sFeatures = strLineText.split(" ");
            // ArrayList<String> featureList = new ArrayList<>(Arrays.asList(sFeatures));
            GolombRiceUtil golombRiceUtil = new GolombRiceUtil(qk, golombRiceLen);
            long startTime = System.currentTimeMillis();
            float d = qDot(f0, sFeatures, golombRiceUtil);
            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            bufferedWriter.write(Float.toString(d));
            bufferedWriter.newLine();
            q_time += totalTime;
        }
        System.out.println("循环总用时：" + (q_time / (float)1000) + "s");
        bufferedWriter.flush();
        bufferedWriter.close();
        bufferedReader.close();
    }

    private void testFeatureError() {
        // similarity should be 0.00466856;
        String s1 = "7qpXQsFdAAAAAQAAtS8fvRkaV7zwvhG9/i0uPBvNgb0S2aU9hd2vPVyERr77+4O9yqDQvXKGBz7w+nK931vHvLlbib3OGTc8v6A/Pd/KeL0iL1U7oOw1PPtU67zuSj09C/oyPi3EkT03tyA9nO0uPt4zD71saZQ9EoGEPI6ntT0TtZ48vdcUPhUmCzw7pva85YVwPQSgQryP76S9pyi5vc0NWb1lmNW8uaoZPaOlFr1es889scWQPY0wBb2y4z+8Bc/ovUcw8bxvqi09rpMSOgspgTxNWYC9qEe4vS16aD1jP9k99G2uPOKKk7zvB8q9uy6PPeMX8byZQBE92faMPagi5ryaSe48lrCZPEqL4js31Ak9oMAHvSMr7Lxm3w881HVMPX5gsL07jTg95Nr3PI6HjT02ySe91GAvPRzYFb3Prqg96fOOPBzpMj6h+sm9sFOvvb8KVju1TEa9eEWQvFaKg72UKSo9Yx6XvZjqBzx/wxU9g9wHvYbPlD0BdPE8WWeoOxc1bLxhqhK83uhKu/fDQT2CAhm9YEuyPOtY7z3uXL27YFcEPea0jzwZZIQ8KZ8LPZUhAzwZbas9sbEUPls2pjsr6pW84hTMPRoge72V35S9hXM9PW4AVb38VwG+XbHFPIV+ljtViFq96TvrPFhKr704PIw7zWLAPct2hbsANaI9FKiqPWpAWT3ts8S6+wghvCjYN7345qO9qtogPectCj3lcX+9xuPMvO7qvzzSWBa8aDaLPMoflj2wA3e9Ph6UPV/EIb1qSRS9mAcQvRf4aD3fxUO94kL1PMY4xr0ILLo8ZOoOu66xN73cShM8pJOsPflNPL0AzCq9D9ctveeprL3obUc97h22vG0Qnbn2dKU9KsmIvG2wNr0SapA9PmspvcRfaDxL/Ii9QTsSPAaNZr1P48E7oCnsPSfVvb3h/lw96lrwPdp0fz167yE9ySNnPWzwiz37PdQ9kTIBvLx3vz3hYDY+5bxfPPSMjbwte9m7QiqAPIlt5b3eqns9OYM9vbSKuDzIdYe8Uza5PBl+Jz02RiS97aVAvTttjz1hqXU98RezPMFzv73F0LQ9fJ6cPZksZbyIiSS9H0L+vLqkgb3NBl88N7+2Pcujhb3nHKs9F0QevvoGkDznraU9GdKqPAXvqzw1wbg8O2YaOzu6ND1twFy9K+kWvW5XObzk3yg9Dnyzu9q7trt24uw8eayqPXXtCz1Jgik93sygPR3aCz0mLQW9/LdjvLQ5Kb0/ZR09yuEBviq/m7wpccy8miGTvYx6NTpcqms8OIQAPtsRzr10B6g8YiNVvTKi9L2IIj69akWDPZiTb73W/le9tJxQvfHykb3EMYm9KseAPVFZ7rnu6I4903eBvQ==";
        String s2 = "7qpXQsFdAAAAAQAAyichvMDdor36pa48FT+JPRfdq7qG5Nq9l0NkPEAQlz1sute90L2AvaecMz0s6u69VfWBPRtXWj1WsQE9rwR8PKCgVT3aY1w9GTbwvRS6IL3Vm8K80PU7vMeoZb23w0s7gaQ2PUNP9zzUVBE947GAu8vlM7267IG8T9ztu/weND2zgAC7lGIzPWPTJr7Hqns9UyEUvbfyA76/v9Y8B7Zyve8pAz1oPhw+WXaivaQwyz18gZm8f9spO+DeIL3BAGM624SivODRq7xUgLK90wuGPToTIjzVXQc9Bd7SvImzwT1wdsi81g5mvJe4Kju64gY93m37PLMUPz3WaJ487AYMvu2wS7ynStG94n+Rvd/LDbvM84a9Fpuku7//N70J+ic9biTEO3FH6T3AXoW9QCiMPGUoMDsx2mE9UWHAO4rAk7xZC629pAGhvRmMiT2fmtM94BYcPbzR0713nbQ8T2xAPtRWHj05K9i80BmsPQhsVT3MOR+6IybrvfZ3n7xsHYg8T9zYuw4bDz0o3Iq9G5L0PAfkCD5IXT0+VfO1vdb43L2MyJA8KG21vCf0ez3fmpK9FqzoOWBoZ71t5KW6E1T4u0jxyD25vXm9CAAWvI/Fpb1x13A9U/iyu6zi8jos09S91sEyvb0lOLz1/7I9VGaEPUjzkD1FAbw80JJYPDDjPb3YvjW8G53+PJ92zLszhso9tTnrO6bdET4/9Rw8zcGrvcmR971EeUS9vxqXPY71BD3l1G+83vJ5vR5m171IQa297o1TvQ4ShT1uT+C9/SauvJ9zP710YY08AHJ4Pdq9SrrKPg0+enlyPK3a47xWkA29ICY7usS33LxH8Kg9x/oXvUlXBz4C8iE7p0t+PMN9m73uqCG7tduGO7GtRj1vzDq9LYMKPUzeAD0NFxw9H8Q3PFRbbrycJzE8NJWoPV9fhT0U7qc82dS8vOosqD1AEWO7FlWePKlX2b1ebha9Ja9YPWh2Fb3GJ4c9jVW4vKsPGr4dCLE8tdnyveROCD286A+8hsnEve+0cr3o8Go8ujrrOntUcb2hYAi8OjKXPQcAaT1OJgY8mIIVvWdDe729jJ09NsGgvYKhib2MIQg7Mb6MvGIomj3FEb07MLWsvT/3pz1rXhO9qd2oPXdcUbxdrfK85ZMMPljFmLpzxRE8Ky9FPVRsk72zyaK9wQDZvQaNFTxXzZI9OOsUveMmQb1UdIa9O/dvvK7o4Lwi+oo5ZEQEvhz+kr3dUfI8J1yJvMVu3DzctP07FOeUPTfekD0G8209tYs6PYGSsj0lZP28vHmuvTtoAz0xowE9/YA7vdRnID0wyKo8Ou5VPVJHdbzhiys9xWxAPb6yWr3vMsc6y9yYPQ==";

        float sim = 0.0f;

        int loop = 10000000;
        float[] f1  = base64ToFloat(s1);
        float[] f2  = base64ToFloat(s2);

        System.out.print("未编解码：");
        long beginTime = System.currentTimeMillis();
        for (int i = 0; i < loop; i++) {
            sim = matrixDot(f1, f2);
        }
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - beginTime;
        System.out.println(String.format("sim=%f, time=%dms", sim, totalTime));

        System.out.print("编解码：");
        GolombRiceUtil golombRiceUtil = new GolombRiceUtil(qk, golombRiceLen);
        String[] golombFeatures = golombRiceUtil.golombRiceEncode(f2);
        byte[] qFeatures = golombRiceUtil.golombRIceDecode(golombFeatures);
        float[] f2_q  = base64ToFloat(new String(qFeatures));
        beginTime = System.currentTimeMillis();
        for (int i = 0; i < loop; i++) {
            sim = matrixDot(f1, f2_q) / 128;
        }
        endTime = System.currentTimeMillis();
        totalTime = endTime - beginTime;
        System.out.println(String.format("sim=%f, time=%dms", sim, totalTime));
    }

    public static void main(String[] args) throws IOException {
        GolombRiceTest golombRiceTest = new GolombRiceTest();

        while(true) {
            System.out.println("1-测试编解码速度，2-测试相似度误差，3-特征值比对速度，请选择（-1退出）：");
            Scanner scanner = new Scanner(System.in, "UTF-8");
            int i = scanner.nextInt();

            switch(i) {
                case -1:
                    System.exit(0);
                    break;
                case 1:
                    golombRiceTest.testEncodeAndDecodeSpeed();
                    break;
                case 2:
                    golombRiceTest.testSimilarityError();
                    break;
                case 3:
                    golombRiceTest.testFeatureError();
                    break;
                default:
                    System.out.println("参数有误！");
                    break;
            }
        }
    }
}

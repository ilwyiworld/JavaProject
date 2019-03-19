package com.znv.fss.common.utils;


import com.znv.fss.common.utils.bitstream.BigEndianBitInputStream;
import com.znv.fss.common.utils.bitstream.BigEndianBitOutputStream;
import com.znv.fss.common.utils.bitstream.BitInputStream;
import com.znv.fss.common.utils.bitstream.BitOutputStream;
import org.jcodec.codecs.h264.H264Utils2;
import org.jcodec.common.io.BitWriter;
import org.jcodec.common.tools.MathUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class FeatureCompressByBs {
    private static final int m = 3;
    private static final int M = 2<<m;
    private static final int DIM = 256;
    private static final int QP = 128;
    private ByteArrayOutputStream outStream;
    private int[] readArray;

    public FeatureCompressByBs() {
        outStream = new ByteArrayOutputStream(DIM);
        this.readArray = new int[DIM];
    }

    public void writeUR(BitOutputStream out, int value) throws IOException {
        int q = value >>> m;
        int r = value & (M - 1);
        out.write(0, q);
        out.write(1, 1);
        out.write(r, m);
    }

    public void writeSR(BitOutputStream out, int value) throws IOException  {
        writeUR(out, MathUtil.golomb(value));
    }

    public void writeQuan(BitOutputStream out, int value) throws IOException {
        int uval = Math.min(Math.abs(value),31);
        int s = value >>> 31;
        out.write(uval, 5);
        out.write(s, 1);
    }

    public int readUR(BitInputStream in) throws IOException  {
        int cnt = 0;
        while (in.read(1) == 0 && cnt < 32)
            cnt++;
        int res = in.read(m);
        if (cnt > 0 ) {
            res += cnt << m;
        }

        return res;
    }

    public int readSR(BitInputStream in) throws IOException  {
        int val = readUR(in);
        val = H264Utils2.golomb2Signed(val);

        return val;
    }

    public int readQuan(BitInputStream in) throws IOException {
        int val = in.read(5);
        return in.read(1) == 0 ? val : -val;
    }

    public byte[] compression(float[] feature) throws IOException {
//        System.out.print("write quan val:[");//
        outStream.reset();
        BigEndianBitOutputStream outs = new BigEndianBitOutputStream(outStream);
        for (int i = 0; i < DIM; i++) {
            int q_val = Math.round(feature[i] * QP);
            writeQuan(outs, q_val);
//            System.out.print(q_val + ",");//
        }
//        System.out.print("]\n");//
        outs.close();
        byte[] bytes = outStream.toByteArray();
        return bytes;
    }

    public int[] decompression(byte[] feature) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(feature);
        BigEndianBitInputStream in = new BigEndianBitInputStream(inputStream);
//        System.out.print("read  quan val:[");
        for (int i = 0; i < DIM; i++) {
            readArray[i] = readQuan(in);
//            System.out.print(readArray[i] + ",");
        }
//        System.out.print("]\n");
        return readArray;
    }
}

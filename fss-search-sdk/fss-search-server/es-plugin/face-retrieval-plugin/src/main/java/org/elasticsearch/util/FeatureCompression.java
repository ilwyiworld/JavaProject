package org.elasticsearch.util;

import org.jcodec.common.io.BitReader;
import org.jcodec.common.io.BitWriter;

import java.nio.ByteBuffer;

public class FeatureCompression {
    private static final int m = 3;
    private static final int M = 2 << m;
    private static final int DIM = 256;
    private static final int QP = 128;
    private ByteBuffer writeBuf;
    private int[] readArray;

    public FeatureCompression() {
        this.writeBuf = ByteBuffer.allocate(DIM);
        this.readArray = new int[DIM];
    }

    public void writeUR(BitWriter out, int value) {
        int q = value >>> m;
        int r = value & (M - 1);
        out.writeNBit(0, q);
        out.write1Bit(1);
        out.writeNBit(r, m);
    }

    public void writeSR(BitWriter out, int value) {
        // writeUR(out, MathUtil.golomb(value));
        // 有符号 转 无符号
        int uvalue;
        if (value <= 0) {
            uvalue = (-value) << 1;
        } else {
            uvalue = (value << 1) - 1;
        }
        writeUR(out, uvalue);
    }

    // 多1个符号位
    public void writeSIR(BitWriter out, int value) {
        int uval = Math.abs(value);
        int q = uval >>> m;
        int r = uval & (M - 1);
        int s = value >>> 31;
        out.writeNBit(0, q);
        out.write1Bit(1);
        out.write1Bit(s);
        out.writeNBit(r, m);
    }

    // 5bit data ,1bit sign
    public void writeQuan(BitWriter out, int value) {
        int uval = Math.min(Math.abs(value), 31);
        int s = value >>> 31;
        out.writeNBit(uval, 5);
        out.write1Bit(s);
    }

    // 4个数据占3个字节,256个值占192字节，512个值占192*2字节
    public byte[] writeQuanV2(float[] value) {
        int multi = value.length / 256; // lq-modify 2018-05-25
        byte[] bytes = new byte[192 * multi]; // lq-modify 2018-05-23
        int offset = 0;
        int i = 0;
        // System.out.print("write quan val:[");//
        while (offset < DIM * multi) {
            int valquan1 = Math.round(value[offset++] * QP);
            byte uval1 = (byte) ((Math.min(Math.abs(valquan1), 31)));
            byte s1 = (byte) (valquan1 >>> 31);
            int valquan2 = Math.round(value[offset++] * QP);
            byte uval2 = (byte) ((Math.min(Math.abs(valquan2), 31)));
            byte s2 = (byte) (valquan2 >>> 31);
            int valquan3 = Math.round(value[offset++] * QP);
            byte uval3 = (byte) ((Math.min(Math.abs(valquan3), 31)));
            byte s3 = (byte) (valquan3 >>> 31);
            int valquan4 = Math.round(value[offset++] * QP);
            byte uval4 = (byte) ((Math.min(Math.abs(valquan4), 31)));
            byte s4 = (byte) (valquan4 >>> 31);

            bytes[i++] = (byte) ((uval1 << 3) | (s1 << 2) | (uval2 >> 3));
            bytes[i++] = (byte) ((uval2 << 5) | (s2 << 4) | (uval3 >> 1));
            bytes[i++] = (byte) ((uval3 << 7) | (s3 << 6) | (uval4 << 1) | s4);
            // System.out.print(String.format("%d,%d,%d,%d,",valquan1,valquan2,valquan3,valquan4));
        }
        // System.out.print("]\n");//

        return bytes;
    }

    // 1个浮点型占1个字节
    public byte[] writeQuanV3(float[] value) {
        byte[] bytes = new byte[value.length];
        int offset = 0;
        // System.out.print("write quan val:[");//
        while (offset < DIM) {
            bytes[offset] = (byte) (Math.round(value[offset] * QP));
            // System.out.print((int)bytes[offset]+",");
            offset++;
        }
        // System.out.print("]\n");//

        return bytes;
    }

    public int readUR(BitReader bits) {
        int cnt = 0;
        while (bits.read1Bit() == 0 && cnt < 32)
            cnt++;
        int res = bits.readNBit(m);
        if (cnt > 0) {
            res += cnt << m;
        }

        return res;
    }

    public int readSR(BitReader bits) {
        int val = readUR(bits);
        // 无符号 转 有符号
        int sval = 0;
        if ((val & 1) == 1) {
            sval = (val + 1) >> 1;
        } else {
            sval = -(val >> 1);
        }
        // int sval = H264Utils2.golomb2Signed(val);

        return sval;
    }

    public int readSIR(BitReader bits) {
        int cnt = 0;
        while (bits.read1Bit() == 0 && cnt < 32)
            cnt++;

        int s = bits.read1Bit() == 0 ? 1 : -1;
        int res = bits.readNBit(m);
        if (cnt > 0) {
            res += cnt << m;
        }

        return res * s;
    }

    public int readQuan(BitReader bits) {
        return bits.readNBitSigned(5);
    }

    // 4个数据占3个字节,256个值占192字节
    public int[] readQuanV2(byte[] bytes) {
        int[] res = new int[DIM];
        int offset = 0;
        int i = 0;
        // System.out.print("read quan val:[");
        float dist = 0f;
        while (offset < 192) {
            res[offset++] = ((bytes[i] >>> 3) & 0x1f)/** ((bytes[i] & 0x04)== 0 ? 1: -1) */
            ;
            res[offset++] = (((bytes[i] & 0x03) << 3)
                    | ((bytes[i + 1] >>> 5) & 0x07))/** ((bytes[i+1] & 0x10)== 0 ? 1: -1) */
            ;
            res[offset++] = (((bytes[i + 1] & 0x0f) << 1)
                    | ((bytes[i + 2] >>> 7) & 0x01))/** ((bytes[i+2] & 0x40)== 0 ? 1: -1) */
            ;
            res[offset++] = ((bytes[i + 2] & 0x3e) >> 1)/** ((bytes[i+2] & 0x01) == 0 ? 1: -1) */
            ;
            i += 3;
            // System.out.print(String.format("%d,%d,%d,%d,",res[offset-4],res[offset-3],res[offset-2],res[offset-1]));
        }
        // System.out.print("]\n");

        return res;
    }

    // 1个浮点型占1个字节
    public int[] readQuanV3(byte[] bytes) {
        int[] res = new int[bytes.length];
        int offset = 0;
        // System.out.print("write quan val:[");//
        while (offset < DIM) {
            res[offset] = (int) bytes[offset];
            // System.out.print(res[offset]+",");
            offset++;
        }
        // System.out.print("]\n");//

        return res;
    }

    public byte[] compression(float[] feature) {
        ByteBuffer writeBuf2 = ByteBuffer.allocate(DIM);
        ;
        writeBuf2.clear();
        BitWriter bw = new BitWriter(writeBuf2);
        // System.out.print("write quan val:[");//
        for (int i = 0; i < DIM; i++) {
            int q_val = Math.round(feature[i] * QP);
            writeSR(bw, q_val);
            // writeQuan(bw, q_val);
            // System.out.print(q_val + ",");
        }
        // System.out.print("]\n");//
        bw.flush();
        writeBuf2.flip();
        byte[] bytes = new byte[writeBuf2.remaining()];
        writeBuf2.get(bytes, 0, bytes.length);
        return bytes;
    }

    public int[] decompression(byte[] feature) {
        ByteBuffer readbuf = ByteBuffer.wrap(feature);
        BitReader bis = BitReader.createBitReader(readbuf);
        // System.out.print("read quan val:[");
        for (int i = 0; i < DIM; i++) {
            readArray[i] = readSR(bis);
            // readArray[i] = readQuan(bis);
            // System.out.print(readArray[i] + ",");
        }
        // System.out.print("]\n");
        return readArray;
    }

    // compare with golomb-rice(m = 3)encode value
    public float DotV1(byte[] fQuery, byte[] fCompress) {
        int offset = 12;
        if ((fQuery.length - offset) != DIM * 4) {
            // throw new Exception("feature length unmatch");
            // LOG.debug("feature length unmatch");
            return 0f;
        }
        float dist = 0.0f;
        int len = fQuery.length;
        ByteBuffer readbuf = ByteBuffer.wrap(fCompress);
        BitReader bis = BitReader.createBitReader(readbuf);
        while (offset < len) {
            dist += Float.intBitsToFloat(GetInt(fQuery, offset)) * readSR(bis);
            offset += 4;
        }
        return dist / 128;
    }

    // compare with quan value(max value 31), total 192bytes
    public float DotV2(byte[] fQuery, byte[] fCompress) {
        int offset = 12;
        if ((fQuery.length - offset) != fCompress.length * 16 / 3) {
            // throw new Exception("feature length unmatch");
            // LOG.debug("feature length unmatch");
            return 0f;
        }
        int[] res = new int[4];
        int i = 0;
        float dist = 0.0f;
        while (offset < fQuery.length) {
            res[0] = ((fCompress[i] >>> 3) & 0x1f) * ((fCompress[i] >>> 2 & 0x01) == 0 ? 1 : -1);
            res[1] = (((fCompress[i] & 0x03) << 3) | ((fCompress[i + 1] >>> 5) & 0x07))
                    * ((fCompress[i + 1] >>> 4 & 0x01) == 0 ? 1 : -1);
            res[2] = (((fCompress[i + 1] & 0x0f) << 1) | ((fCompress[i + 2] >>> 7) & 0x01))
                    * ((fCompress[i + 2] >>> 6 & 0x01) == 0 ? 1 : -1);
            res[3] = ((fCompress[i + 2] & 0x3e) >> 1) * ((fCompress[i + 2] & 0x01) == 0 ? 1 : -1);
            for (int j = 0; j < 4; j++) {
                dist += Float.intBitsToFloat(GetInt(fQuery, offset)) * res[j];
                offset += 4;
            }
            i += 3;
        }

        return dist / 128;
    }

    // compare with quan value(max value 128), total 256bytes
    public float DotV3(byte[] fQuery, byte[] fCompress) {
        int offset = 12;
        int len1 = fQuery.length;
        int len2 = fCompress.length;
        if ((fQuery.length - offset) != fCompress.length * 4) {
            // throw new Exception("feature length unmatch");
            // LOG.debug("feature length unmatch");
            return 0f;
        }
        int i = 0;
        float dist = 0.0f;
        int len = fQuery.length;
        while (offset < len) {
            // while (i < DIM) {
            // dist += fQuery[i] * fCompress[i];
            // dist += k*fCompress[i];
            dist += Float.intBitsToFloat(GetInt(fQuery, offset)) * fCompress[i];
            // dist += fQuery[i] * fQuery[i];
            i++;
            offset += 4;
        }
        return dist / 128;
    }

    public int GetInt(byte[] bytes, int offset) {
        // return (0xff & bytes[offset]) | (0xff00 & (bytes[offset + 1] << 8)) | (0xff0000 & (bytes[offset + 2] << 16))
        // | (0xff000000 & (bytes[offset + 3] << 24));
        return (0xff & bytes[offset]) | ((0xff & bytes[offset + 1]) << 8) | ((0xff & bytes[offset + 2]) << 16)
                | ((0xff & bytes[offset + 3]) << 24);
    }
}

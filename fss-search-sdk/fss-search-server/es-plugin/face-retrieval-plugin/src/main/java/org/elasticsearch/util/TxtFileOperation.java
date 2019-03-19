package org.elasticsearch.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.zip.GZIPOutputStream;

/**
 * Created by Administrator on 2017/10/9.
 */
public class TxtFileOperation {
    /**
     * 创建文件
     *
     * @param fileName
     * @return
     */
    private static Logger logger = LogManager.getLogger(TxtFileOperation.class.getName());

    public static boolean createFile(File fileName) throws Exception {
        boolean flag = false;
        try {
            if (!fileName.exists()) {

                if (fileName.createNewFile()) {
                    flag = true;
                }
            }
        } catch (Exception e) {
            logger.error(e);
        }
        return flag;
    }

    /**
     * 读TXT文件内容
     *
     * @param fileName
     * @return
     */
    public static String readTxtFile(File fileName) throws Exception {
        String result = "";
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        try {
            fileReader = new FileReader(fileName);
            bufferedReader = new BufferedReader(fileReader);
            try {
                String read;
                while ((read = bufferedReader.readLine()) != null) {
                    result = result + read + "\r\n";
                }
            } catch (Exception e) {
                logger.error(e);
            }
        } catch (Exception e) {
            logger.error(e);
        } finally {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (fileReader != null) {
                fileReader.close();
            }
        }
        System.out.println("读取出来的文件内容是：" + "\r\n" + result);
        return result;
    }

    public static boolean writeTxtFile(String content, File fileName) throws Exception {
        RandomAccessFile mm = null;
        boolean flag = false;
        FileOutputStream o = null;
        try {
            o = new FileOutputStream(fileName);
            o.write(content.getBytes("GBK"));
            // mm=new RandomAccessFile(fileName,"rw");
            // mm.writeBytes(content);
            flag = true;
        } catch (Exception e) {
            // TODO: handle exception
            logger.error(e);
        } finally {
//            if (mm != null) {
//                mm.close();
//            }
            if (o != null) {
                o.close();
            }
        }
        return flag;
    }

    public static void contentToTxt(String filePath, String content) {
        String str = new String(); // 原有txt内容
        String s1 = new String();// 内容更新
        try {
            File f = new File(filePath);
            if (f.exists()) {
                // System.out.print("文件存在");
            } else {
                System.out.print("文件不存在");
                if(!f.createNewFile()){
                    // 不存在则创建
                    logger.error("create file error!");
                }
            }
            try (BufferedReader input = new BufferedReader(new FileReader(f))) {
                while ((str = input.readLine()) != null) {
                    s1 += str + "\n";
                }
            }
            // System.out.println(s1);
            s1 += content;

            try (BufferedWriter output = new BufferedWriter(new FileWriter(f))) {
                output.write(s1);
            }
        } catch (Exception e) {
            logger.error(e);

        }
    }

    private static void genHashObj() {
        String filename = "random_vecs.txt";
        String dstfilename = "lshHashFunctionsCos_16x16.obj";
        //    String filename = "features_32.txt";
        //   String dstfilename = "lshHashFunctionsCos_32.obj";

        try {
            String filecontent = readTxtFile(new File(filename));
            String fileinfo[] = filecontent.split("\r\n");
            File hashFile = new File(dstfilename);
            int dim = 16;
            if (!hashFile.exists()) {
                try (ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(hashFile)))) {
                    oos.writeInt(dim);
                    oos.writeInt(fileinfo.length);
                    for (int c = 0; c < fileinfo.length; c++) {
                        String hashes = fileinfo[c];
                        String hasharray[] = hashes.split(",");
                        assert hasharray.length >= dim : "hasharray is not correct!";
                        for (int j = 0; j < dim; j++) {
                            float rand = Float.parseFloat(hasharray[j].trim());
                            oos.writeFloat(rand);
                        }
                    }
                }
            } else {
                System.err.println("Hashes could not be written: " + "lshHashFunctionsCos_16.obj" + " already exists");
            }

        } catch (Exception e) {
            logger.error(e);
        }
    }

    private static void genPCAObj() {
        String filename[] = {"pca_mean_32.txt", "pca_components_32.txt"};
        String dstfilename[] = {"pcaMean_256_32.obj", "pcaComponents_256x32.obj"};
        int dimN[] = {256, 32};
        int dimM[] = {1, 256};

        try {
            for (int i = 0; i < filename.length; i++) {
                String filecontent = readTxtFile(new File(filename[i]));
                String fileinfo[] = filecontent.split("\r\n");
                File pcaFile1 = new File(dstfilename[i]);

                if (!pcaFile1.exists()) {
                    try (ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(pcaFile1)))) {
                        System.out.println(filename[i] + " line - " + fileinfo.length);
                        oos.writeInt(dimN[i]);
                        oos.writeInt(dimM[i]);
                        for (int c = 0; c < fileinfo.length; c++) {
                            String hashes = fileinfo[c];
                            String hasharray[] = hashes.split(",");
                            for (int j = 0; j < hasharray.length; j++) {
                                float rand = Float.parseFloat(hasharray[j].trim());
                                oos.writeFloat(rand);
                            }
                        }
                    }
                } else {
                    System.err.println("Hashes could not be written: " + "lshHashFunctionsCos_16.obj" + " already exists");
                }
            }

        } catch (Exception e) {
            logger.error(e);
        }
    }

    public static void main(String args[]) {
        //  genHashObj();
        genPCAObj();
    }
}

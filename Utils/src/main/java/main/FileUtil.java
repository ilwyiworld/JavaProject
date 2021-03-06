package main;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018-11-21.
 */
public class FileUtil {
    private List<String> picPaths = new ArrayList<>();
    Long total = 0L;

    public static String writeFile2Str(String filePath) throws Exception {
        File file = new File(filePath);//定义一个file对象，用来初始化FileReader
        FileReader reader = new FileReader(file);//定义一个fileReader对象，用来初始化BufferedReader
        BufferedReader bReader = new BufferedReader(reader);//new一个BufferedReader对象，将文件内容读取到缓存
        StringBuilder sb = new StringBuilder();//定义一个字符串缓存，将字符串存放缓存中
        String s = "";
        while ((s = bReader.readLine()) != null) {//逐行读取文件内容，不读取换行符和末尾的空格
            sb.append(s + "\n");//将读取的字符串添加换行符后累加存放在缓存中
        }
        bReader.close();
        return sb.toString();
    }

    public void traverseFolder(String path) {
        File file = new File(path);
        if (file.exists()) {
            File[] files = file.listFiles();
            if (files.length == 0) {
                System.out.println("文件夹是空的!");
                return;
            } else {
                for (File file2 : files) {
                    if (file2.isDirectory()) {
                        System.out.println("文件夹:" + file2.getAbsolutePath());
                        traverseFolder(file2.getAbsolutePath());
                    } else {
                        picPaths.add(file2.getAbsolutePath());
                    }
                }
            }
        } else {
            System.out.println("文件不存在!");
        }
    }

    //统计文件数量
    private File generateFileList() {
        File fileListFile = new File("C:\\Users\\yiliang\\Desktop\\file.list");
        // 一次写入文件列表的文件个数
        int WRITE_TO_FILE_LIST_COUNT_ONCE = 10;
        File workingDir = new File("C:\\Users\\yiliang\\Desktop\\yi");
        List<String> filePathNames = new ArrayList<>(WRITE_TO_FILE_LIST_COUNT_ONCE);
        try {
            Files.walk(workingDir.toPath(), 5)
                    .map(path -> path.toFile())
                    .filter(file -> matchedPhotoFile(file))
                    .forEach(file -> {
                        filePathNames.add(file.getPath());
                        if (filePathNames.size() >= WRITE_TO_FILE_LIST_COUNT_ONCE) {
                            //将文件夹里的文件名按行写入文件中
                            writeFilePathNamesToListFile(fileListFile, filePathNames);
                            filePathNames.clear();
                        }
                        total++;
                    });
            if (filePathNames.size() > 0) {
                writeFilePathNamesToListFile(fileListFile, filePathNames);
                filePathNames.clear();
            }
            System.out.println(total);
            return fileListFile;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean matchedPhotoFile(File file) {
        if (!file.exists() || file.isDirectory()) {
            return false;
        }
        return true;
    }

    private void writeFilePathNamesToListFile(File fileListFile, List<String> filePathNames) {
        try {
            FileUtils.writeLines(fileListFile, filePathNames, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        new FileUtil().generateFileList();
    }

}

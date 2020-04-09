package mongo;

import avro.shaded.com.google.common.collect.Lists;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import main.PropertiesUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.bson.Document;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Properties;

public class InsertCapture {

    public final static String UUID = "2020030412345";
    public final static String INSERT_URL = "http://10.10.1.119:8080/api/v1/tmp/insert/";
    public final static String ONEVMORE_URL = "http://10.10.1.119:8080/api/v1/search/1vn";
    public final static String FEATURE_URL = "http://10.10.1.119:8080/api/v1/tmp/getFeature/";
    public static long totalTime = 0L;
    public static int hasResult = 0;
    public static int totalMatchCount = 0;

    public static int n = 0;

    public static void main(String[] args) throws Exception {
        String filterPath = "mongo.insertCapture.properties";
        Properties properties = PropertiesUtil.getProperties(filterPath);
        insertMongoAndSdk(properties);
        //compare(properties);
    }

    // 抓拍测试数据写入mongo和sdk
    private static void insertMongoAndSdk(Properties properties) throws Exception {
        String insertPicPath = properties.getProperty("insertPicPath");
        String insertNoMatchPath = properties.getProperty("insertNoMatchPath");
        String parserType = properties.getProperty("parserType");
        String cameraId = properties.getProperty("cameraId");
        MongoDatabase mongoDatabase = getConnect();
        MongoCollection collection = mongoDatabase.getCollection("capture".concat(parserType));
        File picListFile = new File(insertPicPath);
        File noMatchFile = new File(insertNoMatchPath);
        List<String> noMatch = Lists.newArrayList();
        Files.walk(picListFile.toPath(), 2)
                .map(Path::toFile)
                .filter(file -> {
                    return file.getName().endsWith(".jpg");
                }).forEach(file -> {
            try {
                //String name = UUID.concat(file.getName().split("\\.")[0].split("_")[2].replaceAll("-", ""));
                String name = file.getName();
                JSONObject capture = new JSONObject();
                JSONObject property = new JSONObject();
                capture.put("property", property);
                capture.put("cameraId", Integer.parseInt(cameraId));
                capture.put("timestamp", System.currentTimeMillis() / 1000);
                String base64 = encryptToBase64(file.getAbsolutePath());
                JSONObject obj = new JSONObject();
                obj.put("base64", base64);
                obj.put("type", parserType);
                obj.put("cameraId", Integer.parseInt(cameraId));
                // 写入sdk 并获得featureId 、图片node和key
                String result = doPost(obj, INSERT_URL);
                if (result != null && result != "") {
                    capture.put("captureId", result.split("_")[0]);
                    capture.put("imageUrl", result.split("_")[1]);
                    capture.put("sceneUrl", name);
                    Document d = Document.parse(capture.toJSONString());
                    // 插入mongo
                    collection.insertOne(d);
                    System.out.println("成功插入：" + (++n) + " 条");
                }
            } catch (Exception e) {
                e.printStackTrace();
                noMatch.add(file.getName());
                System.out.println("上传失败：" + file.getName());
            }
            /*try {
                Thread.sleep(66);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
        });
        FileUtils.writeLines(noMatchFile, noMatch, true);
    }

    // 比对
    private static void compare(Properties properties) throws Exception {
        String comparePicPath = properties.getProperty("comparePicPath");
        String parserType = properties.getProperty("parserType");
        String timeCountPath = properties.getProperty("timeCountPath");
        String noMatchPath = properties.getProperty("compareNoMatchPath");
        String matchCountPath = properties.getProperty("matchCountPath");
        Integer cameraId = Integer.parseInt(properties.getProperty("cameraId"));
        Integer threshold = Integer.parseInt(properties.getProperty("threshold"));
        List<String> timeList = Lists.newArrayList();
        List<String> noMatchList = Lists.newArrayList();
        List<String> matchCountList = Lists.newArrayList();
        File fileListFile = new File(comparePicPath);
        File timeCountFile = new File(timeCountPath);
        File matchCountFile = new File(matchCountPath);
        File noMatchFile = new File(noMatchPath);
        StringBuilder eachTime = new StringBuilder();
        Files.walk(fileListFile.toPath(), 1)
                .map(Path::toFile)
                .filter(file -> {
                    return file.getName().endsWith("00.jpg");
                }).forEach(file -> {
            try {
                String name = file.getName().split("\\.")[0];
                String captureIdPre = UUID.concat(name.split("_")[2].split("-")[0]);
                String base64 = encryptToBase64(file.getAbsolutePath());
                JSONObject obj = new JSONObject();
                List<Integer> repoIds = Lists.newArrayList();
                List<Integer> cameraIds = Lists.newArrayList();
                cameraIds.add(cameraId);
                obj.put("cameraIds", cameraIds);
                obj.put("repoIds", repoIds);
                obj.put("type", parserType.toUpperCase());
                obj.put("threshold", threshold);
                obj.put("maxResults", 20);
                obj.put("currentPage", 0);
                obj.put("recordPerPage", 20);
                obj.put("base64", base64);
                String featureBase64 = doPost(obj, FEATURE_URL);
                obj.remove("base64");
                obj.put("featureBase64", featureBase64);
                long startTime = System.currentTimeMillis();
                String compareResult = doPost(obj, ONEVMORE_URL);
                long time = System.currentTimeMillis() - startTime;
                totalTime += time;
                eachTime.append(time).append(",");
                if (StringUtils.isNotBlank(compareResult)) {
                    JSONArray array = JSON.parseObject(compareResult).getJSONObject("datas").getJSONArray("rows");
                    int size = array.size();
                    if (size > 0) {
                        hasResult++;
                        int matchCount = 0;
                        for (int i = 0; i < size; i++) {
                            String captureId = array.getJSONObject(i).getString("sceneImageUrl");
                            if (StringUtils.isNotBlank(captureId) && captureId.startsWith(captureIdPre)) {
                                matchCount++;
                            }
                        }
                        matchCountList.add(file.getName() + " matchCount：" + matchCount);
                        totalMatchCount += matchCount;
                    } else {
                        noMatchList.add(file.getName());
                    }
                }
                System.out.println("检索成功：".concat(file.getName()));
                Thread.sleep(100);
            } catch (Exception e) {
                System.out.println("检索失败：".concat(file.getName()));
            }

        });
        timeList.add("searchTime:" + eachTime.substring(0, eachTime.length() - 1).toString());
        timeList.add("totalTime：" + totalTime);
        timeList.add("hasResult：" + hasResult);
        timeList.add("-------------------------");
        timeList.add("\t");
        noMatchList.add("-------------------------");
        noMatchList.add("\t");
        matchCountList.add("\t");
        matchCountList.add(threshold + "命中率：" + (float)totalMatchCount / 500);
        matchCountList.add("-------------------------");
        matchCountList.add("\t");
        // 统计写入文件
        FileUtils.writeLines(timeCountFile, timeList, true);
        FileUtils.writeLines(noMatchFile, noMatchList, true);
        FileUtils.writeLines(matchCountFile, matchCountList, true);
    }

    //需要密码认证方式连接
    public static MongoDatabase getConnect() {
        List<ServerAddress> adds = new ArrayList<>();
        ServerAddress serverAddress = new ServerAddress("10.10.1.89", 27017);
        adds.add(serverAddress);
        List<MongoCredential> credentials = new ArrayList<>();
        MongoCredential mongoCredential = MongoCredential.createScramSha1Credential("pensees", "admin", "pensees@1234".toCharArray());
        credentials.add(mongoCredential);
        MongoClient mongoClient = new MongoClient(adds, credentials);
        MongoDatabase mongoDatabase = mongoClient.getDatabase("parser");
        return mongoDatabase;
    }

    public static String convertFileToBase64(String imgPath) throws IOException {
        BufferedImage image = ImageIO.read(new File(imgPath));
        ByteArrayOutputStream baoS = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baoS);
        baoS.flush();
        return Base64.getEncoder().encodeToString(baoS.toByteArray());
    }

    public static String encryptToBase64(String filePath) {
        if (filePath == null) {
            return null;
        }
        try {
            byte[] b = Files.readAllBytes(Paths.get(filePath));
            return Base64.getEncoder().encodeToString(b);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String doPost(JSONObject obj, String url) {
        String result = null;
        HttpPost httpPost = new HttpPost(url);
        CloseableHttpClient client = HttpClients.createDefault();
        StringEntity entity = new StringEntity(obj.toString(), Charset.forName("UTF-8"));
        entity.setContentType("application/json");
        httpPost.setEntity(entity);
        CloseableHttpResponse response = null;
        HttpEntity resEntity = null;
        try {
            response = client.execute(httpPost);
            resEntity = response.getEntity();
            result = EntityUtils.toString(resEntity);
        } catch (Exception e) {

        } finally {
            if (null != response) {
                try {
                    response.close();
                } catch (IOException e) {

                }
            }
            if (null != resEntity) {
                try {
                    EntityUtils.consume(resEntity);
                } catch (IOException e) {

                }
            }
        }
        return result;
    }

}


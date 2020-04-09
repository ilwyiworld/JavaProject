package mongo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import main.PropertiesUtil;
import org.apache.commons.io.FileUtils;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

public class DownloadPicFromMongo {

    public static void main(String[] args) {
        String filterPath = "mongo.filter.properties";
        Properties properties = PropertiesUtil.getProperties(filterPath);
        filterFind(properties);
    }

    public static void filterFind(Properties properties) {
        //String captureId=properties.getProperty("captureId");
        int cameraId = Integer.parseInt(properties.getProperty("cameraId"));
        String urlPath = properties.getProperty("urlPath");
        String downloadDir = properties.getProperty("downloadDir");
        String downloadPath = properties.getProperty("downloadPath");
        int timestampMin = Integer.parseInt(properties.getProperty("timestamp.min"));
        int timestampMax = Integer.parseInt(properties.getProperty("timestamp.max"));
        MongoDatabase mongoDatabase = DownloadPicFromMongo.getConnect();
        MongoCollection<Document> collection = mongoDatabase.getCollection("captureNonMoto");
        //指定查询过滤器
        Bson filter;
        filter = Filters.and(
                Filters.gte("timestamp", timestampMin),
                Filters.lte("timestamp", timestampMax),
                Filters.eq("cameraId", cameraId)
        );
        //指定查询过滤器查询
        FindIterable findIterable = collection.find(filter);
        MongoCursor<Document> cursor = findIterable.iterator();
        JSONArray propertyArr = new JSONArray();
        while (cursor.hasNext()) {
            Document doc = cursor.next();
            JSONObject obj = JSON.parseObject(JSONObject.toJSONString(doc.get("property")));
            obj.remove("_class");
            String id = (String) doc.get("captureId");
            obj.put("captureId", id);
            obj.put("img", id + ".jpg");
            obj.put("fullImg", "Full_" + id + ".jpg");
            propertyArr.add(obj);
            String imgNode = ((String) doc.get("imageUrl")).split("/")[0];
            String imgKey = ((String) doc.get("imageUrl")).split("/")[1];
            String fullNode = ((String) doc.get("sceneUrl")).split("/")[0];
            String fullKey = ((String) doc.get("sceneUrl")).split("/")[1];
            String imgUrl = String.format(urlPath, imgKey, imgNode);
            String fullUrl = String.format(urlPath, fullKey, fullNode);
            downloadFile(downloadDir, imgUrl, id + ".jpg");
            downloadFile(downloadDir, fullUrl, "Full_" + id + ".jpg");
        }
        try {
            FileUtils.writeLines(new File(downloadPath), propertyArr, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private static void downloadFile(String downloadDir, String urlPath, String name) {
        try {
            URL url = new URL(urlPath);
            URLConnection urlConnection = url.openConnection();
            HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;
            httpURLConnection.setConnectTimeout(1000 * 5);
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setRequestProperty("Charset", "UTF-8");
            httpURLConnection.connect();
            BufferedInputStream bin = new BufferedInputStream(httpURLConnection.getInputStream());
            // 指定存放位置(有需求可以自定义)
            String path = downloadDir + File.separatorChar + name;
            File file = new File(path);
            // 校验文件夹目录是否存在，不存在就创建一个目录
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            OutputStream out = new FileOutputStream(file);
            int size = 0;
            int len = 0;
            byte[] buf = new byte[2048];
            while ((size = bin.read(buf)) != -1) {
                len += size;
                out.write(buf, 0, size);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("文件下载失败！");
        }

    }
}


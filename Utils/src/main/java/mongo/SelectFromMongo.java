package mongo;

import avro.shaded.com.google.common.collect.Lists;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class SelectFromMongo {

    public static void main(String[] args) {
        filterFind();
    }

    public static void filterFind() {
        List<String> featureIdList = Lists.newArrayList();
        List<String> personIdList = Lists.newArrayList();
        List<String> notPersonIdList = Lists.newArrayList();
        MongoDatabase captureDatabase = SelectFromMongo.getConnect().getDatabase("capture");
        MongoDatabase faceDatabase = SelectFromMongo.getConnect().getDatabase("face");
        MongoCollection<Document> collision = captureDatabase.getCollection("collision");
        MongoCollection<Document> face = faceDatabase.getCollection("face");
        //指定查询过滤器
        Bson filter = Filters.and(
                Filters.eq("collision_id", 3)
        );
        //指定查询过滤器查询
        FindIterable findIterable = collision.find(filter);
        MongoCursor<Document> cursor = findIterable.iterator();

        while (cursor.hasNext()) {
            Document doc = cursor.next();
            String feature_id = (String) doc.get("source_feature_id");
            featureIdList.add(feature_id);
        }

        Bson faceFilter = Filters.and(
                Filters.in("feature_id", featureIdList),
                Filters.eq("repository_id", 83)
        );
        FindIterable faceIterable = face.find(faceFilter);
        MongoCursor<Document> cursorFace = faceIterable.iterator();
        while (cursorFace.hasNext()) {
            Document doc = cursorFace.next();
            String person_id = (String) doc.get("person_id");
            personIdList.add(person_id);
        }

        Bson noFaceFilter = Filters.and(
                Filters.eq("repository_id", 83),
                Filters.nin("feature_id", featureIdList)
        );
        FindIterable noFaceIterable = face.find(noFaceFilter);
        MongoCursor<Document> cursorNoFace = noFaceIterable.iterator();
        while (cursorNoFace.hasNext()) {
            Document doc = cursorNoFace.next();
            String person_id = (String) doc.get("person_id");
            notPersonIdList.add(person_id);
        }
        File faceFile = new File("/home/ubuntu/yl/mongoFace/face.txt");
        File noFaceFile = new File("/home/ubuntu/yl/mongoFace/noFace.txt");
        File featureFile = new File("/home/ubuntu/yl/mongoFace/feature.txt");
        try {
            FileUtils.writeLines(faceFile, personIdList, false);
            FileUtils.writeLines(noFaceFile, notPersonIdList, false);
            FileUtils.writeLines(featureFile, featureIdList, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //需要密码认证方式连接
    public static MongoClient getConnect() {
        List<ServerAddress> adds = new ArrayList<>();
        ServerAddress serverAddress = new ServerAddress("11.61.61.103", 27017);
        adds.add(serverAddress);
        List<MongoCredential> credentials = new ArrayList<>();
        MongoCredential mongoCredential = MongoCredential.createScramSha1Credential("pesuser", "admin", "pensees@huairou".toCharArray());
        credentials.add(mongoCredential);
        MongoClient mongoClient = new MongoClient(adds, credentials);
        return mongoClient;
    }

}


package mongo;

import avro.shaded.com.google.common.collect.Lists;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.apache.commons.io.FileUtils;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CopyFromDir {

    public static void main(String[] args) {
        filterFind();
    }

    public static void filterFind() {
        String filePre = "/home/ubuntu/ZTBH20200522/";
        File dir = new File("/home/ubuntu/ZTBH20200522_2");
        List<String> featureIdList = Lists.newArrayList();
        MongoDatabase captureDatabase = CopyFromDir.getConnect().getDatabase("capture");
        MongoDatabase faceDatabase = CopyFromDir.getConnect().getDatabase("face");
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

        Bson noFaceFilter = Filters.and(
                Filters.eq("repository_id", 83),
                Filters.nin("feature_id", featureIdList)
        );
        FindIterable noFaceIterable = face.find(noFaceFilter);
        MongoCursor<Document> cursorNoFace = noFaceIterable.iterator();
        while (cursorNoFace.hasNext()) {
            Document doc = cursorNoFace.next();
            String person_id = (String) doc.get("person_id");
            File noFaceFile = new File(filePre.concat(person_id).concat("_1.jpg"));
            try{
                if (noFaceFile.exists()) {
                    FileUtils.copyFileToDirectory(noFaceFile, dir);
                }else{
                    FileUtils.copyFileToDirectory(new File(filePre.concat(person_id).concat(".jpg")), dir);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
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
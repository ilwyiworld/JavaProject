package ImageTest;

import com.alibaba.fastjson.JSONObject;
import com.znv.kafka.ProducerBase;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Random;

/**
 * Created by ct on 2016-10-13.
 */
public class ImageProducer {
    private static final String TOPIC = "image-byte-1229";
    private static final String MSG_TYPE = "msg_type";
    private static final String DATA = "data";
    private static final String TYPE = "type";
    private static final int MAX_LEN = 10240;// 51200 ok 512000 not ok

    private Random rnd = new Random();

    ProducerBase producer = new ProducerBase();

    // private static class callback implements Callback{
    // public void onCompletion(RecordMetadata metadata, Exception exception){
    // if (metadata != null){
    // //System.out.println("onCompletion send ok");
    // }else {
    // System.out.println(exception.getMessage());
    // }
    // }
    // }

    public void send(Path file, String key, byte[] data, String end) {
        try {
            JSONObject json = new JSONObject();
            // System.out.printf("data len = %d \n",data.length);
            // json.put("data", data);
            // ByteBuffer buffer = ByteBuffer.wrap(data);
            json.put(DATA, data);
            json.put(MSG_TYPE, "image");
            json.put(TYPE, end);
            // json.put(DATA, buffer);
            producer.sendData(key, json);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void uploadData(Path file) {
        try {
            BufferedInputStream in = new BufferedInputStream(Files.newInputStream(file));
            try {
                // String fhash = String.valueOf(file.getFileName().toString().hashCode());
                // int keyabs = ((Math.abs(Integer.parseInt(fhash))) % 5 );
                // String key = String.valueOf(keyabs);
                Integer prex = rnd.nextInt(6);
                String key = prex.toString() + "_" + file.getFileName().toString();
                byte[] bytes = new byte[MAX_LEN];
                byte[] data;
                int bytesRead = in.read(bytes);
                int count = 0;
                String end = "false";
                while (bytesRead > 0) {
                    count++;
                    if (bytesRead < bytes.length) {
                        data = Arrays.copyOf(bytes, bytesRead);
                        end = "true";
                        System.out.printf("[%s]send[%d] last data len = %d \n", key, count, data.length);
                    } else {
                        data = bytes;
                    }

                    send(file, key, data, end);
                    Thread.sleep(1);// 必须sleep，否则图片数据过大时会出现丢数据，且kafka日志没有异常信息
                    bytesRead = in.read(bytes);
                }
            } finally {
                in.close();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    public void uploadFile() throws IOException {
        Path path = Paths.get("E:\\IdeaProject\\iCapSDK\\picuture");
        if (Files.isDirectory(path)) {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    // System.out.println("filename " + file.getFileName());
                    uploadData(file);

                    return FileVisitResult.CONTINUE;
                }
            });
        }

    }

    public static void main(String[] args) throws IOException {
        ImageProducer imageProducer = new ImageProducer();
        imageProducer.uploadFile();
    }
}

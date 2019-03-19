package main;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import net.coobird.thumbnailator.Thumbnails;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.io.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.codec.binary.Base64;
import sun.misc.BASE64Encoder;

import javax.imageio.stream.FileImageOutputStream;

/**
 * Created by Administrator on 2018/2/27.
 */
public class Main {
    public static void main(String[] args) throws Exception {
        /*String brokerList = "10.45.152.238:9092";
        String groupId = "consumer_r_yiworld";
        String topic = "data1";
        int consumerNum = 1;
        ConsumerGroup consumerGroup = new ConsumerGroup(consumerNum, groupId, topic, brokerList);
        consumerGroup.execute();*/
        /*Properties props = PropertiesUtil.getProperties("consumer.properties");
        KafkaConsumer consumer = new KafkaConsumer(props);
        ArrayList<String> topics = TopicUtil.getTopicList("receive.topics");
        consumer.subscribe(topics);
        while (true){
            ConsumerRecords<String, String> records = consumer.poll(100);
            System.out.println("records:"+records.count());
            for (ConsumerRecord<String, String> record : records) {
                try {
                    String value = record.value();
                    JSONObject obj = JSON.parseObject(value);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }*/
        InputStream inputStream = null;
        byte[] data = null;
        try {
            inputStream = new FileInputStream("C:\\Users\\Administrator\\Desktop\\2.jpg");
            data = new byte[inputStream.available()];
            inputStream.read(data);
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //图片base64
        String fileStr=Base64Util.encode(data);
        System.out.println(fileStr.length());

        byte[] data2=Base64Util.decode(fileStr);
        byte[] buf;
        InputStream in=new ByteArrayInputStream(data);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        //Thumbnails.of(in).scale(0.1f).toOutputStream(outputStream);
        Thumbnails.of(in).size(640, 512).toOutputStream(outputStream);
        buf = outputStream.toByteArray();
        String data3=Base64Util.encode(buf);

        File result = new File("C:\\Users\\Administrator\\Desktop\\1.jpg");//要写入的图片
        FileImageOutputStream imageOutput = new FileImageOutputStream(result);
        imageOutput.write(buf, 0, buf.length);
        imageOutput.close();// 关闭输入输出流

        System.out.println(data3.length());
    }

    public static String getInnetIp() throws SocketException {
        String localip = null;// 本地IP，如果没有配置外网IP则返回它
        String netip = null;// 外网IP
        Enumeration<NetworkInterface> netInterfaces;
        netInterfaces = NetworkInterface.getNetworkInterfaces();
        InetAddress ip = null;
        boolean finded = false;// 是否找到外网IP
        while (netInterfaces.hasMoreElements() && !finded) {
            NetworkInterface ni = netInterfaces.nextElement();
            Enumeration<InetAddress> address = ni.getInetAddresses();
            while (address.hasMoreElements()) {
                ip = address.nextElement();
                if (!ip.isSiteLocalAddress()&&!ip.isLoopbackAddress()&&ip.getHostAddress().indexOf(":") == -1){// 外网IP
                    netip = ip.getHostAddress();
                    finded = true;
                    break;
                } else if (ip.isSiteLocalAddress()&&!ip.isLoopbackAddress()&&ip.getHostAddress().indexOf(":") == -1){// 内网IP
                    localip = ip.getHostAddress();
                }
            }
        }
        if (netip != null && !"".equals(netip)) {
            return netip;
        } else {
            return localip;
        }
    }

    public class ListNode {
        int val;
        ListNode next;

        ListNode(int x) {
            val = x;
        }
    }

    public ListNode addTwoNumbers(ListNode l1, ListNode l2) {

        return null;
    }
}



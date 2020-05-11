package main;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import javax.swing.*;

/**
 * Created by Administrator on 2018/2/27.
 */
public class Main {
    public static void main(String[] args) throws Exception {
        //execCommend("nvidia-smi -L","10.10.1.82",22,"ubuntu","cimevue");
        File picFile = new File("/home/ftpuser/pics");
        Files.walk(picFile.toPath(), 2)
                .map(Path::toFile)
                .filter(
                        file -> (file.getName().endsWith(".jpg") || file.getName().endsWith(".png") || file.getName().endsWith(".DAT"))
                ).forEach(file -> {
            FileUtils.deleteQuietly(file);
            System.out.println("delete file " + file.getName());
        });

        /*DecimalFormat df = new DecimalFormat("#.000");
        float test=(float)4/3;
        System.out.println(test);
        String dd=df.format(test);
        System.out.println(dd);
        System.out.println(1.0==1);

        Stream<String> stream = Stream.of("I", "love", "you", "too");
        Map<String, Integer> map = stream.collect(Collectors.toMap(Function.identity(), String::length));
        System.out.println(map.entrySet());*/
        /*String brokerList = "10.45.152.238:9092";
        String groupId = "consumer_r_yiworld";
        String topic = "data1";
        int consumerNum = 1;
        ConsumerGroup consumerGroup = new ConsumerGroup(consumerNum, groupId, topic, brokerList);
        consumerGroup.execute();*/
        /*Properties props = PropertiesUtil.getProperties("kafka.consumer.properties");
        KafkaConsumer kafka.consumer = new KafkaConsumer(props);
        ArrayList<String> topics = TopicUtil.getTopicList("receive.topics");
        kafka.consumer.subscribe(topics);
        while (true){
            ConsumerRecords<String, String> records = kafka.consumer.poll(100);
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
        /*InputStream inputStream = null;
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

        System.out.println(data3.length());*/

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
                if (!ip.isSiteLocalAddress() && !ip.isLoopbackAddress() && ip.getHostAddress().indexOf(":") == -1) {// 外网IP
                    netip = ip.getHostAddress();
                    finded = true;
                    break;
                } else if (ip.isSiteLocalAddress() && !ip.isLoopbackAddress() && ip.getHostAddress().indexOf(":") == -1) {// 内网IP
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

    public static int execCommend(String command, String ip, int port, String user, String passwd) {
        int result = 0;
        Connection connection = new Connection(ip, port);
        Session session = null;
        InputStream stdout = null;
        BufferedReader stdoutReader = null;
        try {
            connection.connect();
            boolean isAuthed = connection.authenticateWithPassword(user, passwd);
            if (isAuthed) {
                session = connection.openSession();
                session.execCommand(command);
                stdout = new StreamGobbler(session.getStdout());
//               InputStream stderr = new StreamGobbler(session.getStderr());
                stdoutReader = new BufferedReader(
                        new InputStreamReader(stdout));
//                BufferedReader stderrReader = new BufferedReader(
//                        new InputStreamReader(stderr));
                while (true) {
                    String line = stdoutReader.readLine();
                    if (line == null) {
                        break;
                    }
                    System.out.println("Stdout " + ip + " :" + line);
                    getGpuInfo(line);
                }
            } else {
                result = 1;
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            result = -1;
        } finally {
            if (null != stdoutReader) {
                try {
                    stdoutReader.close();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
            if (null != stdout) {
                try {
                    stdout.close();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
            if (null != session) {
                session.close();
            }
            if (null != connection) {
                connection.close();
            }
        }
        return result;
    }

    public static void getGpuInfo(String line) {
        String gpuNum = line.split(":")[0].trim();
        String gpuName = line.split(":")[1].split("\\(")[0].trim();
        System.out.println(gpuNum + ";" + gpuName);
    }

    public static int getFile(ByteArrayOutputStream bos, String remoteFile, String ip, int port, String user, String passwd) {
        int result = 0;
        Connection connection = new Connection(ip, port);
        try {
            connection.connect();
            boolean isAuthed = connection.authenticateWithPassword(user, passwd);
            if (isAuthed) {
                SCPClient scpClient = connection.createSCPClient();
                scpClient.get(remoteFile, bos);
                //new String(bos.toByteArray(), "UTF-8");
            } else {
                result = 1;
            }
        } catch (Exception ex) {
            result = -1;
        } finally {
            if (null != connection) {
                connection.close();
            }
        }
        return result;
    }

    /**
     * @param data                  文件二进制数据
     * @param remoteFileName        远程文件名
     * @param remoteTargetDirectory 远程文件目录
     * @param ip
     * @param port
     * @param user
     * @param passwd
     * @return
     */
    public static int putFile(byte[] data, String remoteFileName, String remoteTargetDirectory, String ip, int port, String user, String passwd) {
        int result = 0;
        Connection connection = new Connection(ip, port);
        try {
            connection.connect();
            boolean isAuthed = connection.authenticateWithPassword(user, passwd);
            if (isAuthed) {
                SCPClient scpClient = connection.createSCPClient();
                scpClient.put(data, remoteFileName, remoteTargetDirectory);
            } else {
                result = 1;
            }
        } catch (Exception ex) {
            result = -1;
        } finally {
            if (null != connection) {
                connection.close();
            }
        }
        return result;
    }

    @Test
    public void test() {
        B b = new B();
        b.scan();  //我的输出结果是什么？
    }

    static class A {
        public void scan() {
            doScan();
        }

        protected void doScan() {
            System.out.println("A.doScan");
        }
    }

    static class B extends A {
        @Override
        protected void doScan() {
            System.out.println("B.doScan");
        }
    }
}





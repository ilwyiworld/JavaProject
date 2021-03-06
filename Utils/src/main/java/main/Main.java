package main;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;
import cn.hutool.Hutool;
import lombok.Builder;
import lombok.Data;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.compress.compressors.gzip.GzipUtils;
import org.apache.commons.io.FileUtils;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.openjdk.jol.info.ClassLayout;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;

/**
 * Created by Administrator on 2018/2/27.
 */
public class Main {
    public static void main(String[] args) throws Exception {
        /*File picFile = new File("/home/ftpuser/pics");
        Files.walk(picFile.toPath(), 2)
                .map(Path::toFile)
                .filter(
                        file -> (file.getName().endsWith(".jpg") || file.getName().endsWith(".png") || file.getName().endsWith(".DAT"))
                ).forEach(file -> {
            FileUtils.deleteQuietly(file);
            System.out.println("delete file " + file.getName());
        });*/
        /*long[] weekData = new long[]{0L, 0L, 0L, 0L, 0L, 0L, 0L};
        ArrayUtils.toObject(weekData);
        System.out.println(StringUtils.join(ArrayUtils.toObject(weekData),","));*/
        /*LocalDateTime nowDate = LocalDateTime.now().minusHours(12);
        System.out.println(getDayZeroTime(nowDate));
        System.out.println(getDayZeroTime(nowDate.with(TemporalAdjusters.firstDayOfMonth())));*/
        File picture = new File("C:\\Users\\yiliang\\Desktop\\图片\\111111.jpg");
        BufferedImage sourceImg = ImageIO.read(new FileInputStream(picture));
        System.out.println(String.format("%.1f",picture.length()/1024.0));
        System.out.println(sourceImg.getWidth());
        System.out.println(sourceImg.getHeight());
    }

    public static Long getDayZeroTime(LocalDateTime localDateTime) {
        try {
            DateTimeFormatter formatter=DateTimeFormatter.ofPattern("yyyy-MM-dd 00:00:00");
            // 当天 00:00:00 字符串 2020-06-06 00:00:00
            String zeroTimeStr = formatter.format(localDateTime);
            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime ldt = LocalDateTime.parse(zeroTimeStr, df);
            return localDateTimeToTimestamp(ldt);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Long localDateTimeToTimestamp(LocalDateTime localDateTime) {
        try {
            ZoneId zoneId = ZoneId.systemDefault();
            Instant instant = localDateTime.atZone(zoneId).toInstant();
            return instant.toEpochMilli();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void scaleImg() throws IOException {
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
        String fileStr = Base64Util.encode(data);
        System.out.println(fileStr.length());

        byte[] data2 = Base64Util.decode(fileStr);
        byte[] buf;
        InputStream in = new ByteArrayInputStream(data);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        //Thumbnails.of(in).scale(0.1f).toOutputStream(outputStream);
        Thumbnails.of(in).size(640, 512).toOutputStream(outputStream);
        buf = outputStream.toByteArray();
        String data3 = Base64Util.encode(buf);
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
//              InputStream stderr = new StreamGobbler(session.getStderr());
                stdoutReader = new BufferedReader(
                        new InputStreamReader(stdout));
//              BufferedReader stderrReader = new BufferedReader(new InputStreamReader(stderr));
                while (true) {
                    String line = stdoutReader.readLine();
                    if (line == null) {
                        break;
                    }
                    System.out.println("Stdout " + ip + " :" + line);
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
}
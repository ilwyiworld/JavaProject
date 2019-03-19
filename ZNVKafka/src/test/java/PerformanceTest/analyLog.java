package PerformanceTest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * Created by ct on 2016-11-11.
 */
public class analyLog {

    private static final Logger L = LoggerFactory.getLogger(analyLog.class);

    public void fetchLogInfo() {
        File configFile = new File("yarn-yarn-nodemanager-bfw01.log");
        File wFile = new File("yarn.log");
        // File wFile2 = new File("master-out2.log");

        String tempString = null;
        long count = 0;
        System.out.println("=====start=====");

        try (BufferedReader in = new BufferedReader(new FileReader(configFile));
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(wFile));
        /* BufferedWriter out1 = new BufferedWriter(new FileWriter(wFile2)) */) {
            while ((tempString = in.readLine()) != null) {
                if (!tempString.contains("2016-12-30")) {
                    continue;
                }
                out.write(tempString.getBytes());
                out.write("\n".getBytes());
                // out1.write(tempString);
                // out1.newLine();
                count++;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.printf("count: %d", count);
    }

    public void processLog() {
        File configFile = new File("info.log");
        File wFile = new File("E:\\IdeaProject\\SparkRemoteDebug\\out.log");
        L.info("config file path: {}", configFile.getAbsolutePath());

        String tempString = null;
        long count = 0;
        int line = 0;
        System.out.println("=====start=====");

        String[] arrTemp = null;
        String[] data = null;
        String DevId = null;
        String MeteId = null;
        String OccurTime = null;
        String MeteType = null;

        try (BufferedReader in = new BufferedReader(new FileReader(configFile));
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(wFile))) {
            while ((tempString = in.readLine()) != null) {
                if (!tempString.contains("OccurTime")) {
                    continue;
                }
                arrTemp = tempString.split(" - ");
                data = arrTemp[1].replace(" ", "").split(",");
                DevId = data[0];
                MeteId = data[1];
                OccurTime = data[2];
                MeteType = data[3];

                out.write(DevId.getBytes());
                out.write(MeteId.getBytes());
                out.write(OccurTime.getBytes());
                out.write(MeteType.getBytes());
                out.write(",".getBytes());

                if (line >= 100) {
                    System.out.println("insert a new line");
                    out.write("\n".getBytes());
                    line = 0;
                }
                count++;
                line++;
            }

        } catch (Exception e) {
            System.out.println(count);
            System.out.println(arrTemp[1]);
            e.printStackTrace();
        }
        System.out.printf("count: %d", count);
    }

    public static void main(String[] args) {
        analyLog test = new analyLog();
        // test.processLog();
        test.fetchLogInfo();
    }

}

package com.znv.fss.ExcelToPhoenix;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.directory.api.util.Strings;
import org.apache.phoenix.shaded.jline.internal.InputStreamReader;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.znv.fss.ExcelToPhoenix.conf.ConfigManager;
import com.znv.fss.ExcelToPhoenix.constant.Constant;

/**
 * Created by User on 2018/4/2.
 */
public class ReadExcelUtilsGet {
    private static Logger L = LoggerFactory.getLogger(ReadExcelUtilsGet.class);
    private static Workbook wb;
    private static Sheet sheet;
    private static Row row;
    private static Connection conn = null;
    public static int colNum;
    public static AtomicInteger succCount = new AtomicInteger(0);
    public static AtomicInteger featureCount = new AtomicInteger(0);
    public static AtomicInteger errorCount = new AtomicInteger(0);
    public static int phoenixCount = 0;
    public static int recordCount = 0;
    public static String errorFileName;
    public static String errorFilePath;
    private static PrintStream ps;
    private static JSONObject featureJson = new JSONObject();
    private static float qualityThreshold;

    public static void init(String xlsPath) throws Exception {
        L.info("##### load excel file: {}", xlsPath);
        if (xlsPath == null) {
            L.error("\tcan't find excel file: {}", xlsPath);
            return;
        }
        String ext = xlsPath.substring(xlsPath.lastIndexOf(".")).toLowerCase();
        try {
            InputStream is = new FileInputStream(xlsPath);
            if (".xls".equals(ext)) {
                wb = new HSSFWorkbook(is);
            } else if (".xlsx".equals(ext)) {
                wb = new XSSFWorkbook(is);
            }
            L.error("\tload excel file OK!");
        } catch (Exception e) {
            L.error("\tload excel file faild: {}", xlsPath);
            L.error(e.getMessage(), e);
        }
    }


    /**
     * 创建文件
     *
     * @throws IOException
     */
    public static void creatTxtFile() throws IOException {
        String xlsxName = new File(Initial.xlsxPath).getName();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
        String dateStr = format.format(new Date());
        errorFileName = String.format("失败记录_%s_%s.txt", xlsxName, dateStr);
        errorFilePath = StringUtils.substringBeforeLast(Initial.xlsxPath, File.separator) + File.separator + errorFileName;
        L.info("##### create log file: {}", errorFilePath);
        File file = new File(errorFilePath);
        if (!file.exists()) {
            ps = new PrintStream(new FileOutputStream(file));
        }

        WriteStringToFile(String.format("##### XLSX: %s", Initial.xlsxPath));
        WriteStringToFile(
                String.format("##### CONF: %s, %s, %s", Initial.tableName, Initial.libId, Initial.personlibType));

        L.info("\tcreate log file OK!");
    }

    /**
     * 写文件
     *
     * @param info
     */
    public static void WriteStringToFile(String info) {
        if (!info.equals("")) {
            ps.append(info);// 在已有的基础上添加字符串
            ps.append("\n");
        }
    }


    /**
     * @param row 判断行为空
     * @return
     */
    public static boolean isRowEmpty(Row row) {
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK)
                return false;
        }
        return true;
    }

    // 读取文件夹下所有文件，包括子目录下的文件
    public static List<String> listALlFile(File fileDir, List<String> picFilepath) {

        if (fileDir != null) {
            File[] filelist = fileDir.listFiles();

            for (File file : filelist) {
                if (file.isFile()) {
                    picFilepath.add(file.getAbsolutePath());
                } else if (file.isDirectory()) {
                    listALlFile(file, picFilepath);
                }
            }
        }

        return picFilepath;
    }

    // 开线程批量获取特征值
    public static JSONObject getFeatureBatch(String picPath) throws IOException {
        File filefir = new File(picPath); // 图片路径
        File[] filelist = filefir.listFiles();

        List<String> picpaths = new ArrayList<>();

        for (File file : filelist) {
            if (file.isFile()) {
                picpaths.add(file.getAbsolutePath());
            }
        }
        ExecutorService getFeatureThread = Executors.newFixedThreadPool(10);

        //获取人脸质量分数阈值
        qualityThreshold = Float.parseFloat(ConfigManager.getString(Constant.QUALITY_THRESHOLD));

        int batchNum = Integer.parseInt(ConfigManager.getString(Constant.BATCHNUM));
        int totalSize = picpaths.size();


        L.info("batchNum: {}, totalSize: {}", batchNum, totalSize);

        String urlBathFeature = ConfigManager.getString(Constant.IP) + ":" + ConfigManager.getString(Constant.BATCHPORT)
                + "/verify/feature/batchGet";

        L.info("face server url: {}", urlBathFeature);

        for (int i = 0; i < Math.ceil((float) totalSize / batchNum); i++) {
            final int k = i;
            getFeatureThread.execute(new Runnable() {
                @Override
                public void run() {
                    JSONObject nameJson = new JSONObject();
                    HashMap<String, String> nameMap = new HashMap<String, String>();
                    for (int j = batchNum * k; j < batchNum * (k + 1) && j < totalSize; j++) {
                        String uuidName = UUID.randomUUID().toString();
                        nameMap.put(picpaths.get(j).replaceAll("\\\\", "\\\\\\\\"), uuidName);
                        nameJson.put(uuidName, picpaths.get(j).replaceAll("\\\\", "\\\\\\\\"));
                    }

                    JSONObject batchJson = GetDataFeature.getImageBatchFeature2(nameMap, urlBathFeature);

                    // 取成功获取到特征值的数据
                    if (batchJson != null && batchJson.getString("result").equals("success")) {
                        JSONArray successFeatureArray = batchJson.getJSONArray("success");
                        for (int i = 0; i < successFeatureArray.size(); i++) {

                            // 获取json，格式为 "图片名"："特征"
                            JSONObject object = successFeatureArray.getJSONObject(i);

                            // 通过uuid名获取对应的文件路径名，
                            String filePathName = nameJson.getString(object.getString("name"));

                            // 只保留文件名
                            filePathName = filePathName.substring(filePathName.lastIndexOf(File.separator) + 1);

                            if (object.containsKey("quality") && object.getFloatValue("quality") < qualityThreshold) {
                                featureJson.put(filePathName, "lowQuality" + object.getFloatValue("quality"));
                            } else {
                                featureJson.put(filePathName, object.getString("feature"));
                            }

                            int count = featureCount.addAndGet(1);
                            if (count % 1000 == 0) {
                                L.info("get feature count: {}", count);
                            }
                        }
                    }
                }
            });
        }

        getFeatureThread.shutdown();
        while (true) {
            if (getFeatureThread.isTerminated()) {
                break;
            }
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                L.error(e.getMessage(), e);
            }

        }
        return featureJson;
    }


    /**
     * 读取Excel数据内容
     */
    public static void readExcelContent(JSONObject featureJson) throws Exception {
        if (wb == null) {
            throw new Exception("Workbook对象为空！");
        }

        sheet = wb.getSheetAt(0);
        recordCount = sheet.getLastRowNum();
        L.info("\texcel row count: {}", recordCount);
        row = sheet.getRow(0);
        colNum = row.getPhysicalNumberOfCells();

        // 创建保存错误信息的txt文件
        creatTxtFile();

        L.info("##### send to phoenix...");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currDate = sdf.format(new Date());

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        String startDate = sdf.format(calendar.getTime());

        calendar.add(Calendar.YEAR, 10);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 50);
        String endDate = sdf.format(calendar.getTime());

        L.info("\tcreate time: {}", currDate);
        L.info("\tstart time: {}", startDate);
        L.info("\tend time: {}", endDate);

        WriteStringToFile(String.format("##### DATE: %s, %s, %s", currDate, startDate, endDate));

        // 正文内容应该从第二行开始,第一行为表头的标题
        try {
            int idx = 0;
            for (int k = 1; k <= recordCount; k++) {
                boolean birthFlag = true; // 若生日格式错误，不写进phoenix；默认为true，生日正常。
                boolean picFlag = true; // 若没有图片字段或者图片，不写进phoenix；默认为true，图片正常。
                String picPath = null;
                row = sheet.getRow(k);
                if (isRowEmpty(row)) {
                    continue;
                }

                int pic = 0;
                while (pic < colNum) {
                    String name = sheet.getRow(0).getCell(pic).toString();
                    if ((Initial.xmlMap1.containsKey(name)) && (Initial.xmlMap1.get(name).equals("person_img"))) {
                        picPath = getCellFormatValue(row.getCell(pic)).toString();
                        break;
                    }
                    pic++;
                }

                // 提示没有图片
                if (picPath == null || picPath.equals("")) {
                    WriteStringToFile(String.format("%s: 人脸图片名称为空", k));
                    errorCount.addAndGet(1);
                    continue;
                }

                JSONObject json = new JSONObject(true);
                long t1 = System.currentTimeMillis();
                String personIdStr = String.format("%13d%3d", t1, idx++).replace(" ", "0");
                idx++;
                if (idx > 999) {
                    idx = 0;
                }
                json.put("person_id", personIdStr);

                int j = 0;
                Cell cell = row.getCell(j);
                while (j < colNum && cell != null && picFlag && birthFlag) {
                    if (!picFlag || !birthFlag) {
                        break;
                    }

                    String name = sheet.getRow(0).getCell(j).toString(); // 对特殊字符处理，如姓名

                    for (Element element : Initial.elements) {
                        if (!picFlag || !birthFlag) {
                            break;
                        }

                        if (!element.attribute("xls_index").getValue().equals(name)) {
                            continue;
                        }

                        String value = element.attribute("hcolname").getValue();
                        if (element.attribute("isPicture").getValue().equals("1")) {
                            String faceName = (String) getCellFormatValue(row.getCell(j));
                            if (!faceName.endsWith(".jpg")) {
                                faceName = faceName + ".jpg";

                            }
                            // 图片名
                            json.put("image_name", faceName);

                            String facePath = Initial.picPath + File.separator + faceName;
                            if (!new File(facePath).exists()) {
                                // 找不到图片路径
                                picFlag = false;
                                WriteStringToFile(String.format("%s: 找不到图片文件，图片路径: %s", k, facePath));
                                break;
                            }

                            byte[] personImg = PictureUtils.image2byte(facePath);
                            json.put(value, personImg);

                            if (!Strings.isNotEmpty(element.attribute("featureColName").getValue())) {
                                // 未配置featureColName字段
                                picFlag = false;
                                WriteStringToFile(String.format("%s: 未配置featureColName字段", k));
                                break;
                            }

                            if (featureJson.containsKey(faceName)) {
                                //TODO 判断是否为低质量照片
                                String featureOrlowQuality = featureJson.getString(faceName);

                                if (featureOrlowQuality.startsWith("lowQuality")) {
                                    //当前图片质量分数
                                    float score = 0.0f;
                                    try {
                                        score = Float.valueOf(featureOrlowQuality.substring(10));
                                    } catch (Exception e) {
                                        System.out.println(e);
                                    }
                                    //图片质量低
                                    picFlag = false;
                                    WriteStringToFile(String.format("%s: 图片质量低于阈值( %f < %f)，图片路径: %s", k, score, qualityThreshold, facePath));
                                } else {
                                    byte[] feature = Base64.decodeBase64(featureOrlowQuality);
                                    json.put(element.attribute("featureColName").getValue(), feature);
                                }
                            } else {
                                // 无法获取特征
                                picFlag = false;
                                WriteStringToFile(String.format("%s: 无法获取特征值，图片路径: %s", k, facePath));
                                break;
                            }
                        } else if (value.equals("person_name")) {
                            String object = (String) getCellFormatValue(row.getCell(j));
//                            if (object.trim().equals("")) {
//                                object = "未登记";
//                            }
                            json.put(value, object);
                        } else if (value.equals("birth")) {
                            String object = (String) getCellFormatValue(row.getCell(j));
                            String birth = checkBirth(object);

                            if (!birth.equals("false")) {
                                json.put(value, birth);
                            } else {
                                // 生日格式错误
                                birthFlag = false;
                                WriteStringToFile(String.format("%s: 生日格式错误", k));
                                break;
                            }

                        } else if (value.equals("sex")) {
                            //TODO ###################################
                            Object object = getCellFormatValue(row.getCell(j));
                            if (object.equals("男") || object.equals("男性") || object.equals("1")) {
                                json.put(value, 1);
                            } else if (object.equals("女") || object.equals("女性") || object.equals("0")) {
                                json.put(value, 0);
                            }
                        } else {
                            if (element.attribute("type").getValue().equals("int")) {
                                if (Strings.isNotEmpty((String) getCellFormatValue(row.getCell(j)))) {
                                    int object = (int) Float.parseFloat((String) getCellFormatValue(row.getCell(j)));
                                    json.put(value, object);
                                } else {
                                    json.put(value, null);
                                }
                            } else if (element.attribute("type").getValue().equals("float")) {
                                if (Strings.isNotEmpty((String) getCellFormatValue(row.getCell(j)))) {
                                    float object = Float.parseFloat((String) getCellFormatValue(row.getCell(j)));
                                    json.put(value, object);
                                } else {
                                    json.put(value, null);
                                }
                            } else {
                                String object = (String) getCellFormatValue(row.getCell(j));
                                json.put(value, object);
                            }
                        }
                        break;
                    }
                    j++;
                }

                if (!picFlag || !birthFlag) {
                    int errorTotal = errorCount.addAndGet(1);
                    if (errorTotal % 200 == 0) {
                        L.error("\t--- error count：{}", errorTotal);
                    }
                    continue;
                }

                // 增加默认字段
                json.put("personlib_type", Initial.personlibType);
                json.put("lib_id", Initial.libId);
                json.put("flag", Initial.flag);

//                if (!json.containsKey("person_name")) {
//                    json.put("person_name", "未登记");
//                }
//                if (!json.containsKey("country")) {
//                    json.put("country", "中国");
//                }
//                if (!json.containsKey("nation")) {
//                    json.put("nation", "汉");
//                }
//                if (!json.containsKey("sex")) {
//                    json.put("sex", 1);
//                }

                if (!json.containsKey("door_open")) {
                    json.put("door_open", 0);
                }
                if (!json.containsKey("birth")) {
                    json.put("birth", currDate);
                }


                //TODO ################### start ####################
                json.put("control_start_time", startDate);
                json.put("control_end_time", endDate);
                json.put("is_del", "0");
                json.put("create_time", currDate);
                json.put("modify_time", currDate);

                json.put("control_community_id", "1");
                json.put("control_person_id", "2");
                json.put("control_event_id", "2");  // 默认事件
                //TODO ################### end ####################


                StringBuilder insertSql = new StringBuilder();

                insertSql.append("UPSERT INTO " + Initial.tableName).append("(");
                for (String keys : json.keySet()) {
                    insertSql.append(keys + ",");
                }
                if (insertSql.charAt(insertSql.length() - 1) == ',') {
                    insertSql.deleteCharAt(insertSql.length() - 1);
                }
                insertSql.append(") VALUES(");
                for (@SuppressWarnings("unused")
                        String keys : json.keySet()) {
                    insertSql.append("?,");
                }
                if (insertSql.charAt(insertSql.length() - 1) == ',') {
                    insertSql.deleteCharAt(insertSql.length() - 1);
                }
                insertSql.append(")");

                if (conn == null) {
                    try {
                        conn = Initial.connectionPool.getConnection();
                    } catch (Exception e) {
                        L.error(e.getMessage(), e);
                    }
                }

                int m = 1;
                try (PreparedStatement stat = conn.prepareStatement(insertSql.toString())) {
                    for (Entry<String, Object> entry : json.entrySet()) {
                        stat.setObject(m, entry.getValue());
                        m++;
                    }
                    stat.executeUpdate(); // object数据写入到phoenix

                    phoenixCount = succCount.addAndGet(1);
                    if (phoenixCount % 1000 == 0) {
                        L.info("\t+++ succ count：{}", phoenixCount);
                        System.out.println("data num：" + phoenixCount);
                    }
                } catch (Exception e) {
                    L.error(e.getMessage(), e);
                    errorCount.addAndGet(1);
                } finally {
                    if (null != conn) {
                        Initial.connectionPool.returnConnection(conn);
                    }
                }

            }
        } catch (Exception e) {
            L.error(e.getMessage(), e);
        }

        phoenixCount = succCount.get();
        L.info("##### record count: {}, succ count: {}, error count: {}", recordCount, succCount.get(),
                errorCount.get());
    }

    /**
     * 根据Cell类型设置数据
     *
     * @param cell
     * @return
     * @author zengwendong
     */

    private static Object getCellFormatValue(Cell cell) {
        Object cellvalue = null;
        if (cell != null) {
            // 判断当前Cell的Type
            switch (cell.getCellType()) {
                case Cell.CELL_TYPE_NUMERIC: { // 如果当前Cell的Type为数字
                    if (DateUtil.isCellDateFormatted(cell)) {
                        Date date = cell.getDateCellValue();
                        cellvalue = date.toString();
                    } else {
                        DecimalFormat df = new DecimalFormat("0");
                        cellvalue = df.format(cell.getNumericCellValue());
                    }
//                    DecimalFormat df = new DecimalFormat("0");
//                    cellvalue = df.format(cell.getNumericCellValue());
                    break;
                }
                case Cell.CELL_TYPE_FORMULA: { // 公式
                    // 判断当前的cell是否为Date
                    if (DateUtil.isCellDateFormatted(cell)) {
                        // 如果是Date类型则，转化为Data格式
                        // data格式是带时分秒的：2013-7-10 0:00:00
                        // cellvalue = cell.getDateCellValue().toLocaleString();
                        // data格式是不带带时分秒的：2013-7-10
                        Date date = cell.getDateCellValue();
                        cellvalue = date;
                    } else { // 如果是纯数字
                        // 取得当前Cell的数值
                        cellvalue = String.valueOf(cell.getNumericCellValue());
                    }
                    break;
                }
                case Cell.CELL_TYPE_STRING:// 如果当前Cell的Type为字符串
                    // 取得当前的Cell字符串
                    cellvalue = cell.getRichStringCellValue().getString();
                    break;
                default:// 默认的Cell值
                    cellvalue = "";
            }
        } else {
            cellvalue = "";
        }
        return cellvalue;
    }

    private static void ExcelToPhoenix(JSONObject featureBatchJson) {
        try {
            // 对读取Excel表格内容测试
            ReadExcelUtilsGet.init(Initial.xlsxPath);
            ReadExcelUtilsGet.readExcelContent(featureBatchJson);
        } catch (Exception e) {
            L.error(e.getMessage(), e);
        }
    }

    /**
     * 校验出生日期
     *
     * @param birthString
     * @return
     */
    public static String checkBirth(String birthString) {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        final SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyyMMdd");
        final SimpleDateFormat dateFormat3 = new SimpleDateFormat("yyyy/MM/dd");
        final SimpleDateFormat dateFormat4 = new SimpleDateFormat("yyyy年MM月dd日");
        try {
            Date birth = dateFormat.parse(birthString.trim());
            return dateFormat.format(birth);
        } catch (Exception e) {
            try {
                Date birth = dateFormat2.parse(birthString.trim());
                return dateFormat.format(birth);
            } catch (Exception e2) {
                try {
                    Date birth = dateFormat3.parse(birthString.trim());
                    return dateFormat.format(birth);
                } catch (Exception e3) {
                    try {
                        Date birth = dateFormat4.parse(birthString.trim());
                        return dateFormat.format(birth);
                    } catch (Exception e4) {
                        try {
                            Date birth = new Date(birthString);
                            return dateFormat.format(birth);
                        } catch (Exception e5) {
                            return "false";
                        }
                    }
                }
            }
        }
    }

    public static void start() throws Exception {
        File featureFile = new File(Initial.picPath + File.separator + "feature.json");
        L.info("##### load feature file: {}", featureFile);
        JSONObject json = null;
        if (featureFile.exists() && featureFile.length() > 0) {
            L.info("\tfeature file exists ...");
            json = new JSONObject();
            try (FileInputStream fis = new FileInputStream(featureFile);
                 InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
                 BufferedReader br = new BufferedReader(isr)) {
                L.info("\tstart read...");
                String line = br.readLine();
                L.info("\tparse to json object...");
                json = JSON.parseObject(line);
                L.info("\tload feature file OK!");
            } catch (Exception e) {
                L.error(e.getMessage(), e);
            }
        } else {
            L.info("\tfeature file not exists...");
            L.info("\tstart get feature...");
            json = getFeatureBatch(Initial.picPath); //批量获取特征
            try (PrintStream ps = new PrintStream(new FileOutputStream(featureFile))) {
                L.info("\tjson to string...");
                String feature = json.toJSONString();
                L.info("\twrite to file...");
                ps.append(feature);
                ps.flush();
                L.info("\tload feature file OK!");
            } catch (Exception e) {
                L.error(e.getMessage(), e);
            }
        }

        L.info("##### load all feature count: {}", json.size());

        ExcelToPhoenix(json);
    }
}

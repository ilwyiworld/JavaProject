package org.elasticsearch.plugin.image.test;

/**
 * Created by Administrator on 2017/5/5.
 */

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class Write2Excel {

    private static Logger LOGGER = LogManager.getLogger(Write2Excel.class.getName());

    public static void main(String[] args) {
        String path = "E://demo.xlsx";
        String name = "test";
        List<String> titles = new ArrayList();
        titles.add("id");
        titles.add("name");
        titles.add("age");
        titles.add("birthday");
        titles.add("gender");
        titles.add("date");
        List<Map<String, Object>> values = new ArrayList();
        for (int i = 0; i < 10; i++) {
            Map<String, Object> map = new HashMap();
            map.put("id", i + 1D);
            map.put("name", "test_" + i);
            map.put("age", i * 1.5);
            map.put("gender", "man");
            map.put("birthday", new Date());
            map.put("date", Calendar.getInstance());
            values.add(map);
        }
        //   System.out.println(writerExcel(path, name, titles, values));
    }

    /**
     * 数据写入Excel文件
     *
     * @param path   文件路径，包含文件全名，例如：D://file//demo.xls
     * @param name   sheet名称
     * @param titles 行标题列
     * @param values 数据集合，key为标题，value为数据
     * @return True\False
     */
    public static boolean writerExcel(String path, String name, List<String> titles, List<Map<String, Object>> values) {
        LOGGER.info("path : {}", path);
        String style = path.substring(path.lastIndexOf("."), path.length()).toUpperCase(); // 从文件路径中获取文件的类型
        return generateWorkbook(path, name, style, titles, values);
    }

    /**
     * 将数据写入指定path下的Excel文件中
     *
     * @param path   文件存储路径
     * @param name   sheet名
     * @param style  Excel类型
     * @param titles 标题串
     * @param values 内容集
     * @return True\False
     */
    private static boolean generateWorkbook(String path, String name, String style, List<String> titles, List<Map<String, Object>> values) {
        LOGGER.info("file style : {}", style);
        HSSFWorkbook workbook = new HSSFWorkbook();
        /*if ("XLS".equals(style.toUpperCase())) {
            workbook = new HSSFWorkbook();
        } else {
            workbook = new XSSFWorkbook();
        }*/
        // 生成一个表格
        HSSFSheet sheet;
        if (null == name || "".equals(name)) {
            sheet = workbook.createSheet(); // name 为空则使用默认值
        } else {
            sheet = workbook.createSheet(name);
        }
        // 设置表格默认列宽度为15个字节
        sheet.setDefaultColumnWidth((short) 15);
        // 生成样式
        Map<String, CellStyle> styles = createStyles(workbook);
        /*
         * 创建标题行
         */
        Row row = sheet.createRow(0);
        // 存储标题在Excel文件中的序号
        Map<String, Integer> titleOrder = new HashMap<>();
        for (int i = 0; i < titles.size(); i++) {
            Cell cell = row.createCell(i);
            cell.setCellStyle(styles.get("header"));
            String title = titles.get(i);
            cell.setCellValue(title);
            titleOrder.put(title, i);
        }
        /*
         * 写入正文
         */
        Iterator<Map<String, Object>> iterator = values.iterator();
        int index = 0; // 行号
        while (iterator.hasNext()) {
            index++; // 出去标题行，从第一行开始写
            row = sheet.createRow(index);
            Map<String, Object> value = iterator.next();
            for (Map.Entry<String, Object> map : value.entrySet()) {
                // 获取列名
                String title = map.getKey();
                // 根据列名获取序号
                int i = titleOrder.get(title);
                // 在指定序号处创建cell
                Cell cell = row.createCell(i);
                // 设置cell的样式
                if (index % 2 == 1) {
                    cell.setCellStyle(styles.get("cellA"));
                } else {
                    cell.setCellStyle(styles.get("cellB"));
                }
                // 获取列的值
                Object object = map.getValue();
                // 判断object的类型
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                if (object instanceof Double) {
                    cell.setCellValue((Double) object);
                } else if (object instanceof Date) {
                    String time = simpleDateFormat.format((Date) object);
                    cell.setCellValue(time);
                } else if (object instanceof Calendar) {
                    Calendar calendar = (Calendar) object;
                    String time = simpleDateFormat.format(calendar.getTime());
                    cell.setCellValue(time);
                } else if (object instanceof Boolean) {
                    cell.setCellValue((Boolean) object);
                } else {
                    cell.setCellValue(object.toString());
                }
            }
        }
        /*
         * 写入到文件中
         */
        boolean isCorrect = false;
        try {
            File file = new File(path);
            OutputStream outputStream = new FileOutputStream(file);
            workbook.write(outputStream);
            outputStream.close();
            isCorrect = true;
        } catch (IOException e) {
            isCorrect = false;
            LOGGER.error("write Excel file error : {}", e.getMessage());
        }
//        try {
//            workbook.close();
//        } catch (IOException e) {
//            isCorrect = false;
//            LOGGER.error("workbook closed error : {}", e.getMessage());
//        }
        return isCorrect;
    }

    /**
     * Create a library of cell styles
     */
    /**
     * @param wb
     * @return
     */
    private static Map<String, CellStyle> createStyles(Workbook wb) {
        Map<String, CellStyle> styles = new HashMap<>();

        // 标题样式
        CellStyle titleStyle = wb.createCellStyle();
        titleStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER); // 水平对齐
        titleStyle.setVerticalAlignment(HSSFCellStyle.ALIGN_CENTER); // 垂直对齐
        titleStyle.setLocked(true); // 样式锁定
        titleStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        Font titleFont = wb.createFont();
        titleFont.setFontHeightInPoints((short) 16);
        //titleFont.setBold(true);
        titleFont.setFontName("微软雅黑");
        titleStyle.setFont(titleFont);
        styles.put("title", titleStyle);

        // 文件头样式
        CellStyle headerStyle = wb.createCellStyle();
        headerStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        headerStyle.setVerticalAlignment(HSSFCellStyle.ALIGN_CENTER);
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex()); // 前景色
        headerStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND); // 颜色填充方式
        headerStyle.setWrapText(true);
        headerStyle.setBorderRight(HSSFCellStyle.BORDER_THIN); // 设置边界
        headerStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
        headerStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        headerStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        headerStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
        headerStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
        headerStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        headerStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        Font headerFont = wb.createFont();
        headerFont.setFontHeightInPoints((short) 12);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        titleFont.setFontName("微软雅黑");
        headerStyle.setFont(headerFont);
        styles.put("header", headerStyle);

        Font cellStyleFont = wb.createFont();
        cellStyleFont.setFontHeightInPoints((short) 12);
        cellStyleFont.setColor(IndexedColors.BLUE_GREY.getIndex());
        cellStyleFont.setFontName("微软雅黑");

        // 正文样式A
        CellStyle cellStyleA = wb.createCellStyle();
        cellStyleA.setAlignment(HSSFCellStyle.ALIGN_CENTER); // 居中设置
        cellStyleA.setVerticalAlignment(HSSFCellStyle.ALIGN_CENTER);
        cellStyleA.setWrapText(true);
        cellStyleA.setBorderRight(HSSFCellStyle.BORDER_THIN);
        cellStyleA.setRightBorderColor(IndexedColors.BLACK.getIndex());
        cellStyleA.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        cellStyleA.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        cellStyleA.setBorderTop(HSSFCellStyle.BORDER_THIN);
        cellStyleA.setTopBorderColor(IndexedColors.BLACK.getIndex());
        cellStyleA.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        cellStyleA.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        cellStyleA.setFont(cellStyleFont);
        styles.put("cellA", cellStyleA);

        // 正文样式B:添加前景色为浅黄色
        CellStyle cellStyleB = wb.createCellStyle();
        cellStyleB.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        cellStyleB.setVerticalAlignment(HSSFCellStyle.ALIGN_CENTER);
        cellStyleB.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        cellStyleB.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        cellStyleB.setWrapText(true);
        cellStyleB.setBorderRight(HSSFCellStyle.BORDER_THIN);
        cellStyleB.setRightBorderColor(IndexedColors.BLACK.getIndex());
        cellStyleB.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        cellStyleB.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        cellStyleB.setBorderTop(HSSFCellStyle.BORDER_THIN);
        cellStyleB.setTopBorderColor(IndexedColors.BLACK.getIndex());
        cellStyleB.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        cellStyleB.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        cellStyleB.setFont(cellStyleFont);
        styles.put("cellB", cellStyleB);

        return styles;
    }

    /**
     * @param @param  picSheet
     * @param @param  pictureFilePaths
     * @param @param  cellRow
     * @param @param  cellCol
     * @param @throws Exception 设定文件
     * @return void 返回类型
     * @throws
     * @Title: addPictureToExcel
     * @Description: TODO(将多个图片按实际大小，插入同一个单元格, 最后一张图如果高度超过了单元格，则压缩高度使之在单元格内)
     * @date 2016年12月16日 下午6:13:52
     */
    /*private static void addPictureToExcel(Sheet picSheet, String[] pictureFilePaths, double cellRow, double cellCol)
            throws Exception {

        final double cellSpace = 0.02;//图片之间的间隔 占比

        double picWidthMax = 0;
        double picHeightSum =0;//空出图片 离上下边框的距离
        ImgFile[] imgFiles = new ImgFile[pictureFilePaths.length];

        for (int i=0;i<pictureFilePaths.length;i++) {
            ImgFile imgFile = new ImgFile();
            File imageFile = new File(pictureFilePaths[i]);
            imgFile.setImgFile(imageFile);
            // 读入图片
            BufferedImage picImage = ImageIO.read(imageFile);
            // 取得图片的像素高度，宽度
            double picWidth = picImage.getWidth() * 0.15;  //具体的实验值，原理不清楚。
            double picHeight = picImage.getHeight() * 15; //具体的实验值，原理不清楚。

            imgFile.setHeigth(picHeight);
            imgFile.setWidth(picWidth);
            //汇总
            if (picWidth > picWidthMax) {
                picWidthMax = picWidth;
            }
            picHeightSum += picHeight;
            imgFiles[i] = imgFile;
        }

        WritableFont font = new WritableFont(WritableFont.ARIAL,14,WritableFont.BOLD,false,UnderlineStyle.NO_UNDERLINE,Colour.RED);
        WritableCellFormat cellFormat = new WritableCellFormat(font);
        //设置背景颜色;
        cellFormat.setBackground(Colour.WHITE);
        //设置边框;
        cellFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
        //设置自动换行;
        cellFormat.setWrap(true);
        //设置文字居中对齐方式;
        cellFormat.setAlignment(Alignment.CENTRE);
        //设置垂直居中;
        cellFormat.setVerticalAlignment(VerticalAlignment.CENTRE);

        Label imageLabel = new Label((int)cellCol, (int)cellRow, "",cellFormat);
        picSheet.addCell(imageLabel);

        //设置单元格宽高
        picSheet.setColumnView((int)cellCol, (int)picWidthMax);//列宽
        picSheet.setRowView((int)cellRow, (int)picHeightSum);//行高

        double widthStart = cellSpace;//开始宽度
        double heightStart = cellSpace;//开始高度
        //插入图片
        for (ImgFile imgFile0: imgFiles) {
            double heigthFact = imgFile0.getHeigth()/picHeightSum;//实际高度
            double widthFact = imgFile0.getWidth()/picWidthMax;
            //图片高度压缩了cellSpace+moreHeight,目的是为了该图片高度不超出单元格
            if (heightStart + heigthFact >= 1) {
                double moreHeight = heightStart + heigthFact - 1.00;
                heigthFact -= moreHeight;
                heigthFact -= cellSpace;
            }
            //图片宽度压缩了cellSpace,目的是为了该图片宽度不超出单元格
            if (widthFact >= 1) {
                widthFact -= cellSpace;
            }
            //生成图片对象
            WritableImage image = new WritableImage(cellCol+widthStart, cellRow + heightStart,
                    widthFact, heigthFact, imgFile0.getImgFile());
            //将图片对象插入到sheet
            picSheet.addImage(image);
            //开始高度累加，获取下一张图片的起始高度（相对该单元格）
            heightStart += heigthFact;
            heightStart +=cellSpace;//图片直接间隔为cellSpace
        }
    }*/

    //自定义的方法,插入某个图片到指定索引的位置
    private static void insertImage(HSSFWorkbook wb, HSSFPatriarch pa, byte[] data, int row, int column, int index) {
        int x1 = index * 250;
        int y1 = 0;
        int x2 = x1 + 255;
        int y2 = 255;
        HSSFClientAnchor anchor = new HSSFClientAnchor(x1, y1, x2, y2, (short) column, row, (short) column, row);
        anchor.setAnchorType(2);
        pa.createPicture(anchor, wb.addPicture(data, HSSFWorkbook.PICTURE_TYPE_JPEG));
    }
}

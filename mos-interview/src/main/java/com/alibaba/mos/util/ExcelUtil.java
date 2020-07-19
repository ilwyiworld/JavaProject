package com.alibaba.mos.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.mos.api.ExcelReadHandler;
import com.alibaba.mos.data.ChannelInventoryDO;
import com.alibaba.mos.data.SkuDO;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.BuiltinFormats;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.util.SAXHelper;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExcelUtil {

    // 读取 Excel
    public static void readAllSheets(String filename, ExcelReadHandler excelReadHandler) {
        Iterator<InputStream> sheets = null;
        XMLReader parser = null;
        try {
            OPCPackage pkg = OPCPackage.open(filename);
            XSSFReader reader = new XSSFReader(pkg);
            SharedStringsTable sst = reader.getSharedStringsTable();
            StylesTable styleTable = reader.getStylesTable();
            parser = fetchSheetParser(sst, styleTable, excelReadHandler);
            sheets = reader.getSheetsData();
        } catch (Exception e) {
            throw new RuntimeException("读取 Excel 出错：", e);
        }

        while (sheets != null && sheets.hasNext()) {
            try (InputStream sheet = sheets.next()) {
                InputSource sheetSource = new InputSource(sheet);
                parser.parse(sheetSource);
            } catch (Exception e) {
                throw new RuntimeException("读取 Excel 中 sheet 表出错", e);
            }
        }
    }

    // Excel 转换为 SkuDO
    public static void excelToSkuDO(LinkedHashMap<String, String> row, SkuDO skuDO) {
        int index = 0;
        boolean isRow = false;
        for (String value : row.values()) {
            if (index == 0 && isInteger(value)) {
                isRow = true;
            }
            if (isRow) {
                switch (index) {
                    case 1:
                        skuDO.setName(value);
                        break;
                    case 2:
                        skuDO.setArtNo(value);
                        break;
                    case 3:
                        skuDO.setSpuId(value);
                        break;
                    case 4:
                        skuDO.setSkuType(value);
                        break;
                    case 5:
                        skuDO.setPrice(new BigDecimal(value));
                        break;
                    case 6:
                        List<ChannelInventoryDO> list = new ArrayList<>();
                        JSONArray jArray = JSON.parseArray(value);
                        for (int i = 0; i < jArray.size(); i++) {
                            JSONObject jobj = jArray.getJSONObject(i);
                            ChannelInventoryDO ciDo = new ChannelInventoryDO();
                            // Inventory 中加上 skuId
                            ciDo.setSkuId(skuDO.getId());
                            ciDo.setChannelCode(jobj.getString("channelCode"));
                            ciDo.setInventory(new BigDecimal(jobj.getString("inventory")));
                            list.add(ciDo);
                        }
                        skuDO.setInventoryList(list);
                        break;
                    default:
                        skuDO.setId(value);
                        break;
                }
            }
            index++;
        }
    }

    // 判断 String 是否int
    public static boolean isInteger(String input) {
        Matcher mer = Pattern.compile("^[0-9]+$").matcher(input);
        return mer.find();
    }

    // 解析 sheet
    public static XMLReader fetchSheetParser(SharedStringsTable sst, StylesTable styleTable, ExcelReadHandler excelReadHandler) throws Exception {
        XMLReader parser = SAXHelper.newXMLReader();
        // XMLReader parser = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
        ContentHandler handler = new SheetHandlerUtil(sst, styleTable, excelReadHandler);
        parser.setContentHandler(handler);
        return parser;
    }

    private static class SheetHandlerUtil extends DefaultHandler {
        // 单元格中的数据可能的数据类型
        enum CellDataType {
            BOOL, ERROR, FORMULA, INLINESTR, SSTINDEX, NUMBER, DATE, NULL
        }

        private final DataFormatter formatter = new DataFormatter();

        // Excel 中，若单元格内的内容是字符串，那么这些字符串都存在这个变量中
        private SharedStringsTable sst;

        // 用于获取时间类型单元格的时间格式
        private StylesTable styleTable;

        // 当前单元格的内容
        private String currentContents;

        // 当前单元格的位置
        private String ref;

        // 当前单元格的类型
        private CellDataType cellDataType;

        // 当前单元格为时间时的格式索引
        private short formatIndex;

        // 当前单元格为时间时的格式
        private String formatString;

        private LinkedHashMap<String, String> result = new LinkedHashMap<>();

        // 读取一行的回调
        private ExcelReadHandler excelReadHandler;

        private SheetHandlerUtil(SharedStringsTable sst, StylesTable styleTable, ExcelReadHandler excelReadHandler) {
            this.sst = sst;
            this.styleTable = styleTable;
            this.excelReadHandler = excelReadHandler;
        }

        // 这个方法在遇到一个 xml 文件的元素开始之前被触发，取出单元格内存放的内容的类型
        @Override
        public void startElement(String uri, String localName, String name,
                                 Attributes attributes) throws SAXException {
            // name 为 c 表示遇到了单元格
            if (name.equals("c")) {
                ref = attributes.getValue("r");
                setNextDataType(attributes);
            }
            // 即将获取单元格的内容，所以置空该变量
            currentContents = "";
        }


        // 处理数据类型
        private void setNextDataType(Attributes attributes) {
            // cellType 为空，则表示该单元格类型为数字
            cellDataType = CellDataType.NUMBER;
            formatIndex = -1;
            formatString = null;
            // 单元格类型
            String cellType = attributes.getValue("t");
            String cellStyleStr = attributes.getValue("s");
            if ("b".equals(cellType)) {
                // 处理布尔值
                cellDataType = CellDataType.BOOL;
            } else if ("e".equals(cellType)) {
                // 处理错误
                cellDataType = CellDataType.ERROR;
            } else if ("inlineStr".equals(cellType)) {
                cellDataType = CellDataType.INLINESTR;
            } else if ("s".equals(cellType)) {
                // 处理字符串
                cellDataType = CellDataType.SSTINDEX;
            } else if ("str".equals(cellType)) {
                cellDataType = CellDataType.FORMULA;
            }
            if (cellStyleStr != null) {
                // 处理日期
                int styleIndex = Integer.parseInt(cellStyleStr);
                XSSFCellStyle style = styleTable.getStyleAt(styleIndex);
                formatIndex = style.getDataFormat();
                formatString = style.getDataFormatString();

                if (formatString == null) {
                    cellDataType = CellDataType.NULL;
                    formatString = BuiltinFormats.getBuiltinFormat(formatIndex);
                }

                if (formatString.contains("m/d/yy")) {
                    cellDataType = CellDataType.DATE;
                    formatString = "yyyy-MM-dd hh:mm:ss";
                }
            }
        }

        private String getDataValue(String value) {
            String thisStr;
            switch (cellDataType) {
                case BOOL:
                    char first = value.charAt(0);
                    thisStr = first == '0' ? "FALSE" : "TRUE";
                    break;
                case ERROR:
                    thisStr = "\"ERROR:" + value.toString() + '"';
                    break;
                case FORMULA:
                    thisStr = '"' + value.toString() + '"';
                    break;
                case INLINESTR:
                    XSSFRichTextString rtsi = new XSSFRichTextString(value);
                    thisStr = rtsi.toString();
                    break;
                case SSTINDEX:
                    String sstIndex = value.toString();
                    try {
                        int idx = Integer.parseInt(sstIndex);
                        // 根据 idx 索引值获取内容值
                        XSSFRichTextString rtss = new XSSFRichTextString(sst.getEntryAt(idx));
                        thisStr = rtss.toString();
                        rtss = null;
                    } catch (NumberFormatException ex) {
                        thisStr = value.toString();
                    }
                    break;
                case NUMBER:
                    if (formatString != null) {
                        thisStr = formatter.formatRawCellContents(Double.parseDouble(value), formatIndex, formatString).trim();
                    } else {
                        thisStr = value;
                    }
                    thisStr = thisStr.replace("_", "").trim();
                    break;
                case DATE:
                    thisStr = formatter.formatRawCellContents(Double.parseDouble(value), formatIndex, formatString);
                    thisStr = thisStr.replace("T", " ");
                    break;
                default:
                    thisStr = " ";
                    break;
            }
            return thisStr;
        }

        // 存储当前单元格的内容
        @Override
        public void characters(char[] ch, int start, int length) {
            currentContents = new String(ch, start, length);
        }

        // 读取完单元格的内容后被执行
        @Override
        public void endElement(String uri, String localName, String name) throws SAXException {
            if (name.equals("v")) {
                result.put(ref, getDataValue(currentContents));
            }
            if (name.equals("row")) {
                excelReadHandler.processOneRow(result);
                result.clear();
            }
        }
    }
}

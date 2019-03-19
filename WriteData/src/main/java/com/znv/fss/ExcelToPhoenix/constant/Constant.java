package com.znv.fss.ExcelToPhoenix.constant;

/**
 * Created by ct on 2017/5/26.
 */
public class Constant {
    public static final String ZOOKEEPER_ADDR = "zookeeper.connect";
    public static final String PHOENIX_DRIVER = "pheonix.driver";

    // 通知到kafka的配置
    public static final String NOTIFY_TOPIC_MSGTYPE = "fss.kafka.topic.blacklistchange.msgtype";
    public static final String NOTIFY_PARTITION_NUM = "fss.kafka.topic.blacklistchange.partition.num";
    public static final String NOTIFY_REPLICATION_NUM = "fss.kafka.topic.blacklistchange.replication.num";

    // schema名称
    public static final String FSS_SDK_SCHEMA_NAME = "fss.phoenix.schema.name";
    // 名单库
    public static final String FSS_PERSONLIST_V113_TABLE_NAME = "fss.phoenix.table.blacklist.name";

    // 初始化连接池数量
    public static final String SDK_CONNECTION_INITIAL_SIZE = "fss.sdk.pheonix.initialSize";
    // 连接池最大数量
    public static final String SDK_CONNECTION_MAX_SIZE = "fss.sdk.pheonix.maxActive";

    public static final String LIBID = "libId";
    public static final String PERSONLIB_TYPE = "personlib_type";
    public static final String FLAG = "flag";
    // 商汤的ip
    public static final String IP = "face.server.ip";
    public static final String PORT = "face.server.port";
    public static final String BATCHPORT = "face.server.batchPort";
    public static final String BATCHNUM = "batchNum";

    public static final String IMPORT_PIC_PATH = "pic_path";
    public static final String IMPORT_XLSX_PATH = "xlsx_path";
    public static final String IMPORT_XML_PATH = "import.xml.path";

    public static final String QUALITY_THRESHOLD = "face.quality.score.threshold";
}

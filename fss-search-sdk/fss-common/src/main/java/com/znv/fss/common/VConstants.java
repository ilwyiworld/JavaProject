package com.znv.fss.common;

import org.apache.hadoop.hbase.util.Bytes;

/**
 * Created by Administrator on 2016/8/9.
 */
public class VConstants {
    //服务信息
    public static final String PHOENIX_DRIVER = "pheonix.driver";
    public static final String ZOOKEEPER_QUORUM = "zookeeper.quorum";
    public static final String ZOOKEEPER_CLIENTPORT = "zookeeper.clientPort";
    public static final String HBASE_ZNODE_PARENT = "hbase.zookeeper.znode.parent";
    public static final String FSS_PHOENIX_SCHEMA_NAME = "fss.phoenix.schema.name";
    // 名单库-迭代三
    public static final String FSS_PERSONLIST_V113_TABLE_NAME = "fss.phoenix.table.blacklist.name";
    // 历史表-迭代三
    public static final String FSS_HISTORY_V113_TABLE_NAME = "fss.phoenix.table.history.name";

    //es
    public static final String ES_SERVER_IP = "es.server.ip";
    public static final String ES_HTTP_PORT = "es.http.port";
    public static final String FSS_ES_INDEX_HISTORY_NAME = "fss.es.search.history.alias";
    public static final String FSS_ES_INDEX_HISTORY_TYPE = "fss.es.index.history.type";
    public static final String FSS_ES_INDEX_HISTORY_Prefix = "fss.es.index.history.prefix";
    public static final String FSS_ES_SEARCH_TEMPLATE_FACESEARCH_ID = "fss.es.search.template.facesearch.id";
    public static final String FSS_ES_SEARCH_TEMPLATE_FLOWCOUNT_ID = "fss.es.search.template.flowCount.id";
    public static final String FSS_ES_SEARCH_TEMPLATE_FASTFEATURE_ID = "fss.es.search.template.fastsearch.id";

    public static final String FSS_ES_SEARCH_TEMPLATE_PERSONLIST_COUNT_ID = "fss.es.search.template.personlist.count.id";
    public static final String FSS_ES_SEARCH_TEMPLATE_HISTORY_PERSON_COUNT_ID = "fss.es.search.template.historyperson.count.id";
    public static final String FSS_ES_SEARCH_TEMPLATE_ALARM_PERSON_COUNT_ID = "fss.es.search.template.alarmperson.count.id";

    public static final String PHEONIX_SCHEEMA_ISENABLED = "phoenix.schema.isNamespaceMappingEnabled";
    public static final String FACE_SERVER_IP = "face.server.ip";
    public static final String FACE_SERVER_PORT = "face.server.port";
    public static final String INDEX_PERSON_LIST_NAME = "fss.es.index.person.list.name";
    public static final String INDEX_PERSON_LIST_TYPE = "fss.es.index.person.list.type";
    public static final String FSS_ES_SEARCH_TEMPLATE_PERSONLIST_ID = "fss.es.search.template.personlist.id";
    public static final String ES_SEARCH_POOL_NUM = "fss.es.search.pool.num";
    public static final String INDEX_ALARM_NAME = "fss.es.index.alarm.name";
    public static final String INDEX_ALARM_TYPE = "fss.es.index.alarm.type";
    public static final String FSS_ES_SEARCH_TEMPLATE_ALARM_SEARCH_ID = "fss.es.search.template.alarmsearch.id";

    public static final String INDEX_LOG_NAME = "fss.es.index.log.name";
    public static final String INDEX_LOG_TYPE = "fss.es.index.log.type";
    //多索引分类搜索添加
    public static final String ES_CLUSTER_NAME = "es.cluster.name";
    public static final String INDEX_EXACT_SEARCH_RESULT= "fss.es.index.exact.search.result";
    //hbase
    public static final String HBASE_SEARCH_POOL_NUM = "fss.hbase.search.threadpool.num";
    public static final String WRITE_POOL_NUM = "fss.hbase.write.threadpool.num";
    public static final String READ_POOL_NUM = "fss.hbase.read.threadpool.num";
    public static final String HBASE_RPC_TIMEOUT = "hbase.rpc.timeout";
    public static final String HBASE_CLIENT_OPERATION_TIMEOUT = "hbase.client.operation.timeout";
    public static final String HBASE_CLIENT_SCANNER_TIMEOUT = "hbase.client.scanner.timeout.period";
    public static final String HBASE_CLIENT_RETRIES_NUMBER = "hbase.client.retries.number";
    public static final String HISTORY_SALT_BUCKETS = "fss.phoenix.table.history.saltbuckets";
    // 大图表名
    public static final String FSS_BIGPICTURE_V113_TABLE_NAME = "fss.phoenix.table.bigpic.name";
    public static final String PIC_SALT_BUCKETS = "fss.phoenix.table.bigpic.saltbuckets";

    //phoenix
    // 通知到kafka的配置
    public static final String NOTIFY_TOPIC_MSGTYPE = "fss.kafka.topic.blacklistchange.msgtype";
    public static final String ZOOKEEPER_ADDR = "zookeeper.connect";
    public static final String NOTIFY_PARTITION_NUM = "fss.kafka.topic.blacklistchange.partition.num";
    public static final String NOTIFY_REPLICATION_NUM = "fss.kafka.topic.blacklistchange.replication.num";
    // 名单库-迭代三
    public static final String FSS_PERSONLIST_MAX_NUM = "fss.sdk.phoenix.personlist.max.num";
    // 单个摄像头最大字库匹配量-迭代三
    public static final String FSS_CAMERA_MAX_NUM = "fss.sdk.phoenix.camera.max.size";
    // 配置表-迭代三
    public static final String FSS_LIB_CONFIG_V113_TABLE_NAME = "fss.phoenix.table.libconfig.name";
    // 布控表-迭代三
    public static final String FSS_CAMERA_LIB_V113_TABLE_NAME = "fss.phoenix.table.cameralib.name";
    // 人员关系表-迭代三
    public static final String FSS_RELATIONSHIP_V113_TABLE_NAME = "fss.phoenix.table.relationship.name";
    // 告警表-迭代三
    public static final String FSS_AIALRM_V113_TABLE_NAME = "fss.phoenix.table.alarm.name";
    // 名单库-迭代三
    public static final String QUERY_FSS_PERSONLIST_V113 = "31001";
    // 名单库静态比对
    public static final String QUERY_STATIC_FSS_PERSONLIST_V113 = "31002";
    // 布控表-迭代三
    public static final String QUERY_FSS_CAMERA_LIB_V113 = "31003";
    // 人员关系表-迭代三
    public static final String QUERY_FSS_REALTIONSHIP_V113 = "31004";
    // 历史表-迭代三
    public static final String QUERY_FSS_HISTORY_V113 = "31005";
    // 告警表-迭代三
    public static final String QUERY_FSS_ALARM_V113 = "31006";
    // 配置表所有信息-迭代三
    public static final String QUERY_FSS_LIB_CONFIG_V113 = "31007";
    // 初始化连接池数量
    public static final String SDK_CONNECTION_INITIAL_SIZE = "fss.sdk.pheonix.initialSize";
    // 连接池最大数量
    public static final String SDK_CONNECTION_MAX_SIZE = "fss.sdk.pheonix.maxActive";
    // 按index获取历史表图片
    public static final String QUERY_FSS_HISTORY_BY_INDEX_V113 = "12001";
    // 获取名单库身份证图片
    public static final String GET_PERSON_CARD_PICTURE_V113 = "31008";
    public static final String GET_ALARM_EXPORT_DATA_V113 = "31009";
    public static final String GET_HISTORY_SUPER_SEARCH_PICTURE_V113 = "31010";
    public static final String BATCH_MODIFY_PERSONLIST_FLAG_V113 = "31011";

    //协处理器
    public static final String SUM_COLUMN = "SUM_COLUMN";
    public static final byte[] FACE_FEATURE = Bytes.toBytes("FEATURE");
    public static final int FACE_ROWKEY_SALTING_LENGTH = 2;
    public static final byte[] FACE_EMPTY_REGION_START_KEY = Bytes.toBytes("10");
    public static final int FACE_SEARCH_ENDPOINT_POOL_NUM = 2;
    public static final int FACE_SIMILARITY_NUM = 3;
    // phoenix rowkey中varchar类型字段间隔字符
    public static final byte PHOENIX_GAP_CHAR_BYTE = (byte) 0;
    public static final byte STAY_TIME_EMPTY_REGION_START_KEY = (byte) 0;
    public static final int PHOENIX_ROWKEY_SALTING_LENGTH = 1;
    public static final int DATE_TIME_STR_LENGTH = 19;//格式为"yyyy-MM-dd HH:mm:ss",19个字节

    // 商汤比对算法归一下数组
    public static final String SENSETIME_FEATURE_SRC = "sensetime.feature.srcPoints";
    public static final String SENSETIME_FEATURE_DST = "sensetime.feature.dstPoints";
}

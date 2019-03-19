package com.znv.util;

/**
 * Created by Administrator on 2017/8/8.
 */
public enum FssErrorCodeEnum {
    // error code range
    // 110000-119999 : spark
    // 120000-129999 : hbase(phoenix)
    // 130000-139999 : es
    // 140000-149999 : kafka
    SUCCESS("success", 100000), SPARK_INVALID_PARAM("invalid param", 110001), HBASE_INVALID_PARAM("hbase invalid param",
        120001), PHOENIX_INVALID_PARAM("phoenix invalid param", 121001), PHOENIX_CONN_NULL("phoenix connection is null",
            121002), PHOENIX_INVALID_SQL("phoenix invalid sql", 121003), PHOENIX_SYS_ERROR("phoenix system error",
                121004), PHOENIX_TABLE_NOT_EXIST("phoenix table not exist", 121005), PHOENIX_FEATURE_NOT_EXIST(
                    "phoenix feature not exist", 121006), PHOENIX_TOO_MANY_LIBS("too many libs",
                        121007), PHOENIX_RELATIONID_ISCHILD("relationship relation id is child",
                            121008), PHOENIX_RELATIONID_NOT_EXIST("relationship relation id is not exist",
                                121009), PHOENIX_PERSONID_NOT_EXIST("relationship person id is not exist",
                                    121010), ES_INVALID_PARAM("invalid param", 130001), KAFKA_INVALID_PARAM(
                                        "invalid param", 140001), PHOENIX_PERSONLIST_TOO_MANY(
                                            "personlist table have too many perple", 121011);

    // 成员变量
    private String explanation;
    private int code;

    // 构造方法，注意：构造方法不能为public，因为enum并不可以被实例化
    FssErrorCodeEnum(String explanation, int code) {
        this.code = code;
        this.explanation = explanation;
    }

    public int getCode() {
        return code;
    }

    public String getExplanation() {
        return explanation;
    }

    // 根据说明获取对应的错误码
    public static int getCodeByExplanation(String explanation) {
        for (FssErrorCodeEnum e : FssErrorCodeEnum.values()) {
            if (e.getExplanation().equals(explanation)) {
                return e.code;
            }
        }
        return 0;
    }

    // 根据错误码获取对应的说明
    public static String getExplanationByCode(int code) {
        for (FssErrorCodeEnum e : FssErrorCodeEnum.values()) {
            if (e.getCode() == code) {
                return e.getExplanation();
            }
        }
        return null;
    }

    // 根据错误码获取对应枚举值
    public static FssErrorCodeEnum getEnumvalByCode(int code) {
        for (FssErrorCodeEnum e : FssErrorCodeEnum.values()) {
            if (e.getCode() == code) {
                return e;
            }
        }
        return null;
    }

}

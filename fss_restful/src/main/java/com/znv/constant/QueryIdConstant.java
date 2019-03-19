/**
 * <pre>
 * 标  题: QueryIdConstant.java.
 * 版权所有: 版权所有(C)2001-2017
 * 公   司: 深圳中兴力维技术有限公司
 * 内容摘要: // 简要描述本文件的内容，包括主要模块、函数及其功能的说明
 * 其他说明: // 其它内容的说明
 * 完成日期: // 输入完成日期，例：2000年2月25日
 * </pre>
 * <pre>
 * 修改记录1:
 *    修改日期：
 *    版 本 号：
 *    修 改 人：
 *    修改内容：
 * </pre>
 * @version 1.0
 * @author 0049002602
 */

package com.znv.constant;

import com.znv.servlet.initConnectionServlet;

import java.util.Properties;

/**
 * SDK请求查询的queryId常量类
 */
public class QueryIdConstant {

    private static Properties Prop= initConnectionServlet.getProp();

    /**
     * 名单库查询
     */
    public static final int BLACK_LIST_QUERY_ID = 20001;

    /**
     * 静态图片搜索
     */
    public static final int PICTURE_STATIC_SEARCH_ID = 20002;

    /**
     * 历史匹配查询
     */
    public static final int ALARM_LIST_QUERY_ID = 30001;

    /**
     * 人员关系查询
     */
    public static final int RELATION_LIST_QUERY_ID = 40001;

    /**
     * 历史库人员关系查询
     */
    public static final int HISTORY_RELATION_ID = 12005;

    /**
     * 区域驻留时间查询
     */
    public static final int RESIDENCE_TIME_ID = 12006;

    /**
     * 人流量和陌生人人流量查询
     */
    public static final String VISITOR_FLOW_ID = Prop.getProperty("fss.es.search.template.flowCount.id");

    /**
     * 任意条件搜索
     */
    public static final String ARBITRARY_CONDITION_ID = Prop.getProperty("fss.es.search.template.facesearch.id");
}

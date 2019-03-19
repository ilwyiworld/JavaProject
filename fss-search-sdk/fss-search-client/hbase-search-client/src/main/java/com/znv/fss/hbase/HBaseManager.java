package com.znv.fss.hbase;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.znv.fss.hbase.blackstatic.MultiBlackStaticSearch;
import com.znv.fss.hbase.mob.MOBReadTask;
import com.znv.fss.hbase.mob.SmallPictureWriteTask;
import com.znv.fss.hbase.peerTrack.PeerTrackSearch;
import com.znv.fss.hbase.mob.MOBWriteTask;
import com.znv.fss.hbase.relationshipsearch.RelationshipSearch;
import com.znv.fss.hbase.searchbyimage.MultiSearchByImage;
import com.znv.fss.hbase.searchbyimage.MultiSearchByTime;
import com.znv.fss.hbase.searchbyimage.SearchByTrial;
import com.znv.fss.hbase.staytime.StayTimeSearch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by Lee-HPSU on 2016/9/5.
 */
public class HBaseManager {
    private static final Log log = LogFactory.getLog(HBaseManager.class);

    public static MultiHBaseSearch createSearch(String jsonParamStr) {
        return createSearch(JSON.parseObject(jsonParamStr));
    }

    public static MultiHBaseSearch createSearch(JSONObject jsonParamObj) {
        MultiHBaseSearch search = null;
        JSONObject service = jsonParamObj.getJSONObject("reportService");
        String id = service.getString("id");
        String type = service.getString("type");
        SearchId sId = SearchId.getSearchId(id);

        if (type.equals("request") && (sId != null)) {
            switch (sId) {
                case SearchByImage:
                    search = new MultiSearchByImage();
                    break;
                case BlackStaticSearch:
                    search = new MultiBlackStaticSearch();
                    break;
                case SearchByTime:
                    search = new MultiSearchByTime();
                    break;
                case RelationshipSearch:
                    search = new RelationshipSearch();
                    break;
                case StayTimeSearch:
                    search = new StayTimeSearch();
                    break;
                case SearchByTrial:
                    search = new SearchByTrial();
                    break;
                case WriteMOB:
                    search = new MOBWriteTask();
                    break;
				case SearchForPeer:
                    search = new PeerTrackSearch();
                    break;
                case WriteSmallPicture:
                    search = new SmallPictureWriteTask();
                    break;
				case ReadMOB:
                    search = new MOBReadTask();
                    break;
                default:
                    log.warn("查询的报表不存在。");
                    break;
            }
        } else {
            log.warn("查询的报表不存在。");
            return null;
        }
        if (search != null) {
            search.setId(id);
        }
        return search;
    }

    /**
     * SearchId
     */
    public enum SearchId {
        SearchByImage("以图搜图", "12001"), BlackStaticSearch("黑名单静态比对", "12002"), SearchByTime("以图搜图按时间查询",
            "12003"), RelationshipSearch("历史人物关系查询", "12005"), StayTimeSearch("驻留时间查询", "12006"), SearchByTrial("轨迹查询",
                "12007"), SearchForPeer("同行人轨迹查询",
                    "12008"), WriteMOB("大图写入", "12009"), WriteSmallPicture("小图写入", "12010"), ReadMOB("图片读取", "12011");
        // 成员变量
        private String name;
        private String id;

        // 构造方法，注意：构造方法不能为public，因为enum并不可以被实例化
        SearchId(String name, String id) {
            this.name = name;
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public String getId() {
            return id;
        }

        public static SearchId getSearchId(String id) {
            for (SearchId s : SearchId.values()) {
                if (s.getId().equals(id)) {
                    return s;
                }
            }
            return null;
        }
    }

}

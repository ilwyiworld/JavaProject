package com.znv.hbase.client.featureComp;

import java.util.List;

/**
 * Created by Administrator on 2016/12/27.
 */
public class FeatureCompOutData {
    private List<FeatureCompId> featureIds; // 人脸分类Id
    private boolean isIdExsit = false; // 是否存在相似人脸

    public List<FeatureCompId> getFeatureIds() {
        return featureIds;
    }

    public void setFeatureIds(List<FeatureCompId> featureIds) {
        this.featureIds = featureIds;
    }

    public boolean isIdExsit() {
        return isIdExsit;
    }

    public void setIdExsit(boolean idExsit) {
        isIdExsit = idExsit;
    }

    /**
     * FeatureCompId
     */
    public static class FeatureCompId {
        private String id = "";
        private float sim = 0f;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public float getSim() {
            return sim;
        }

        public void setSim(float sim) {
            this.sim = sim;
        }
    }

}

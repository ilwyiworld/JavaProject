package org.elasticsearch.index.query.image;

//import org.elasticsearch.common.collect.MapMaker;

import org.elasticsearch.common.collect.MapBuilder;

import java.util.Map;

/**
 * Cache document score for {@link org.elasticsearch.index.query.image.FeatureHashQuery}
 */
public class FeatureScoreCache {
    // private Map<String, Float> scoreCache = new MapMaker().makeMap();
    private Map<String, Float> scoreCache = new MapBuilder().map();

    public Float getScore(String key) {
        if (!scoreCache.containsKey(key)) {
            return null;
        }
        return scoreCache.get(key);
    }

    public void setScore(String key, Float score) {
        scoreCache.put(key, score);
    }
}

package org.elasticsearch.plugin.image;

import org.elasticsearch.index.mapper.Mapper;
import org.elasticsearch.index.mapper.image.FeatureFieldMapper;
import org.elasticsearch.index.query.image.FeatureCompNativeScriptFactory;
import org.elasticsearch.index.query.image.FeatureQueryBuilder;
import org.elasticsearch.plugins.MapperPlugin;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.plugins.ScriptPlugin;
import org.elasticsearch.plugins.SearchPlugin;
import org.elasticsearch.script.NativeScriptFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ImagePlugin extends Plugin implements MapperPlugin, SearchPlugin, ScriptPlugin {

    @Override
    public Map<String, Mapper.TypeParser> getMappers() {
        return Collections.singletonMap(FeatureFieldMapper.CONTENT_TYPE, new FeatureFieldMapper.TypeParser());
    }

    @Override
    public List<QuerySpec<?>> getQueries() {
        return Collections.singletonList(
                new QuerySpec<>(FeatureQueryBuilder.NAME, FeatureQueryBuilder::new, FeatureQueryBuilder::fromXContent));
    }

    @Override
    public List<NativeScriptFactory> getNativeScripts() {
        return Collections.singletonList(new FeatureCompNativeScriptFactory());
    }

}

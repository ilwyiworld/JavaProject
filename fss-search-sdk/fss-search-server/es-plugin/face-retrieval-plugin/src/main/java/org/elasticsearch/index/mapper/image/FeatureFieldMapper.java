package org.elasticsearch.index.mapper.image;

import com.carrotsearch.hppc.cursors.ObjectObjectCursor;
import net.semanticmetadata.lire.indexing.hashing.BitSampling;
import net.semanticmetadata.lire.utils.SerializationUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.Query;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.collect.Iterators;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.hashing.LocalitySensitiveHashingCos;
import org.elasticsearch.hashing.PCADimReduction;
import org.elasticsearch.index.mapper.*;
import org.elasticsearch.index.query.QueryShardContext;
import org.elasticsearch.index.query.QueryShardException;
import org.elasticsearch.lopq.LOPQModel;
import org.elasticsearch.util.FeatureCompUtil;
import org.elasticsearch.util.FeatureCompression;
import org.elasticsearch.util.TypeConvertUtil;

import java.io.IOException;
import java.util.*;

public class FeatureFieldMapper extends FieldMapper {

    private static Logger logger = LogManager.getLogger(FeatureFieldMapper.class.getName());

    public static final String CONTENT_TYPE = "feature";

    public static final String HASH = "hash";
    public static final String HASH_STR_SUFFIX = ".string";
    public static final String HASH_SHORT_SUFFIX = ".short";
    public static final String FEATURE_HIGH = "feature_high";
    // public static final String FEATURE_LOW = "feature_low";
    // public static final String METADATA = "metadata";

    public static final String LOPQ = "lopq";
    public static final String LOPQ_COARSE_ID_SUFFIX = ".coarse_id";
    public static final String LOPQ_FINE_ID_SUFFIX = ".fine_id";
    public static final String COARSE_ENCODE = "coarse_encode";
    public static final String LOPQ_ENCODE = "lopq_encode";


    public static final String LSH_HASH_FILE = "/hash/lshHashFunctionsCos_16x256.obj";
    public static final String PCA_MEAN_FILE = "/pca/pcaMean_256_32_200w.obj";
    public static final String PCA_DIM_REDUCTION_FILE = "/pca/pcaComponents_256x32_200w.obj";
    public static final String LOPQ_MODEL_FILE = "/lopq/lopq_model_V1.0_D512_C13.lopq";

    private FeatureCompUtil fc = new FeatureCompUtil();
    private FeatureCompression fCompress = new FeatureCompression();

    public static class Defaults {
        public static final FeatureFieldType FIELD_TYPE = new FeatureFieldType();

        static {
            try {
                LocalitySensitiveHashingCos
                        .readHashFunctions(FeatureFieldMapper.class.getResourceAsStream(LSH_HASH_FILE));
                PCADimReduction.readPCAMeanAndMatrix(FeatureFieldMapper.class.getResourceAsStream(PCA_MEAN_FILE),
                        FeatureFieldMapper.class.getResourceAsStream(PCA_DIM_REDUCTION_FILE));
                LOPQModel.loadProto(FeatureFieldMapper.class.getResourceAsStream(LOPQ_MODEL_FILE));

            } catch (IOException e) {
                logger.error("Failed to initialize hash functions and pca matrix", e);
            }
        }
    }

    public static class Builder extends FieldMapper.Builder<Builder, FeatureFieldMapper> {
        private List<String> hashes = new ArrayList<String>();
        private boolean coarse_code = false;
        private boolean fine_code = false;

        public Builder(String name) {
            super(name, Defaults.FIELD_TYPE, Defaults.FIELD_TYPE);
            this.builder = this;
        }

        public Builder addHash(String hashName) {
            this.hashes.add(hashName);
            return this;
        }

        public Builder coarse_code(boolean isCoarseEncode) {
            this.coarse_code = isCoarseEncode;
            return this;
        }

        public Builder fine_code(boolean isLOPQEncode) {
            this.fine_code = isLOPQEncode;
            return this;
        }

        @Override
        protected void setupFieldType(BuilderContext context) {
            super.setupFieldType(context);
            FeatureFieldType fieldType = (FeatureFieldType) fieldType();
            fieldType.setCoarseEncode(coarse_code);
            fieldType.setLopqEncode(fine_code);
        }

        @Override
        public FeatureFieldMapper build(BuilderContext context) {
            Map<String, FieldMapper> hashMappers = new HashMap<String, FieldMapper>();// Maps.newHashMap();
            Map<String, FieldMapper> featureMappers = new HashMap<String, FieldMapper>();
            Map<String, FieldMapper> lopqMappers = new HashMap<String, FieldMapper>();

            context.path().add(name);
            // add feature and hash mappers
            FieldMapper featureMapper1 = new BinaryFieldMapper.Builder(FEATURE_HIGH).store(false).includeInAll(false)
                    .docValues(true).build(context);
            // FieldMapper featureMapper2 = new BinaryFieldMapper.Builder(FEATURE_LOW).store(false).includeInAll(false)
            // .docValues(true).build(context);
            featureMappers.put(FEATURE_HIGH, featureMapper1);
            // featureMappers.put(FEATURE_LOW, featureMapper2);

            for (String h : hashes) {
                String hashFieldName = HASH + "." + h;
                String hashFieldNameStr = hashFieldName + HASH_STR_SUFFIX;
                hashMappers.put(hashFieldNameStr, new KeywordFieldMapper.Builder(hashFieldNameStr).store(false)
                        .docValues(true).includeInAll(false).index(true).build(context));
                String hashFieldNameShort = hashFieldName + HASH_SHORT_SUFFIX;
                hashMappers.put(hashFieldNameShort,
                        new NumberFieldMapper.Builder(hashFieldNameShort, NumberFieldMapper.NumberType.SHORT).store(false)
                                .docValues(true).includeInAll(false).index(true).build(context));

                // hy added
                if (h.equals(HashEnum.LSH.name())) {
                    for (int i = 0; i < LocalitySensitiveHashingCos.getNumFunctionBundles(); i++) {
                        String hashvalFieldName = hashFieldName + "." + String.valueOf(i);
                        hashMappers.put(hashvalFieldName, new KeywordFieldMapper.Builder(hashvalFieldName).store(false)
                                .includeInAll(false).index(true).docValues(true).build(context));
                    }
                }
            }

            // add lopq id
            if (coarse_code) {
                String coarseFieldName = LOPQ + LOPQ_COARSE_ID_SUFFIX;
                FieldMapper coarseMapper = new KeywordFieldMapper.Builder(coarseFieldName).store(false)
                        .includeInAll(false).index(true).docValues(true).build(context);
                lopqMappers.put(coarseFieldName, coarseMapper);
            }
            if (fine_code) {
                String fineFieldName = LOPQ + LOPQ_FINE_ID_SUFFIX;
                FieldMapper fineMapper = new KeywordFieldMapper.Builder(fineFieldName).store(false).includeInAll(false)
                        .index(true).docValues(true).build(context);
                lopqMappers.put(fineFieldName, fineMapper);
            }

            context.path().remove(); // remove name
            setupFieldType(context);
            return new FeatureFieldMapper(name, fieldType, defaultFieldType, context.indexSettings(),
                    multiFieldsBuilder.build(this, context), copyTo, hashMappers, hashes, featureMappers, lopqMappers);
        }
    }

    public static class TypeParser implements Mapper.TypeParser {
        @SuppressWarnings({"unchecked"})
        @Override
        public Mapper.Builder parse(String name, Map<String, Object> node, ParserContext parserContext)
                throws MapperParsingException {
            FeatureFieldMapper.Builder builder = new FeatureFieldMapper.Builder(name);
            TypeParsers.parseField(builder, name, node, parserContext);

            boolean isHash = false;
            for (Iterator<Map.Entry<String, Object>> iterator = node.entrySet().iterator(); iterator.hasNext(); ) {
                Map.Entry<String, Object> entry = iterator.next();
                String propName = entry.getKey();
                Object propNode = entry.getValue();

                if (propName.equals(HASH)) {
                    if (propNode instanceof List) {
                        for (String h : (List<String>) propNode) {
                            builder.addHash(HashEnum.valueOf(h).name());
                        }
                    } else if (propNode instanceof String) {
                        builder.addHash(HashEnum.valueOf((String) propNode).name());
                    } else {
                        throw new IllegalArgumentException("Malformed hash value");
                    }
                    //iterator.remove();
                    isHash = true;
                }
                if (propName.equals(COARSE_ENCODE)) {
                    if (propNode instanceof Boolean) {
                        builder.coarse_code((boolean) propNode);
                    } else {
                        throw new IllegalArgumentException("Malformed coarse encode option");
                    }
                }
                if (propName.equals(LOPQ_ENCODE)) {
                    if (propNode instanceof Boolean) {
                        builder.fine_code((boolean) propNode);
                    } else {
                        throw new IllegalArgumentException("Malformed lopq encode option");
                    }
                }
                iterator.remove();
            }

           /* if (!isHash) {
                throw new ElasticsearchException("Feature hash type not found.");
            }*/

            return builder;
        }
    }

    public static class FeatureFieldType extends MappedFieldType {
        protected boolean coarseEncode;
        protected boolean lopqEncode;

        public FeatureFieldType() {
            setCoarseEncode(false);
            setLopqEncode(false);
        }

        protected FeatureFieldType(FeatureFieldType ref) {
            super(ref);
            setCoarseEncode(ref.coarseEncode);
            setLopqEncode(ref.lopqEncode);
        }

        @Override
        public MappedFieldType clone() {
            return new FeatureFieldType(this);
        }

        @Override
        public String typeName() {
            return CONTENT_TYPE;
        }

        public boolean isCoarseEncode() {
            return coarseEncode;
        }

        public void setCoarseEncode(boolean coarseEncode) {
            this.coarseEncode = coarseEncode;
        }

        public boolean isLopqEncode() {
            return lopqEncode;
        }

        public void setLopqEncode(boolean lopqEncode) {
            this.lopqEncode = lopqEncode;
        }

        @Override
        public Query termQuery(Object value, QueryShardContext context) {
            // todo
            throw new QueryShardException(context, "Feature fields do not support searching");
        }
    }

    private volatile ImmutableOpenMap<String, FieldMapper> featureMappers = ImmutableOpenMap.of();
    private volatile List<String> hashes;
    private volatile ImmutableOpenMap<String, FieldMapper> hashMappers = ImmutableOpenMap.of();
    private volatile ImmutableOpenMap<String, FieldMapper> lopqMappers = ImmutableOpenMap.of();

    public FeatureFieldMapper(String simpleName, MappedFieldType fieldType, MappedFieldType defaultFieldType,
                              Settings indexSettings, MultiFields multiFields, CopyTo copyTo, Map<String, FieldMapper> hashMappers,
                              List<String> hashes, Map<String, FieldMapper> featureMappers, Map<String, FieldMapper> lopqMappers) {
        super(simpleName, fieldType, defaultFieldType, indexSettings, multiFields, copyTo);
        if (hashes != null) {
            this.hashes = hashes;
        }
        if (hashMappers != null) {
            this.hashMappers = ImmutableOpenMap.builder(this.hashMappers).putAll(hashMappers).build();
        }
        if (featureMappers != null) {
            this.featureMappers = ImmutableOpenMap.builder(this.featureMappers).putAll(featureMappers).build();
        }
        if (lopqMappers != null) {
            this.lopqMappers = ImmutableOpenMap.builder(this.lopqMappers).putAll(lopqMappers).build();
        }
    }

    @Override
    public Mapper parse(ParseContext context) throws IOException {
        byte[] feature = context.parseExternalValue(byte[].class);
        if (feature == null) {
            if (context.parser().currentToken() == XContentParser.Token.VALUE_NULL) {
                throw new MapperParsingException("No content is provided.");
            } else {
                feature = context.parser().binaryValue();
            }
        }
        if (feature == null) {
            throw new MapperParsingException("No content is provided.");
        }

        FieldMapper featureMapperH = featureMappers.get(FEATURE_HIGH);

        // lq modify feature Q7 chuan cut to 31 write to feature_high
        // featureMapperH.parse(context.createExternalValueContext(feature));
        float[] f_array = fc.getFloatArray(feature);
        byte[] f_compress_31 = fCompress.writeQuanV2(f_array);
        featureMapperH.parse(context.createExternalValueContext(f_compress_31));

        // context.doc().add(new BinaryDocValuesField(featureMapper.name(), new BytesRef(feature)));
     /*   double[] f_high = fc.getDoubleArray(feature, 12);
        double[] f_low_d = PCADimReduction.generateLowDimensions(f_high);
        if (f_low_d == null) {  //传入特征有问题
            return null;
        }
        byte[] f_low_b = PCADimReduction.double2Bytes(f_low_d);*/
        // FieldMapper featureMapperL = featureMappers.get(FEATURE_LOW);
        // featureMapperL.parse(context.createExternalValueContext(f_low_b));

        for (String h : hashes) {
            double[] f_high = fc.getDoubleArray(feature, 12);
            HashEnum hashEnum = HashEnum.valueOf(h);
            int[] hashVals = null;
            if (hashEnum.equals(HashEnum.BIT_SAMPLING)) {
                hashVals = BitSampling.generateHashes(f_high);
            } else if (hashEnum.equals(HashEnum.LSH)) {
                hashVals = LocalitySensitiveHashingCos.generateHashes(f_high);//f_low_d
            }
            String mapperName = HASH + "." + h;
            String mapperNameStr = mapperName + HASH_STR_SUFFIX;
            FieldMapper hashMapperStr = hashMappers.get(mapperNameStr);
            String hashstr = TypeConvertUtil.intarrayToString(hashVals);
            hashMapperStr.parse(context.createExternalValueContext(hashstr));
            // context.doc().add(new Field(hashMapper.name(), hashstr, hashMapper.fieldType()));

            String mapperNameShort = mapperName + HASH_SHORT_SUFFIX;
            FieldMapper hashMapperShort = hashMappers.get(mapperNameShort);
            short hashlong = TypeConvertUtil.intarrayToShort(hashVals);
            hashMapperShort.parse(context.createExternalValueContext(hashlong));
            if (hashVals != null) {
                for (int i = 0; i < hashVals.length; i++) {
                    String valmapperName = mapperName + "." + String.valueOf(i);
                    FieldMapper hashvalMapper = hashMappers.get(valmapperName);
                    hashvalMapper.parse(context.createExternalValueContext(String.valueOf(hashVals[i])));
                    // context.doc()
                    // .add(new Field(hashvalMapper.name(), String.valueOf(hashVals[i]), hashvalMapper.fieldType()));
                }
            }
        }
        //lopq
        String coarseFieldName = LOPQ + LOPQ_COARSE_ID_SUFFIX;
        FieldMapper coarseMapper = lopqMappers.get(coarseFieldName);
        if (coarseMapper != null) {
            int[] coarseCode = LOPQModel.predictCoarse(fc.getFloatArray(feature));
            coarseMapper.parse(context.createExternalValueContext(SerializationUtils.arrayToString(coarseCode)));
        }
        String fineFieldName = LOPQ + LOPQ_FINE_ID_SUFFIX;
        FieldMapper fineMapper = lopqMappers.get(fineFieldName);
        if (fineMapper != null) {
            int[] fineCode = LOPQModel.predictFine(fc.getFloatArray(feature), null);
            fineMapper.parse(context.createExternalValueContext(SerializationUtils.arrayToString(fineCode)));
            //todo
        }
        return null;// hy added
    }

    @Override
    protected void parseCreateField(ParseContext context, List<IndexableField> fields) throws IOException {
        throw new UnsupportedOperationException("should not be invoked");
    }

    @Override
    public FieldMapper updateFieldType(Map<String, MappedFieldType> fullNameToFieldType) {
        FeatureFieldMapper update = (FeatureFieldMapper) super.updateFieldType(fullNameToFieldType);
        Map<String, FieldMapper> featureMappersUpdate = new HashMap<String, FieldMapper>(featureMappers.size());
        for (ObjectObjectCursor<String, FieldMapper> cursor : featureMappers) {
            featureMappersUpdate.put(cursor.key, cursor.value.updateFieldType(fullNameToFieldType));
        }
        Map<String, FieldMapper> hashMappersUpdate = new HashMap<String, FieldMapper>(hashMappers.size());
        for (ObjectObjectCursor<String, FieldMapper> cursor : hashMappers) {
            hashMappersUpdate.put(cursor.key, cursor.value.updateFieldType(fullNameToFieldType));
        }
        Map<String, FieldMapper> lopqMappersUpdate = new HashMap<String, FieldMapper>(lopqMappers.size());
        for (ObjectObjectCursor<String, FieldMapper> cursor : lopqMappers) {
            lopqMappersUpdate.put(cursor.key, cursor.value.updateFieldType(fullNameToFieldType));
        }

        boolean eqflag = true;
        for (ObjectObjectCursor<String, FieldMapper> cursor : hashMappers) {
            if (hashMappersUpdate.get(cursor.key) != cursor.value) {
                eqflag = false;
                break;
            }
        }

        for (ObjectObjectCursor<String, FieldMapper> cursor : featureMappers) {
            if (featureMappersUpdate.get(cursor.key) != cursor.value) {
                eqflag = false;
                break;
            }
        }
        for (ObjectObjectCursor<String, FieldMapper> cursor : lopqMappers) {
            if (lopqMappersUpdate.get(cursor.key) != cursor.value) {
                eqflag = false;
                break;
            }
        }
        if (update == this && eqflag) {
            return this;
        }
        if (update == this) {
            update = (FeatureFieldMapper) clone();
        }
        ImmutableOpenMap.builder(update.hashMappers).putAll(hashMappersUpdate).build();
        ImmutableOpenMap.builder(update.featureMappers).putAll(featureMappersUpdate).build();
        ImmutableOpenMap.builder(update.lopqMappers).putAll(lopqMappersUpdate).build();
        return update;
    }

    @Override
    public Iterator<Mapper> iterator() {
        List<Mapper> list = new ArrayList<>(hashMappers.size() + featureMappers.size());
        for (ObjectObjectCursor<String, FieldMapper> cursor : hashMappers) {
            list.add(cursor.value);
        }
        for (ObjectObjectCursor<String, FieldMapper> cursor : featureMappers) {
            list.add(cursor.value);
        }
        for (ObjectObjectCursor<String, FieldMapper> cursor : lopqMappers) {
            list.add(cursor.value);
        }
        return Iterators.concat(super.iterator(), list.iterator());// featureMapper.iterator(),
    }

    @Override
    protected String contentType() {
        return CONTENT_TYPE;
    }

    @Override
    protected void doXContentBody(XContentBuilder builder, boolean includeDefaults, Params params) throws IOException {
        super.doXContentBody(builder, includeDefaults, params);
        //builder.field(HASH, hashes);
        FeatureFieldType fieldType = (FeatureFieldType) fieldType();
        if (includeDefaults || fieldType.isCoarseEncode()) {
            builder.field(COARSE_ENCODE, fieldType.isCoarseEncode());
        }
        if (includeDefaults || fieldType.isLopqEncode()) {
            builder.field(LOPQ_ENCODE, fieldType.isLopqEncode());
        }
    }
}

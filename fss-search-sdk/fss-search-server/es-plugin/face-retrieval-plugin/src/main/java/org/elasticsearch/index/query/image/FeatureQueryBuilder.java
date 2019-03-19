package org.elasticsearch.index.query.image;

import net.semanticmetadata.lire.indexing.hashing.BitSampling;
import net.semanticmetadata.lire.utils.SerializationUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.elasticsearch.common.ParsingException;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.lucene.search.Queries;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.hashing.LocalitySensitiveHashingCos;
import org.elasticsearch.hashing.PCADimReduction;
import org.elasticsearch.index.mapper.image.FeatureFieldMapper;
import org.elasticsearch.index.mapper.image.HashEnum;
import org.elasticsearch.index.query.AbstractQueryBuilder;
import org.elasticsearch.index.query.QueryParseContext;
import org.elasticsearch.index.query.QueryShardContext;
import org.elasticsearch.lopq.LOPQModel;
import org.elasticsearch.util.FeatureCompUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class FeatureQueryBuilder extends AbstractQueryBuilder<FeatureQueryBuilder> {
    private static Logger logger = LogManager.getLogger(FeatureQueryBuilder.class.getName());
    public static final String NAME = "feature";
    private String fieldName;
    // private byte[] feature;
    private List<byte[]> feature = new ArrayList<>();
    private String hash;
    private float boost = super.boost() /*-1*/;
    private int limit = -1;
    private int hmDistance = -1;
    private boolean useScript = true;
    private String lookupIndex;
    private String lookupType;
    private String lookupId;
    private String lookupRouting;
    private String lookupPath;
    private boolean coarseEncode = false;
    private boolean lopqEncode = false;
    private int numCoarseCenters=1;

    public FeatureQueryBuilder(String fieldName) {
        if (Strings.isEmpty(fieldName)) {
            throw new IllegalArgumentException("[feature] field name is null or empty");
        }
        this.fieldName = fieldName;
    }

    /**
     * Read from a stream.
     */
    public FeatureQueryBuilder(StreamInput in) throws IOException {
        super(in);
        fieldName = in.readString();
        hash = in.readOptionalString();
        lookupIndex = in.readOptionalString();
        lookupType = in.readOptionalString();
        lookupId = in.readOptionalString();
        lookupRouting = in.readOptionalString();
        lookupPath = in.readOptionalString();
        limit = in.readOptionalVInt();
        hmDistance = in.readOptionalVInt();
        int featureSize = in.readInt();
        for (int i = 0; i < featureSize; i++) {
            feature.add(in.readByteArray());
        }
        //feature = in.readByteArray();
        coarseEncode = in.readBoolean();
        lopqEncode = in.readBoolean();
        useScript = in.readBoolean();
    }

    @Override
    protected void doWriteTo(StreamOutput out) throws IOException {
        out.writeString(fieldName);
        out.writeOptionalString(hash);
        out.writeOptionalString(lookupIndex);
        out.writeOptionalString(lookupType);
        out.writeOptionalString(lookupId);
        out.writeOptionalString(lookupRouting);
        out.writeOptionalString(lookupPath);
        out.writeOptionalVInt(limit);
        out.writeOptionalVInt(hmDistance);
        out.writeInt(feature.size());
        //out.writeByteArray(feature);
        for (int i = 0; i < feature.size(); i++) {
            out.writeByteArray(feature.get(i));
        }
        out.writeOptionalBoolean(coarseEncode);
        out.writeOptionalBoolean(lopqEncode);
        out.writeOptionalBoolean(useScript);
    }

    /* public FeatureQueryBuilder feature(byte[] feature) {
         this.feature = feature;
         return this;
     }*/
    public FeatureQueryBuilder feature(List<byte[]> feature) {
        this.feature = feature;
        return this;
    }

    public FeatureQueryBuilder hash(String hash) {
        this.hash = hash;
        return this;
    }

    public FeatureQueryBuilder limit(int limit) {
        this.limit = limit;
        return this;
    }

    public FeatureQueryBuilder hmDistance(int hmDistance) {
        this.hmDistance = hmDistance;
        return this;
    }

    public FeatureQueryBuilder coarseEncode(boolean coarseEncode) {
        this.coarseEncode = coarseEncode;
        return this;
    }

    public FeatureQueryBuilder useScript(boolean useScript) {
        this.useScript = useScript;
        return this;
    }

    public FeatureQueryBuilder lopqEncode(boolean lopqEncode) {
        this.lopqEncode = lopqEncode;
        return this;
    }

    public FeatureQueryBuilder lookupIndex(String lookupIndex) {
        this.lookupIndex = lookupIndex;
        return this;
    }

    public FeatureQueryBuilder lookupType(String lookupType) {
        this.lookupType = lookupType;
        return this;
    }

    public FeatureQueryBuilder lookupId(String lookupId) {
        this.lookupId = lookupId;
        return this;
    }

    public FeatureQueryBuilder lookupPath(String lookupPath) {
        this.lookupPath = lookupPath;
        return this;
    }

    public FeatureQueryBuilder lookupRouting(String lookupRouting) {
        this.lookupRouting = lookupRouting;
        return this;
    }

    @Override
    protected void doXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject(NAME);
        builder.startObject(fieldName);

        if (feature != null) {
            builder.field("feature", feature);
        }

        if (lookupIndex != null) {
            builder.field("index", lookupIndex);
        }
        builder.field("type", lookupType);
        builder.field("id", lookupId);
        if (lookupRouting != null) {
            builder.field("routing", lookupRouting);
        }
        builder.field("path", lookupPath);

        if (hash != null) {
            builder.field("hash", hash);
        }

        if (limit != -1) {
            builder.field("limit", limit);
        }

        if (hmDistance != -1) {
            builder.field("hmDistance", hmDistance);
        }
        builder.field("useScript", useScript);
        builder.field("coarseEncode", coarseEncode);
        builder.field("lopqEncode", lopqEncode);
        printBoostAndQueryName(builder);
        builder.endObject();
        builder.endObject();
    }

    public static Optional<FeatureQueryBuilder> fromXContent(QueryParseContext parseContext) throws IOException {
        XContentParser parser = parseContext.parser();
        XContentParser.Token token = parser.nextToken();
        if (token != XContentParser.Token.FIELD_NAME) {
            throw new ParsingException(parser.getTokenLocation(), "[feature] query malformed, no field");
        }

        String fieldName = parser.currentName();
        String queryName = null;
        // byte[] feature = null;
        List<Object> featureStringList = new ArrayList<>();
        List<byte[]> feature = new ArrayList<>();
        HashEnum hashEnum = null;
        int limit = -1;
        int hmDistance = -1;
        boolean useScript = true;
        String lookupIndex = null;
        String lookupType = null;
        String lookupId = null;
        String lookupPath = null;
        String lookupRouting = null;
        boolean coarseEncode = false;
        boolean lopqEncode = false;
        Base64 basedecoder = new Base64();
        token = parser.nextToken();
        if (token == XContentParser.Token.START_OBJECT) {
            String currentFieldName = null;
            while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
                if (token == XContentParser.Token.FIELD_NAME) {
                    currentFieldName = parser.currentName();
                } else {
                    if ("feature".equals(currentFieldName)) {
                        featureStringList = parser.list();
                        for (int i = 0; i < featureStringList.size(); i++) {
                            feature.add(i, basedecoder.decode(featureStringList.get(i).toString()));
                            // feature.add(i,parser.binaryValue()) ;
                        }
                    } else if ("hash".equals(currentFieldName)) {
                        hashEnum = HashEnum.getByName(parser.text());
                    } else if ("limit".equals(currentFieldName)) {
                        limit = parser.intValue();
                    } else if ("hmDistance".equals(currentFieldName)) {
                        hmDistance = parser.intValue();
                    } else if ("useScript".equals(currentFieldName)) {
                        useScript = parser.booleanValue();
                    } else if ("coarseEncode".equals(currentFieldName)) {
                        coarseEncode = parser.booleanValue();
                    } else if ("lopqEncode".equals(currentFieldName)) {
                        lopqEncode = parser.booleanValue();
                    } else if ("index".equals(currentFieldName)) {
                        lookupIndex = parser.text();
                    } else if ("type".equals(currentFieldName)) {
                        lookupType = parser.text();
                    } else if ("id".equals(currentFieldName)) {
                        lookupId = parser.text();
                    } else if ("path".equals(currentFieldName)) {
                        lookupPath = parser.text();
                    } else if ("routing".equals(currentFieldName)) {
                        lookupRouting = parser.textOrNull();
                    } else if (AbstractQueryBuilder.NAME_FIELD.match(currentFieldName)) {
                        queryName = parser.text();
                    } else {
                        throw new ParsingException(parser.getTokenLocation(),
                                "[feature] query does not support [" + currentFieldName + "]");
                    }
                }
            }
            parser.nextToken();
        }

        FeatureQueryBuilder queryBuilder = new FeatureQueryBuilder(fieldName);
        queryBuilder.feature(feature);
        if (hashEnum != null) {
            queryBuilder.hash(hashEnum.name());
        }
        queryBuilder.lookupIndex(lookupIndex);
        queryBuilder.lookupType(lookupType);
        queryBuilder.lookupId(lookupId);
        queryBuilder.lookupPath(lookupPath);
        queryBuilder.lookupRouting(lookupRouting);
        queryBuilder.limit(limit);
        queryBuilder.hmDistance(hmDistance);
        queryBuilder.useScript(useScript);
        queryBuilder.queryName(queryName);
        queryBuilder.coarseEncode(coarseEncode);
        queryBuilder.lopqEncode(lopqEncode);
        return Optional.of(queryBuilder);
    }

    protected Query fineQuery(QueryShardContext context) throws IOException {
        Query query = null;
        if (lopqEncode) {
            FeatureCompUtil fc = new FeatureCompUtil();
           /* List<double[]> qfh = new ArrayList<>();
            for(int i=0;i<feature.size();i++){
                qfh.add(i,fc.getDoubleArray(feature.get(i), 12));
            }*/
            // double[] qfh = fc.getDoubleArray(feature, 12);
            //  int[] fineCode = LOPQModel.predictFine(fc.getFloatArray(feature), null);
            ArrayList<int[]> fineCodeList = new ArrayList<>();
            for (int j = 0; j < feature.size(); j++) {
                int[][] fineCode = LOPQModel.predictFineOrder(fc.getFloatArray(feature.get(j)), null);
                //int[] fineCode = LOPQModel.predictFine(fc.getFloatArray(feature.get(j)), null);
                // fineCodeList.add(j,fineCode);
                for (int i = 0; i < fineCode.length; i++) {
                    fineCodeList.add(fineCode[i]);
                }
            }
            BooleanQuery.Builder fineBooleanQueryBuilder = new BooleanQuery.Builder();

            String fineFieldName = fieldName + "." + FeatureFieldMapper.LOPQ + FeatureFieldMapper.LOPQ_FINE_ID_SUFFIX;
            /*fineBooleanQueryBuilder.add(
                    new BooleanClause(new TermQuery(new Term(fineFieldName, SerializationUtils.arrayToString(fineCode))), BooleanClause.Occur.FILTER));*/

            // BoolQueryBuilder fineBooleanQueryBuilder1 = QueryBuilders.boolQuery();
            //   fineBooleanQueryBuilder1.should(QueryBuilders.termsQuery(fineFieldName, fineCodeList)).minimumShouldMatch(1);
            for (int k = 0; k < fineCodeList.size(); k++) {
                fineBooleanQueryBuilder.add(
                        new BooleanClause(new TermQuery(new Term(fineFieldName, SerializationUtils.arrayToString(fineCodeList.get(k)))), BooleanClause.Occur.SHOULD));
            }
            query = Queries.filtered(fineBooleanQueryBuilder.build(), null);
            // ?? return adjustPureNegative ? fixNegativeQueryIfNeeded(query) : query;
        }
        return query;
    }

    protected Query coarseQuery(QueryShardContext context) throws IOException {
        Query query = null;
        if (coarseEncode) {
            FeatureCompUtil fc = new FeatureCompUtil();
            // double[] qfh = fc.getDoubleArray(feature, 12);
            // int[] coarseCode = LOPQModel.predict(fc.getFloatArray(feature));
           /* List<double[]> qfh = new ArrayList<>();
            for(int i=0;i<feature.size();i++){
                qfh.add(i,fc.getDoubleArray(feature.get(i), 12));
            }*/
            ArrayList<int[]> coarseCodeList = new ArrayList<>();
            for (int j = 0; j < feature.size(); j++) {
                int[][] coarseCode = LOPQModel.predictCoarseOrder(fc.getFloatArray(feature.get(j)),numCoarseCenters);
                for (int i = 0; i < coarseCode.length; i++) {
                    coarseCodeList.add(coarseCode[i]);
                }
            }
            BooleanQuery.Builder coarseBooleanQueryBuilder = new BooleanQuery.Builder();
            String coarseFieldName = fieldName + "." + FeatureFieldMapper.LOPQ + FeatureFieldMapper.LOPQ_COARSE_ID_SUFFIX;
            for (int k = 0; k < coarseCodeList.size(); k++) {
                coarseBooleanQueryBuilder.add(
                        new BooleanClause(new TermQuery(new Term(coarseFieldName, SerializationUtils.arrayToString(coarseCodeList.get(k)))), BooleanClause.Occur.SHOULD));
            }
            query = Queries.filtered(coarseBooleanQueryBuilder.build(), null);
            // ?? return adjustPureNegative ? fixNegativeQueryIfNeeded(query) : query;

        }
        return query;
    }

    @Override
    protected Query doToQuery(QueryShardContext context) throws IOException {
        Query query = null;
        if (hash != null) { // no hash, need to scan all documents
            // todo return new FeatureQuery(luceneFieldName, feature, boost);
            query = hashQuery(context);
        } else if (coarseEncode) {
            query = coarseQuery(context);
        } else if (lopqEncode) {
            query = fineQuery(context);
        }

        //  Query query = coarseOrFineQuery(context);
        return query;
    }

    protected Query hashQuery(QueryShardContext context) throws IOException {
        if (hash == null) { // no hash, need to scan all documents
            // todo return new FeatureQuery(luceneFieldName, feature, boost);
            return null;
        } else { // query by hash first
            int[] hashval = null;
            FeatureCompUtil fc = new FeatureCompUtil();
            int offset = 0; // low -0 ;high -12
            //feature.get(0):暂且取第一个特征值
            byte[] feature_low = PCADimReduction.double2Bytes(PCADimReduction.generateLowDimensions(fc.getDoubleArray(feature.get(0), 12)));
            if (hash.equals(HashEnum.BIT_SAMPLING.name())) {
                hashval = BitSampling.generateHashes(fc.getDoubleArray(feature_low, 0));
            } else if (hash.equals(HashEnum.LSH.name())) {
                hashval = LocalitySensitiveHashingCos.generateHashes(fc.getDoubleArray(feature_low, 0));
            }
            String featureFieldName = fieldName + "." + FeatureFieldMapper.FEATURE_HIGH;
            String hashFieldName = fieldName + "." + FeatureFieldMapper.HASH + "." + hash;

            if (limit > 0) {  // has max result limit, use ImageHashLimitQuery
                //todo return new FeatureHashLimitQuery(hashFieldName, hashval, limit, luceneFieldName, feature, boost);
                return null;
            } else { // no max result limit, use ImageHashQuery
                BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();
                FeatureScoreCache imageScoreCache = new FeatureScoreCache();
                if (hashval != null) {
                    for (int i = 0; i < hashval.length; i++) {
                        String hashvalFieldName = hashFieldName + "." + String.valueOf(i);
                        if (!useScript) {
                            for (int j = 0; i < feature.size(); j++) {
                                booleanQueryBuilder.add(
                                        new BooleanClause(new FeatureHashQuery(new Term(hashvalFieldName, Integer.toString(hashval[i])),
                                                featureFieldName, feature.get(j), imageScoreCache, boost), BooleanClause.Occur.SHOULD));
                            }

                        } else {
                            booleanQueryBuilder.add(
                                    new BooleanClause(new TermQuery(new Term(hashvalFieldName, Integer.toString(hashval[i]))), BooleanClause.Occur.SHOULD));
                        }
                    }
                }
                int minimumShouldMatch = 1;// todo
                // todo add minimumShouldMatch param??
                if (this.hmDistance >= 0) {
                    minimumShouldMatch = Math.max(LocalitySensitiveHashingCos.getNumFunctionBundles() - this.hmDistance,
                            minimumShouldMatch);
                }
                Query query = Queries.applyMinimumShouldMatch(booleanQueryBuilder.build(),
                        String.valueOf(minimumShouldMatch));
                // ?? return adjustPureNegative ? fixNegativeQueryIfNeeded(query) : query;
                return query;
            }

        }
    }

    @Override
    protected boolean doEquals(FeatureQueryBuilder other) {
        return Objects.equals(fieldName, other.fieldName) && Objects.equals(feature, other.feature)
                && Objects.equals(hash, other.hash) && Objects.equals(hmDistance, other.hmDistance)
                && Objects.equals(limit, other.limit) && Objects.equals(lookupIndex, other.lookupIndex)
                && Objects.equals(lookupType, other.lookupType) && Objects.equals(lookupId, other.lookupId)
                && Objects.equals(lookupPath, other.lookupPath) && Objects.equals(lookupRouting, other.lookupRouting)
                && Objects.equals(useScript, other.useScript) && Objects.equals(coarseEncode, other.coarseEncode)
                && Objects.equals(lopqEncode, other.lopqEncode);
    }

    @Override
    protected int doHashCode() {
        return Objects.hash(fieldName, feature, hash, hmDistance, useScript, limit, lookupIndex, lookupType, lookupId, lookupPath,
                lookupRouting, coarseEncode, lopqEncode);
    }

    @Override
    public String getWriteableName() {
        return NAME;
    }

}

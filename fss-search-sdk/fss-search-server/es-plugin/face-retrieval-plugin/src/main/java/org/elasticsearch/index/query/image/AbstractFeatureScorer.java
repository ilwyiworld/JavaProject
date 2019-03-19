package org.elasticsearch.index.query.image;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.index.BinaryDocValues;
import org.apache.lucene.index.DocValuesType;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Weight;
import org.apache.lucene.store.ByteArrayDataInput;
import org.apache.lucene.util.*;
import org.elasticsearch.ElasticsearchImageProcessException;
import org.elasticsearch.index.fielddata.ScriptDocValues;
import org.elasticsearch.index.fielddata.SortedBinaryDocValues;
import org.elasticsearch.util.FeatureCompUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.apache.lucene.search.DocIdSetIterator.NO_MORE_DOCS;

/**
 * Calculate score for each image
 * score = (1 / distance) * boost
 */
public abstract class AbstractFeatureScorer extends Scorer {
    private static Logger logger = LogManager.getLogger(AbstractFeatureScorer.class.getName());
    private final String luceneFieldName;
    private final byte[] lireFeature;
    private final LeafReaderContext context;
    private final float boost;
    private BinaryDocValues binaryDocValues;
    private FeatureCompUtil fc = new FeatureCompUtil();

    protected AbstractFeatureScorer(Weight weight, String luceneFieldName, byte[] lireFeature,
                                    LeafReaderContext context, float boost) {
        super(weight);
        this.luceneFieldName = luceneFieldName;
        this.lireFeature = lireFeature;
        this.context = context;
        this.boost = boost;
    }

    @Override
    public float score() throws IOException {
        assert docID() != NO_MORE_DOCS;

        if (binaryDocValues == null) {
            //AtomicReader atomicReader = (AtomicReader) reader;
            binaryDocValues = context.reader().getBinaryDocValues(luceneFieldName);
        }

        try {
            List<byte[]> refs = getBytesValues(binaryDocValues, docID());
            byte[] docFeature = refs.get(0);

            float score = 0f;
            if (docFeature != null && lireFeature != null) {
                score = fc.Normalize(fc.Dot(docFeature, lireFeature, 12));
            }
            /*if (Float.compare(distance, 1.0f) <= 0) { // distance less than 1, consider as same image
                score = 2f - distance;
            } else {
                score = 1 / distance;
            }*/
            return score * boost;
        } catch (Exception e) {
            throw new ElasticsearchImageProcessException("Failed to calculate score", e);
        }
    }

    @Override
    public int freq() {
        return 1;
    }

    //fieldmapper 类型为BinaryFieldMapper
    //参考 org.elasticsearch.index.fielddata.plain.BytesBinaryDVAtomicFieldData.getBytesValues 函数
    private List<byte[]> getBytesValues(BinaryDocValues values, int docId) {
        int count;
        List<byte[]> refs = new ArrayList<>(1);
        final ByteArrayDataInput in = new ByteArrayDataInput();

        final BytesRef bytes = values.get(docId);
        in.reset(bytes.bytes, bytes.offset, bytes.length);
        if (bytes.length == 0) {
            count = 0;
        } else {
            count = in.readVInt();
            for (int i = 0; i < count; ++i) {
                final int length = in.readVInt();
                final byte[] scratch = new byte[length];
                in.readBytes(scratch, 0, length);
                refs.add(scratch);
            }
        }

        return refs;
    }
}

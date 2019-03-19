package org.elasticsearch.index.query.image;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.Similarity;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

/**
 * Copied from {@link TermQuery}, query by hash first and only calculate score for matching docs
 */
public class FeatureHashQuery extends Query {
    private static Logger logger = LogManager.getLogger(FeatureHashQuery.class.getName());
    private final Term term;

    private String luceneFieldName;
    private byte[] lireFeature;
    private FeatureScoreCache imageScoreCache;
    private float boost;

    final class FeatureHashScorer extends AbstractFeatureScorer {
        private final PostingsEnum postingsEnum;
        private final IndexReader reader;

        FeatureHashScorer(Weight weight, PostingsEnum td, LeafReaderContext context) {
            super(weight, luceneFieldName, lireFeature, context, boost);
            this.postingsEnum = td;
            this.reader = context.reader();
        }

        @Override
        public int docID() {
            return postingsEnum.docID();
        }

        @Override
        public DocIdSetIterator iterator() {
            return postingsEnum;
        }

        @Override
        public float score() throws IOException {
            assert docID() != DocIdSetIterator.NO_MORE_DOCS;
            int docId = docID();
            String cacheKey = reader.toString() + ":" + docId;
            if (imageScoreCache.getScore(cacheKey) != null) {
                return 0f; // BooleanScorer will add all score together, return 0 for docs already processed
            }
            float score = super.score();
            imageScoreCache.setScore(cacheKey, score);
            return score;
        }

        /**
         * Returns a string representation of this <code>FeatureHashScorer</code>.
         */
        @Override
        public String toString() {
            return "scorer(" + weight + ")[" + super.toString() + "]";
        }
    }

    final class FeatureHashWeight extends Weight {
        private final TermContext termStates;
        private final Similarity similarity;
        private final Similarity.SimWeight stats;

        public FeatureHashWeight(IndexSearcher searcher, TermContext termStates) throws IOException {
            super(FeatureHashQuery.this);
            assert termStates != null : "TermContext must not be null";
            this.termStates = termStates;

            boolean needsScores = true;// hy added
            this.similarity = searcher.getSimilarity(needsScores);

            final CollectionStatistics collectionStats;
            final TermStatistics termStats;
            collectionStats = searcher.collectionStatistics(term.field());
            termStats = searcher.termStatistics(term, termStates);
//            if (needsScores) {
//                collectionStats = searcher.collectionStatistics(term.field());
//                termStats = searcher.termStatistics(term, termStates);
//            } else {
//                // we do not need the actual stats, use fake stats with docFreq=maxDoc and ttf=-1
//                final int maxDoc = searcher.getIndexReader().maxDoc();
//                collectionStats = new CollectionStatistics(term.field(), maxDoc, -1, -1, -1);
//                termStats = new TermStatistics(term.bytes(), maxDoc, -1);
//            }

            this.stats = similarity.computeWeight(collectionStats, termStats);
        }

        @Override
        public void extractTerms(Set<Term> terms) {
            terms.add(getTerm());
        }

        @Override
        public String toString() {
            return "weight(" + FeatureHashQuery.this + ")";
        }

        @Override
        public float getValueForNormalization() {
            return 1f;
        }

        @Override
        public void normalize(float queryNorm, float topLevelBoost) {
        }

        @Override
        public Scorer scorer(LeafReaderContext context) throws IOException {
            assert termStates == null || termStates.wasBuiltFor(ReaderUtil.getTopLevelContext(context)) : "The top-reader used to create Weight is not the same as the current reader's top-reader (" + ReaderUtil.getTopLevelContext(context);
            final TermsEnum termsEnum = getTermsEnum(context);
            if (termsEnum == null) {
                return null;
            }
            PostingsEnum docs = termsEnum.postings(null, PostingsEnum.FREQS);//needsScores
            assert docs != null;
            return new FeatureHashScorer(this, docs, context);
        }

        private TermsEnum getTermsEnum(LeafReaderContext context) throws IOException {
            if (termStates != null) {
                // TermQuery either used as a Query or the term states have been provided at construction time
                assert termStates.wasBuiltFor(ReaderUtil.getTopLevelContext(context)) : "The top-reader used to create Weight is not the same as the current reader's top-reader (" + ReaderUtil.getTopLevelContext(context);
                final TermState state = termStates.get(context.ord);
                if (state == null) { // term is not present in that reader
                    assert termNotInReader(context.reader(), term) : "no termstate found but term exists in reader term=" + term;
                    return null;
                }
                final TermsEnum termsEnum = context.reader().terms(term.field()).iterator();
                termsEnum.seekExact(term.bytes(), state);
                return termsEnum;
            } else {
                // TermQuery used as a filter, so the term states have not been built up front
                Terms terms = context.reader().terms(term.field());
                if (terms == null) {
                    return null;
                }
                final TermsEnum termsEnum = terms.iterator();
                if (termsEnum.seekExact(term.bytes())) {
                    return termsEnum;
                } else {
                    return null;
                }
            }
        }

        private boolean termNotInReader(LeafReader reader, Term term) throws IOException {
            return reader.docFreq(term) == 0;
        }

        @Override
        public Explanation explain(LeafReaderContext context, int doc) throws IOException {
            Scorer scorer = scorer(context);
            if (scorer != null) {
                int newDoc = scorer.iterator().advance(doc);
                if (newDoc == doc) {
                    float freq = scorer.freq();
                    Similarity.SimScorer docScorer = similarity.simScorer(stats, context);
                    Explanation freqExplanation = Explanation.match(freq, "termFreq=" + freq);
                    Explanation scoreExplanation = docScorer.explain(doc, freqExplanation);
                    return Explanation.match(
                            scoreExplanation.getValue(),
                            "weight(" + getQuery() + " in " + doc + ") ["
                                    + similarity.getClass().getSimpleName() + "], result of:",
                            scoreExplanation);
                }
            }
            return Explanation.noMatch("no matching term");
        }
    }

    public FeatureHashQuery(Term t, String luceneFieldName, byte[] lireFeature, FeatureScoreCache imageScoreCache,
                            float boost) {
        this.term = t;
        this.luceneFieldName = luceneFieldName;
        this.lireFeature = lireFeature;
        this.imageScoreCache = imageScoreCache;
        this.boost = boost;
    }

    public Term getTerm() {
        return term;
    }

    @Override
    public Weight createWeight(IndexSearcher searcher, boolean needsScores) throws IOException {
        final IndexReaderContext context = searcher.getTopReaderContext();
        final TermContext termState = TermContext.build(context, term);
        return new FeatureHashWeight(searcher, termState);
    }

    /* @Override */
    public void extractTerms(Set<Term> terms) {
        terms.add(getTerm());
    }

    @Override
    public String toString(String field) {
        StringBuilder buffer = new StringBuilder();
        if (!term.field().equals(field)) {
            buffer.append(term.field());
            buffer.append(":");
        }
        buffer.append(term.text());
        buffer.append(";");
        buffer.append(luceneFieldName);
        buffer.append(",");
        buffer.append(lireFeature.getClass().getSimpleName());
        buffer.append(Float.toString(boost));
        return buffer.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof FeatureHashQuery))
            return false;
        FeatureHashQuery other = (FeatureHashQuery) o;
        return (this.boost == other.boost) && this.term.equals(other.term) && this.luceneFieldName.equals(other.luceneFieldName);
//                && this.lireFeature.equals(other.lireFeature);
    }

    @Override
    public int hashCode() {
        int result = classHash();
        result = 31 * result + term.hashCode();
        result = 31 * result + luceneFieldName.hashCode();
        result = 31 * result + Arrays.hashCode(lireFeature);
        result = Float.floatToIntBits(boost) ^ result;
        return result;
    }
}

package com.broadtech.qp.query.custom;


import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.util.Set;

/**
 * Created by Chen Yuanjun on 2017/8/8.
 */
public class CustomQuery extends Query {
    private Term queryTerm;

    public CustomQuery(Term term) {
        this.queryTerm = term;
    }

    @Override
    public String toString(String field) {
        return field;
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public Weight createWeight(IndexSearcher searcher, boolean needsScores) throws IOException {
        return new CustomWeight(this, searcher, needsScores);
    }

    final class CustomWeight extends Weight {
        private IndexSearcher searcher;
        private CustomQuery query;
        private boolean needsScores;

        public CustomWeight(CustomQuery query, IndexSearcher searcher, boolean needsScores) {
            super(query);
            this.query = query;
            this.searcher = searcher;
            this.needsScores = needsScores;
        }

        @Override
        public void extractTerms(Set<Term> terms) {

        }

        @Override
        public Explanation explain(LeafReaderContext context, int doc) throws IOException {
            return null;
        }

        @Override
        public float getValueForNormalization() throws IOException {
            return 0;
        }

        @Override
        public void normalize(float norm, float boost) {

        }

        @Override
        public Scorer scorer(LeafReaderContext context) throws IOException {
            // 此处逻辑现有框架不能省略，因为在Weight::DefaultBulkScorer初始化的时候就调用了
            // Scorer::iterator()方法保持PostingsEnum引用
            final TermsEnum termsEnum = getTermsEnum(context);
            if (termsEnum == null) {
                return null;
            }
            PostingsEnum docs = termsEnum.postings(null, needsScores ? PostingsEnum.FREQS : PostingsEnum.NONE);
            assert docs != null;
            return new CustomScorer(this, docs);
        }

        /**
         * 【重点】： seekExact()
         *
         * @throws IOException
         */
        private TermsEnum getTermsEnum(LeafReaderContext context) throws IOException {
            Terms terms = context.reader().terms(query.queryTerm.field());
            if (terms == null) {
                return null;
            }
            final TermsEnum termsEnum = terms.iterator();
            BytesRef byteBuff = termsEnum.next();
            while (byteBuff != null) {
                if (prefixMatch(byteBuff, query.queryTerm.bytes())) {
                    return termsEnum;
                }
                byteBuff = termsEnum.next();
            }
//            if (termsEnum.seekExact(query.queryTerm.bytes())) {
//                return termsEnum;
//            } else {
//                return null;
//            }
            return null;
        }

        private boolean prefixMatch(BytesRef byteBuff, BytesRef queryBuff) {
            if (byteBuff.length <= 0 || byteBuff.length > queryBuff.length) return false;
            int cmp;
            for (int bytePos = 0; bytePos < byteBuff.length; bytePos++) {
                cmp = (byteBuff.bytes[bytePos] & 0xFF) - (queryBuff.bytes[bytePos] & 0xFF);
                if (cmp != 0) return false;
            }
            return true;
        }
    }

    final class CustomScorer extends Scorer {
        private final PostingsEnum postingsEnum;

        /**
         * Constructs a Scorer
         *
         * @param weight The scorers <code>Weight</code>.
         */
        protected CustomScorer(Weight weight, PostingsEnum td) {
            super(weight);
            this.postingsEnum = td;
        }


        @Override
        public int docID() {
            return postingsEnum.docID();
        }

        @Override
        public int freq() throws IOException {
            return postingsEnum.freq();
        }

        @Override
        public DocIdSetIterator iterator() {
            return postingsEnum;
        }

        @Override
        public float score() throws IOException {
            return 1.0f;
        }

        /**
         * Returns a string representation of this <code>TermScorer</code>.
         */
        @Override
        public String toString() {
            return "scorer(" + weight + ")[" + super.toString() + "]";
        }
    }
}
